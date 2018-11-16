package org.sonatype.nexus.repository.puppet.internal;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.nexus.common.collect.NestedAttributesMap;
import org.sonatype.nexus.repository.puppet.internal.metadata.PuppetAttributes;

@Named
@Singleton
public class PuppetAssetAttributePopulator
    extends ComponentSupport
{
  public void populate(final NestedAttributesMap attributes, final PuppetAttributes puppetAttributes) {
    attributes.set("name", puppetAttributes.getName());
    attributes.set("version", puppetAttributes.getVersion());
    attributes.set("summary", puppetAttributes.getSummary());
    attributes.set("description", puppetAttributes.getDescription());
    attributes.set("author", puppetAttributes.getAuthor());
    attributes.set("license", puppetAttributes.getLicense());
    attributes.set("source", puppetAttributes.getSource());
    attributes.set("project_page", puppetAttributes.getProject_page());
    attributes.set("issues_url", puppetAttributes.getIssues_url());
  }
}
