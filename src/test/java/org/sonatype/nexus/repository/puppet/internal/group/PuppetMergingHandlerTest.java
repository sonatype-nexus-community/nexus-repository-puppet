package org.sonatype.nexus.repository.puppet.internal.group;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Binder;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.eclipse.sisu.launch.InjectedTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonatype.nexus.blobstore.api.BlobStore;
import org.sonatype.nexus.blobstore.api.BlobStoreConfiguration;
import org.sonatype.nexus.blobstore.api.BlobStoreManager;
import org.sonatype.nexus.orient.DatabaseInstanceNames;
import org.sonatype.nexus.orient.DatabaseManager;
import org.sonatype.nexus.orient.testsupport.DatabaseInstanceRule;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.config.ConfigurationStore;
import org.sonatype.nexus.repository.config.internal.orient.OrientConfiguration;
import org.sonatype.nexus.repository.group.GroupHandler.DispatchedRepositories;
import org.sonatype.nexus.repository.internal.blobstore.BlobStoreConfigurationStore;
import org.sonatype.nexus.repository.internal.blobstore.orient.OrientBlobStoreConfiguration;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.repository.puppet.internal.hosted.PuppetHostedRecipe;
import org.sonatype.nexus.repository.puppet.internal.metadata.ModuleReleases;
import org.sonatype.nexus.repository.puppet.internal.metadata.ModuleReleasesResult;
import org.sonatype.nexus.repository.puppet.internal.proxy.PuppetProxyRecipe;
import org.sonatype.nexus.repository.puppet.internal.stub.StubModule;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.internal.ComponentSchemaRegistration;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Request;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.transaction.TransactionModule;
import org.sonatype.nexus.transaction.UnitOfWork;

import javax.inject.Inject;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.HashMap;
import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.sonatype.nexus.repository.config.WritePolicy.ALLOW_ONCE;
import static org.sonatype.nexus.repository.puppet.internal.group.PuppetMergingHandler.readReleases;

