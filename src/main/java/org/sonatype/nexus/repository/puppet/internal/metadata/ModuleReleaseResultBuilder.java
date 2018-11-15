package org.sonatype.nexus.repository.puppet.internal.metadata;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.storage.Asset;

@Named
@Singleton
public class ModuleReleaseResultBuilder
{
  public ModuleReleasesResult parse(final Asset asset) {
    ModuleReleasesResult result = new ModuleReleasesResult();
    result.setSlug(asset.name());
    result.setUri(asset.name());

    return result;
  }
}
