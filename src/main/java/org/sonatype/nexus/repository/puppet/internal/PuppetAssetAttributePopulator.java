/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2018-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.puppet.internal;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.nexus.common.collect.NestedAttributesMap;
import org.sonatype.nexus.repository.puppet.internal.metadata.PuppetAttributes;
import org.sonatype.nexus.repository.puppet.internal.metadata.PuppetDependencyAttributes;

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

    if (puppetAttributes.getDependencies() != null) {
      NestedAttributesMap dependencies = attributes.child("dependencies");
      for (PuppetDependencyAttributes puppetDependency : puppetAttributes.getDependencies()) {
        dependencies.set(puppetDependency.getName(), puppetDependency.getVersion_requirement());
      }
    }

  }
}
