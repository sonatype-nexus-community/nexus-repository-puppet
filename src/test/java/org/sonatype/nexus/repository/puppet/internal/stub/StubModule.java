package org.sonatype.nexus.repository.puppet.internal.stub;

import com.google.inject.Binder;
import com.google.inject.Module;
import org.sonatype.nexus.blobstore.api.metrics.BlobStoreMetricsStore;
import org.sonatype.nexus.common.app.ApplicationDirectories;
import org.sonatype.nexus.common.app.ApplicationVersion;
import org.sonatype.nexus.common.event.EventManager;
import org.sonatype.nexus.common.node.NodeAccess;
import org.sonatype.nexus.common.template.TemplateHelper;
import org.sonatype.nexus.security.ClientInfoProvider;
import org.sonatype.nexus.selector.SelectorManager;

public final class StubModule implements Module {
  @Override
  public void configure(Binder binder) {
    binder.bind(ApplicationDirectories.class).to(ApplicationDirectoriesStub.class);
    binder.bind(ApplicationVersion.class).to(ApplicationVersionStub.class);
    binder.bind(ClientInfoProvider.class).to(ClientInfoProviderStub.class);
    binder.bind(EventManager.class).to(GuavaEventManager.class);
    binder.bind(NodeAccess.class).to(NodeAccessStub.class);
    binder.bind(SelectorManager.class).to(SelectorManagerStub.class);
    binder.bind(TemplateHelper.class).to(TemplateHelperStub.class);
    binder.bind(BlobStoreMetricsStore.class).to(BlobStoreMetricsStoreStub.class);
  }
}
