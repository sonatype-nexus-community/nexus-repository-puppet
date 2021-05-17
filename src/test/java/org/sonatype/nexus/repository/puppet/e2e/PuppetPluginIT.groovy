package org.sonatype.nexus.repository.puppet.e2e

import groovy.json.JsonSlurper
import org.apache.commons.codec.binary.Hex
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.SystemUtils
import org.junit.Assume
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification

import java.nio.charset.StandardCharsets
import java.nio.file.FileSystems
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import java.security.MessageDigest
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingDeque

class PuppetPluginIT extends Specification {
  private static final Logger LOG = LoggerFactory.getLogger(PuppetPluginIT)

  def 'test if Nexus boots well with Puppet plugin'() {
    given:
    def nexus = installNexus(this.&installPuppetJar as Closure<Path>)

    when:
    def retcode = runNexus(nexus)

    then:
    retcode == 0
  }

  int runNexus(TestNexus nexus) {
    def procBuild = new ProcessBuilder(
      nexus.path.resolve('bin').resolve('nexus').toString(),
      'run'
    )
    def server = procBuild.start()
    def shutdown = false
    BlockingQueue<String> logs = new LinkedBlockingDeque<>(10_000)
    RuntimeException thrown

    def nexusStreamer = new Thread(new Runnable() {
      @Override
      void run() {
        new BufferedReader(new InputStreamReader(server.getInputStream()))
          .withCloseable { BufferedReader input ->
            new BufferedReader(new InputStreamReader(server.getErrorStream()))
              .withCloseable { BufferedReader error ->
                String line
                try {
                  while (!shutdown) {
                    while ((line = input.readLine()) != null) {
                      System.out.println(line)
                      logs.add(line)
                    }
                    while ((line = error.readLine()) != null) {
                      System.err.println(line)
                    }
                  }
                } catch (IOException ex) {
                  if (!shutdown) {
                    throw ex
                  }
                }
              }
          }
      }
    }, 'nexus-streamer')
    def logsProcessor = new Thread(new Runnable() {
      @Override
      void run() {
        while (!shutdown) {
          def line = logs.take()
          if (line.contains('BROKEN org.sonatype.nexus.plugins.nexus-repository-puppet')) {
            thrown = new IllegalStateException('The Puppet plugin is broken. See logs.')
            shutdown = true
            server.destroy()
          }
          if (line.contains("Started Sonatype Nexus OSS ${nexus.version}")) {
            LOG.info('Nexus started')
            shutdown = true
            server.destroy()
          }
        }
      }
    }, 'logs-processor')

    nexusStreamer.start()
    logsProcessor.start()

    server.waitFor()
    shutdown = true
    nexusStreamer.join()
    logsProcessor.join()

    if (thrown != null) {
      throw thrown
    }
    return server.exitValue()
  }

  void installPuppetJar(Path appDir) {
    def target = appDir
      .resolve('deploy')
      .resolve('nexus-repository-puppet.jar')
    def source = jar()
    Files.copy(source, target)
    LOG.info('Puppet plugin installed: {}', source)
  }

  TestNexus installNexus(Closure<Path>... configurers) {
    def slurper = new JsonSlurper()
    def project = slurper.parse(PuppetPluginIT.getResource("/project.json"))
    def version = project.parent.version as String
    def tgz = downloadNexus(version)
    def appDir = extract(tgz).resolve("nexus-${version}")
    configurers.each { it(appDir) }
    LOG.info('Nexus ({}) installed', version)
    return new TestNexus(path: appDir, version: version)
  }

  Path extract(Path tgz) {
    def dir = tgz.parent
    def nexus = dir.resolve('nexus')
    if (Files.exists(nexus)) {
      FileUtils.deleteDirectory(nexus.toFile())
    }
    Files.createDirectories(nexus)
    tgz.newInputStream().withCloseable {
      extractTarGZ(it, nexus)
    }
    return nexus
  }

  void extractTarGZ(InputStream is, Path basedir) {
    int BUFFER_SIZE = 100_000
    GzipCompressorInputStream gzipIs = new GzipCompressorInputStream(is)
    new TarArchiveInputStream(gzipIs).withCloseable { TarArchiveInputStream tarIs ->
      TarArchiveEntry entry

      while ((entry = (TarArchiveEntry) tarIs.getNextEntry()) != null) {
        def f = basedir.resolve(entry.getName())
        /** If the entry is a directory, create the directory. **/
        if (entry.isDirectory()) {
          Files.createDirectories(f)
        } else {
          int count
          def data = new byte[BUFFER_SIZE]
          Files.createDirectories(f.parent)
          f.newOutputStream().withCloseable {
            new BufferedOutputStream(it, BUFFER_SIZE).withCloseable { BufferedOutputStream dest ->
              while ((count = tarIs.read(data, 0, BUFFER_SIZE)) != -1) {
                dest.write(data, 0, count)
              }
            }
          }
          Files.setPosixFilePermissions(f, fromInt(entry.mode))
        }
      }
    }
  }

