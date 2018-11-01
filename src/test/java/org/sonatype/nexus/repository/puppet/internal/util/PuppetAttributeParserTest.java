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

import java.io.InputStream;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.repository.puppet.internal.metadata.PuppetAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class PuppetAttributeParserTest
    extends TestSupport
{
  private TgzParser tgzParser;

  private ObjectMapper objectMapper;

  private PuppetAttributeParser underTest;

  @Before
  public void setUp() throws Exception {
    tgzParser = new TgzParser();
    objectMapper = new ObjectMapper();
    underTest = new PuppetAttributeParser(tgzParser, objectMapper);
  }

  @Test
  public void getAttributesFromPuppetModule() throws Exception {
    InputStream module = getClass().getResourceAsStream("puppetlabs-stdlib-5.1.0.tar.gz");
    PuppetAttributes result = underTest.getAttributesFromInputStream(module);

    assertThat(result.getName(), is(notNullValue()));
    assertThat(result.getVersion(), is(notNullValue()));
    assertThat(result.getDescription(), is(notNullValue()));
    assertThat(result.getSummary(), is(notNullValue()));
  }
}