/**
 * @author <a href="mailto:krzysztof.suszynski@wavesoftware.pl">Krzysztof Suszynski</a>
 * @since 0.1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class PuppetMergingHandlerTest extends InjectedTest {

  private static final Map<String, Object> STORAGE_ATTRIBUTES = imap()
    .put("blobStoreName", "junit")
    .put("strictContentTypeValidation", true)
    .put("writePolicy", ALLOW_ONCE)
    .build();
  @Rule
  public DatabaseInstanceRule databaseInstanceRule = DatabaseInstanceRule
    .inMemory(DatabaseInstanceNames.CONFIG);
  @Mock
  private ConfigurationStore configurationStore;
  @Mock
  private SecurityManager securityManager;
  @Mock
  private Subject subject;
  @Inject
  private RepositoryManager repositoryManager;
  @Inject
  private BlobStoreManager blobStoreManager;
  @Inject
  private PuppetMergingHandler mergingHandler;
  @Inject
  private ComponentSchemaRegistration componentSchemaRegistration;
  @Inject
  private BlobStoreConfigurationStore blobStoreConfigurationStore;

  private static ImmutableMap.Builder<String, Object> imap() {
    return ImmutableMap.builder();
  }

  @Override
  public void configure(Binder binder) {
    binder.bind(Validator.class).toInstance(
      Validation.buildDefaultValidatorFactory().getValidator()
    );
    binder.bind(ConfigurationStore.class).toInstance(configurationStore);
    binder.install(new TransactionModule());
    binder.install(new StubModule());
    binder.bind(DatabaseManager.class).toInstance(databaseInstanceRule.getManager());
  }

  @Test
  public void testDoGetOnV3ApiReleases() throws Exception {
    // given
    ThreadContext.bind(securityManager);
    when(securityManager.createSubject(any())).thenReturn(subject);
    when(subject.isPermitted(any(Permission.class))).thenReturn(true);
    componentSchemaRegistration.start();
    blobStoreConfigurationStore.start();
    blobStoreManager.start();
    Repository groupRepo = defineExampleGroupRepository();

    Request request = new Request.Builder()
      .action("GET")
      .path("/v3/releases")
      .parameter("module", "coi-jboss")
      .parameter("sort_by", "version")
      .parameter("exclude_fields", "readme,changelog,license,uri,module," +
        "tags,supported,file_size,downloads,created_at,updated_at,deleted_at")
      .build();
    Context ctx = new Context(
      groupRepo, request
    );
    DispatchedRepositories dispatched = new DispatchedRepositories();

    // when
    Response response = getResponse(mergingHandler, ctx, dispatched);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getStatus().getCode()).isEqualTo(200);
    assertThat(response.getPayload()).isNotNull();
    ModuleReleases releases = readReleases(response);
    assertThat(releases.getResults()).isNotEmpty();
    ModuleReleasesResult latestRelease = releases.getResults().iterator().next();
    assertThat(latestRelease.getVersion()).matches("^\\d+\\.\\d+\\.\\d+$");
    assertThat(latestRelease.getSlug()).contains("coi-jboss");
  }

  private Repository defineExampleGroupRepository() throws Exception {
    UnitOfWork.begin(lookup(StorageFacet.class).txSupplier());
    try {
      BlobStoreConfiguration blobStoreCfg = new OrientBlobStoreConfiguration();
      blobStoreCfg.setName("junit");
      blobStoreCfg.setType("File");
      blobStoreCfg.setWritable(true);
      blobStoreCfg.attributes("file").set("path", "junit");
      BlobStore defaultBlobStore = blobStoreManager.create(blobStoreCfg);
      assertThat(defaultBlobStore).isNotNull();

      Configuration internalCfg = new OrientConfiguration();
      internalCfg.setRecipeName(PuppetHostedRecipe.NAME);
      internalCfg.setRepositoryName("puppet-internal");
      internalCfg.setOnline(true);
      internalCfg.setAttributes(
        new HashMap<>(
          ImmutableMap.<String, Map<String, Object>>builder()
            .put("storage", STORAGE_ATTRIBUTES)
            .build()
        )
      );
      Repository internalRepo = repositoryManager.create(internalCfg);

      Configuration proxyCfg = new OrientConfiguration();
      proxyCfg.setOnline(true);
      proxyCfg.setRepositoryName("puppet-proxy");
      proxyCfg.setRecipeName(PuppetProxyRecipe.NAME);
      proxyCfg.setAttributes(
        ImmutableMap.<String, Map<String, Object>>builder()
          .put("storage", STORAGE_ATTRIBUTES)
          .put("proxy", imap()
            .put("remoteUrl", "https://forgeapi.puppet.com")
            .put("contentMaxAge", 1440)
            .put("metadataMaxAge", 1440)
            .build())
          .put("httpclient", imap()
            .put("blocked", false)
            .put("autoBlock", true)
            .put("connection", imap().put("useTrustStore", false).build())
            .build())
          .put("negativeCache", imap()
            .put("enabled", true)
            .put("timeToLive", 1440)
            .build())
          .build()
      );
      Repository proxyRepo = repositoryManager.create(proxyCfg);

      assertThat(internalRepo).isNotNull();
      assertThat(proxyRepo).isNotNull();
      Configuration groupCfg = new OrientConfiguration();
      groupCfg.setOnline(true);
      groupCfg.setRepositoryName("puppet-group");
      groupCfg.setRecipeName(PuppetGroupRecipe.NAME);
      groupCfg.setAttributes(
        new HashMap<>(
          ImmutableMap.<String, Map<String, Object>>builder()
            .put("storage", STORAGE_ATTRIBUTES)
            .put("group", imap()
              .put("memberNames", ImmutableList.of("puppet-internal", "puppet-proxy"))
              .build())
            .build()
        )
      );
      return repositoryManager.create(groupCfg);
    } finally {
      UnitOfWork.end();
    }
  }

  private Response getResponse(PuppetMergingHandler mergingHandler, Context ctx, DispatchedRepositories dispatched) {
    try {
      return mergingHandler.doGet(ctx, dispatched);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
