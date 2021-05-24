package org.sonatype.nexus.repository.puppet.internal.stub;

import org.sonatype.nexus.common.app.ApplicationDirectories;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author <a href="mailto:krzysztof.suszynski@wavesoftware.pl">Krzysztof Suszynski</a>
 * @since 0.1.0
 */
final class ApplicationDirectoriesStub
  implements ApplicationDirectories {

  private final Path tempDir;

  ApplicationDirectoriesStub() {
    try {
      tempDir = Files.createTempDirectory("nexus-puppet-stub-");
      Path esConfigPath = getConfigDirectory("fabric")
        .toPath()
        .resolve("elasticsearch.yml");
      Files.createDirectories(esConfigPath.getParent());
      String config = "path.home: " + getWorkDirectory("es");
      Files.write(esConfigPath, config.getBytes(StandardCharsets.UTF_8));
    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        Files.walk(tempDir)
          .filter(Files::isRegularFile)
          .map(Path::toFile)
          .forEach(File::delete);
      } catch (IOException ex) {
        throw new IllegalStateException(ex);
      }
    }));
  }

  @Override
  public File getInstallDirectory() {
    return tempDir.resolve("install").toFile();
  }

  @Override
  public File getConfigDirectory(String subsystem) {
    return tempDir.resolve("config").resolve(subsystem).toFile();
  }

  @Override
  public File getTemporaryDirectory() {
    return tempDir.resolve("temp").toFile();
  }

  @Override
  public File getWorkDirectory() {
    return getWorkPath().toFile();
  }

  @Override
  public File getWorkDirectory(String path, boolean create) {
    Path work = getWorkPath().resolve(path);
    if (create) {
      createDirectories(work);
    }
    return work.toFile();
  }

  @Override
  public File getWorkDirectory(String path) {
    return getWorkDirectory(path, true);
  }

  private Path getWorkPath() {
    Path work = tempDir.resolve("work");
    createDirectories(work);
    return work;
  }

  private void createDirectories(Path directory) {
    try {
      Files.createDirectories(directory);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