  Set<PosixFilePermission> fromInt(int perms) {
    def ds = Integer.toString(perms, 8)
      .padLeft(4, '0')
      .toCharArray()

    def ss = ['-', '-', '-', '-', '-', '-', '-', '-', '-']
    for (int i = ds.length - 1; i > 0; i--) {
      int n = ds[i] - '0'.toCharacter()
      if (i == ds.length - 1) {
        if ((n & 1) != 0) ss[8] = 'x'
        if ((n & 2) != 0) ss[7] = 'w'
        if ((n & 4) != 0) ss[6] = 'r'
      } else if (i == ds.length - 2) {
        if ((n & 1) != 0) ss[5] = 'x'
        if ((n & 2) != 0) ss[4] = 'w'
        if ((n & 4) != 0) ss[3] = 'r'
      } else if (i == ds.length - 3) {
        if ((n & 1) != 0) ss[2] = 'x'
        if ((n & 2) != 0) ss[1] = 'w'
        if ((n & 4) != 0) ss[0] = 'r'
      }
    }
    String sperms = ss.join('')
    return PosixFilePermissions.fromString(sperms)
  }

  Path downloadNexus(String version) {
    Assume.assumeTrue(SystemUtils.IS_OS_UNIX)
    def url = "https://download.sonatype.com/nexus/3/nexus-${version}-unix.tar.gz"
    def dir = Paths.get('target', version).toAbsolutePath()
    def filename = "nexus-${version}-unix.tar.gz"
    def filepath = dir.resolve(filename)
    if (Files.isRegularFile(filepath)) {
      LOG.info("Already downloaded: {}", filename)
      return filepath
    }
    def digestUrl = "${url}.sha1"
    def digestFilepath = dir.resolve("${filename}.sha1")
    try {
      downloadFile(URI.create(digestUrl), digestFilepath)
      downloadFile(URI.create(url), filepath)
      validateDigest(filepath, digestFilepath)
    } catch (RuntimeException ex) {
      deleteIfExists(digestFilepath, filepath)
      throw ex
    } finally {
      deleteIfExists(digestFilepath)
    }
    filepath
  }

  void validateDigest(
    Path filePath, Path digestPath,
    MessageDigest algo = MessageDigest.getInstance('SHA-1')
  ) {
    def want = new String(digestPath.readBytes(), StandardCharsets.UTF_8)
      .trim()
      .toLowerCase(Locale.ENGLISH)
    LOG.info("Validating {} to have {}: {}", filePath.fileName, algo.algorithm, want)
    int KB = 1024
    int MB = 1024 * KB
    filePath.eachByte(MB) { byte[] buf, int bytesRead ->
      algo.update(buf, 0, bytesRead)
    }
    def got = Hex.encodeHexString(algo.digest()).toLowerCase(Locale.ENGLISH)
    if (want == got) {
      LOG.info("The {} is valid", filePath.fileName)
    } else {
      throw new IllegalStateException("Downloaded file digest is not matching, " +
        "want = '${want}', got = '${got}'")
    }
  }

  void downloadFile(URI uri, Path filepath) {
    LOG.info('Downloading: {}', uri)
    FileUtils.copyURLToFile(
      uri.toURL(), filepath.toFile()
    )
  }

  Path jar() {
    def targetDir = PuppetPluginIT.getResource("/")
      .toURI()
      .resolve("..")
    def glob = "glob:nexus-repository-puppet-*.jar"
    find(glob, targetDir).get()
  }

  static Optional<Path> find(String glob, URI location) throws IOException {

    PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(glob)
    Path found = null
    Path base = Paths.get(location)
    Files.walkFileTree(base, new SimpleFileVisitor<Path>() {

      @Override
      FileVisitResult visitFile(Path fullpath,
                                BasicFileAttributes attrs) throws IOException {
        Path path = base.relativize(fullpath)
        if (pathMatcher.matches(path)) {
          found = fullpath
          return FileVisitResult.TERMINATE
        }
        return FileVisitResult.CONTINUE
      }

      @Override
      FileVisitResult visitFileFailed(Path file, IOException exc)
        throws IOException {
        return FileVisitResult.CONTINUE
      }
    })
    return Optional.ofNullable(found)
  }

  void deleteIfExists(Path... paths) {
    paths.each { Files.deleteIfExists(it) }
  }

  static class TestNexus {
    String version
    Path path
  }

}
