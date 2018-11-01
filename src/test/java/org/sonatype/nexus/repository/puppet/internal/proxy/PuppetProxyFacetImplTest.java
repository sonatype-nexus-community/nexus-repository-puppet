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
package org.sonatype.nexus.repository.puppet.internal.proxy;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.common.collect.AttributesMap;
import org.sonatype.nexus.repository.puppet.internal.AssetKind;
import org.sonatype.nexus.repository.puppet.internal.util.PuppetAttributeParser;
import org.sonatype.nexus.repository.puppet.internal.util.PuppetDataAccess;
import org.sonatype.nexus.repository.puppet.internal.util.PuppetPathUtils;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Parameters;
import org.sonatype.nexus.repository.view.Request;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

public class PuppetProxyFacetImplTest
    extends TestSupport
{
  private PuppetProxyFacetImpl underTest;

  @Mock
  private PuppetPathUtils puppetPathUtils;

  @Mock
  private PuppetDataAccess puppetDataAccess;

  @Mock
  private PuppetAttributeParser puppetAttributeParser;

  @Mock
  private Context context;

  @Mock
  private Request request;

  @Mock
  private AttributesMap attributesMap;

  @Before
  public void setUp() throws Exception {
    underTest = new PuppetProxyFacetImpl(puppetPathUtils, puppetDataAccess, puppetAttributeParser);
  }

  @Test
  public void returnUrlWithParametersIfModuleReleaseByName() throws Exception {
    setupMocks("/do/a/thing", AssetKind.MODULE_RELEASES_BY_NAME);
    setupParameters();
    String result = underTest.getUrl(context);

    assertThat(result, is(equalTo("do/a/thing?module=puppetlabs-stdlib&sort_by=version")));
  }

  @Test
  public void returnUrlWithoutParametersIfNotModuleReleaseByName() throws Exception {
    setupMocks("/do/a/different/thing", AssetKind.MODULE_RELEASE_BY_NAME_AND_VERSION);
    String result = underTest.getUrl(context);

    assertThat(result, is(equalTo("do/a/different/thing")));
  }

  private void setupMocks(final String path,
                          final AssetKind assetKind)
  {
    when(context.getRequest()).thenReturn(request);
    when(request.getPath()).thenReturn(path);
    when(context.getAttributes()).thenReturn(attributesMap);
    when(attributesMap.require(AssetKind.class)).thenReturn(assetKind);
  }

  private void setupParameters() {
    ListMultimap<String, String> entries = MultimapBuilder.linkedHashKeys().arrayListValues().build();
    entries.put("module", "puppetlabs-stdlib");
    entries.put("sort_by", "version");
    Parameters parameters = new Parameters(entries);

    when(request.getParameters()).thenReturn(parameters);
  }

}
