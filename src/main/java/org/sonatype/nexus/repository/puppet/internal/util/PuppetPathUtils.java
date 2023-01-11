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
package org.sonatype.nexus.repository.puppet.internal.util;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Parameters;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher.State;

import com.google.common.base.Joiner;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 0.0.1
 */
@Named
@Singleton
public class PuppetPathUtils
{
  public TokenMatcher.State matcherState(final Context context) {
    return context.getAttributes().require(TokenMatcher.State.class);
  }

  public String user(final TokenMatcher.State state) {
    return match(state, "user");
  }

  public String module(final TokenMatcher.State state) {
    return match(state, "module");
  }

  public String version(final TokenMatcher.State state) {
    return match(state, "version");
  }

  private String match(final TokenMatcher.State state, final String name) {
    checkNotNull(state);
    String result = state.getTokens().get(name);
    checkNotNull(result);
    return result;
  }

  public String buildModuleReleaseByNameAndVersionPath(final State matcherState) {
    return String.format("v3/releases/%s-%s-%s", user(matcherState), module(matcherState), version(matcherState));
  }

  public String buildModuleByNamePath(final State matcherState) {
    return String.format("v3/modules/%s-%s", user(matcherState), module(matcherState));
  }

  public String buildModuleReleaseByNamePath(final Parameters parameters) {
    if (parameters.isEmpty()) {
      return "v3/releases";
    }
    return String.format("v3/releases?%s", Joiner.on("&").withKeyValueSeparator("=").join(parameters));
  }

  public String buildModuleDownloadPath(final State matcherState) {
    return String.format("v3/files/%s-%s-%s.tar.gz", user(matcherState), module(matcherState), version(matcherState));
  }
}
