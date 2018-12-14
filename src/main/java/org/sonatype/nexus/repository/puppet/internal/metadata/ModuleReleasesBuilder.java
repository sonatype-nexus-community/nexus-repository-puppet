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
package org.sonatype.nexus.repository.puppet.internal.metadata;

public class ModuleReleasesBuilder
{
  public ModuleReleasesBuilder() {
    // no-op
  }

  public ModuleReleases parse(final long total, final long limit, final long offset) {
    ModuleReleases releases = new ModuleReleases();
    releases.setPagination(parsePagination(total, limit, offset));
    return releases;
  }

  private ModulePagination parsePagination(final long total, final long limit, final long offset) {
    ModulePagination modulePagination = new ModulePagination();

    modulePagination.setTotal(total);
    modulePagination.setLimit(limit);
    modulePagination.setOffset(offset);

    // TODO: create first, previous, current, and next query URLs

    return modulePagination;
  }
}
