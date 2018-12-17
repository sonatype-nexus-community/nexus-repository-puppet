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

import java.util.Map.Entry;

import javax.inject.Inject;

import org.sonatype.nexus.repository.puppet.internal.util.PuppetPathUtils;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Parameters;

import static com.google.common.base.Preconditions.checkNotNull;

public class ModuleReleasesBuilder
{
  private PuppetPathUtils puppetPathUtils;

  @Inject
  public ModuleReleasesBuilder(final PuppetPathUtils puppetPathUtils) {
    this.puppetPathUtils = checkNotNull(puppetPathUtils);
  }

  public ModuleReleases parse(final long total, final long limit, final long offset, final Context context) {
    ModuleReleases releases = new ModuleReleases();
    releases.setPagination(parsePagination(total, limit, offset, context));
    return releases;
  }

  private ModulePagination parsePagination(final long total, final long limit, final long offset, final Context context) {
    ModulePagination modulePagination = new ModulePagination();
    Parameters parameters = context.getRequest().getParameters();

    modulePagination.setTotal(total);
    modulePagination.setLimit(limit);
    modulePagination.setOffset(offset);

    Parameters newParameters = new Parameters();
    for (Entry<String, String> param: parameters) {
      newParameters.set(param.getKey(), param.getValue());
    }

    newParameters.replace("offset", "0");
    modulePagination.setFirst(puppetPathUtils.buildModuleReleaseByNamePath(newParameters));

    newParameters.replace("offset", Long.toString(offset));
    modulePagination.setCurrent(puppetPathUtils.buildModuleReleaseByNamePath(newParameters));

    if (offset - limit > 0) {
      newParameters.replace("offset", Long.toString(offset - limit));
      modulePagination.setPrevious(puppetPathUtils.buildModuleReleaseByNamePath(newParameters));
    }
    else {
      modulePagination.setPrevious(null);
    }

    if (offset + limit < total) {
      newParameters.replace("total", Long.toString(offset + limit));
      modulePagination.setNext(puppetPathUtils.buildModuleReleaseByNamePath(newParameters));
    }
    else {
      modulePagination.setNext(null);
    }

    return modulePagination;
  }
}
