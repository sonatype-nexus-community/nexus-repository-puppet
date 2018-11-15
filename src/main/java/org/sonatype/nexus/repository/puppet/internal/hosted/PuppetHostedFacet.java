package org.sonatype.nexus.repository.puppet.internal.hosted;

import java.io.IOException;

import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.Facet.Exposed;
import org.sonatype.nexus.repository.puppet.internal.AssetKind;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Payload;

@Exposed
public interface PuppetHostedFacet
    extends Facet
{
  Content get(String path);

  void upload(String path, Payload payload, final AssetKind assetKind) throws IOException;
}
