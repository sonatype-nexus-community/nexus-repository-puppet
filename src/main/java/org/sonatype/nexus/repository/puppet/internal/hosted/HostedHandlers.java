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
package org.sonatype.nexus.repository.puppet.internal.hosted;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.nexus.repository.puppet.internal.AssetKind;
import org.sonatype.nexus.repository.puppet.internal.metadata.ModulePagination;
import org.sonatype.nexus.repository.puppet.internal.util.PuppetDataAccess;
import org.sonatype.nexus.repository.puppet.internal.util.PuppetPathUtils;
import org.sonatype.nexus.repository.search.SearchService;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Handler;
import org.sonatype.nexus.repository.view.Parameters;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher.State;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.http.HttpResponses.notFound;
import static org.sonatype.nexus.repository.http.HttpResponses.ok;

@Named
@Singleton
public class HostedHandlers
    extends ComponentSupport
{
  private PuppetPathUtils pathUtils;
  private PuppetDataAccess puppetDataAccess;

  @Inject
  public HostedHandlers(final PuppetPathUtils pathUtils,
                        final PuppetDataAccess puppetDataAccess) {
    this.pathUtils = checkNotNull(pathUtils);
    this.puppetDataAccess= checkNotNull(puppetDataAccess);
  }

  final Handler get = context -> {
    AssetKind assetKind = context.getAttributes().require(AssetKind.class);
    String path;

    State state = context.getAttributes().require(TokenMatcher.State.class);
    path = pathUtils.buildModuleDownloadPath(state);

    Content content = context.getRepository().facet(PuppetHostedFacet.class).get(path);

    return (content != null) ? ok(content) : notFound();
  };

  final Handler upload = context -> {
    State state = context.getAttributes().require(TokenMatcher.State.class);
    String path = pathUtils.buildModuleDownloadPath(state);

    AssetKind assetKind = context.getAttributes().require(AssetKind.class);
    context.getRepository().facet(PuppetHostedFacet.class).upload(path, context.getRequest().getPayload(), assetKind);

    return ok();
  };

  final Handler searchByName = context -> {
    Parameters parameters = context.getRequest().getParameters();

    Content content = context.getRepository().facet(PuppetHostedFacet.class).searchByName(parameters, context);

    return (content != null) ? ok(content) : notFound();
  };

  final Handler moduleByNameAndVersion = context -> {
    State state = context.getAttributes().require(TokenMatcher.State.class);

    String user = pathUtils.user(state);
    String module = pathUtils.module(state);
    String version = pathUtils.module(state);

    Content content = context.getRepository().facet(PuppetHostedFacet.class).moduleByNameAndVersion(user, module, version);

    return (content != null) ? ok(content) : notFound();
  };
}
