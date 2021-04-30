package org.sonatype.nexus.repository.puppet.internal.stub;

import org.sonatype.nexus.common.app.ApplicationVersion;

final class ApplicationVersionStub implements ApplicationVersion {
  @Override
  public String getVersion() {
    return "1.2.3-TEST";
  }

  @Override
  public String getEdition() {
    return getVersion();
  }

  @Override
  public String getBrandedEditionAndVersion() {
    return getVersion();
  }

  @Override
  public String getBuildRevision() {
    return getVersion();
  }

  @Override
  public String getBuildTimestamp() {
    return getVersion();
  }

  @Override
  public String getNexus2CompatibleVersion() {
    return getVersion();
  }
}
