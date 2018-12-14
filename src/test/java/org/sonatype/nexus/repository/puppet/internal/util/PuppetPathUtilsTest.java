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

import java.util.HashMap;
import java.util.Map;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Parameters;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

public class PuppetPathUtilsTest
    extends TestSupport
{
  private PuppetPathUtils underTest;

  @Mock
  Context context;

  @Mock
  AttributesMap attributesMap;

  @Mock
  TokenMatcher.State mockState;

  @Before
  public void setUp() throws Exception {
    underTest = new PuppetPathUtils();
  }

  @Test
  public void testGetMatcherState() throws Exception {
    when(context.getAttributes()).thenReturn(attributesMap);
    when(attributesMap.require(TokenMatcher.State.class)).thenReturn(mockState);
    TokenMatcher.State state = underTest.matcherState(context);

    assertThat(state, is(equalTo(mockState)));
  }

  @Test
  public void buildModuleReleaseByNameAndVersionPath() throws Exception {
    setupTokens();
    String result = underTest.buildModuleReleaseByNameAndVersionPath(mockState);

    assertThat(result, is(equalTo("/v3/releases/puppetlabs-stdlib-5.1.0")));
  }

  @Test
  public void buildModuleReleaseByNamePath() throws Exception {
    // ?module=puppetlabs-stdlib&sort_by=version
    ListMultimap<String, String> entries = MultimapBuilder.linkedHashKeys().arrayListValues().build();
    entries.put("module", "puppetlabs-stdlib");
    entries.put("sort_by", "version");
    Parameters parameters = new Parameters(entries);

    String result = underTest.buildModuleReleaseByNamePath(parameters);

    assertThat(result, is(equalTo("/v3/releases?module=puppetlabs-stdlib&sort_by=version")));
  }

  @Test
  public void buildModuleReleaseByNamePathNoParameters() throws Exception {
    ListMultimap<String, String> entries = MultimapBuilder.linkedHashKeys().arrayListValues().build();
    Parameters parameters = new Parameters(entries);

    String result = underTest.buildModuleReleaseByNamePath(parameters);

    assertThat(result, is(equalTo("/v3/releases")));
  }

  @Test
  public void buildModuleByNamePath() throws Exception {
    setupTokens();

    String result = underTest.buildModuleByNamePath(mockState);

    assertThat(result, is(equalTo("/v3/modules/puppetlabs-stdlib")));
  }

  @Test
  public void buildModuleDownloadPath() throws Exception {
    setupTokens();

    String result = underTest.buildModuleDownloadPath(mockState);

    assertThat(result, is(equalTo("/v3/files/puppetlabs-stdlib-5.1.0.tar.gz")));
  }

  private void setupTokens() {
    Map<String, String> tokens = new HashMap<>();
    tokens.put("user", "puppetlabs");
    tokens.put("module", "stdlib");
    tokens.put("version", "5.1.0");
    when(mockState.getTokens()).thenReturn(tokens);
  }
}
