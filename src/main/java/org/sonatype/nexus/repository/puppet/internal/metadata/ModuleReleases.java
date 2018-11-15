package org.sonatype.nexus.repository.puppet.internal.metadata;

import java.util.ArrayList;
import java.util.List;

public final class ModuleReleases
{
  private List<ModuleReleasesResult> results;

  public ModuleReleases() {
    this.results = new ArrayList<>();
  }

  public void addResult(final ModuleReleasesResult result) {
    this.results.add(result);
  }

  public List<ModuleReleasesResult> getResults() {
    return this.results;
  }
}
