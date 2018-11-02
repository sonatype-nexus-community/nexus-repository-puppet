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

import java.io.InputStream;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.mime.MimeRulesSource;
import org.sonatype.nexus.repository.storage.DefaultContentValidator;

import com.google.common.base.Supplier;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static org.sonatype.nexus.repository.view.ContentTypes.*;

public class PuppetContentValidatorTest
    extends TestSupport
{
  private final static String TEST_JSON_CONTENT_NAME = "testjsonwithoutextension";

  private final static String TEST_TAR_GZ_CONTENT_NAME = "test.tar.gz";

  private PuppetContentValidator underTest;

  @Mock
  private DefaultContentValidator defaultContentValidator;

  @Mock
  private Supplier<InputStream> contentStream;

  @Mock
  private MimeRulesSource mimeRulesSource;

  @Before
  public void setUp() throws Exception {
    underTest = new PuppetContentValidator(defaultContentValidator);
  }

  @Test
  public void returnJsonTypeWhenValidatingJson() throws Exception {
    setUpMock(TEST_JSON_CONTENT_NAME + ".json", APPLICATION_JSON, false);

    String result = underTest.determineContentType(
        true, contentStream, mimeRulesSource, TEST_JSON_CONTENT_NAME, APPLICATION_JSON
    );

    assertThat(result, is(equalTo(APPLICATION_JSON)));
  }

  @Test
  public void returnGzipTypeWhenValidatingTarGz() throws Exception {
    setUpMock(TEST_TAR_GZ_CONTENT_NAME, APPLICATION_GZIP, true);

    String result = underTest.determineContentType(
        true, contentStream, mimeRulesSource, TEST_TAR_GZ_CONTENT_NAME, APPLICATION_GZIP
    );

    assertThat(result, is(equalTo(APPLICATION_GZIP)));
  }

  private void setUpMock(final String contentName,
                         final String contentType,
                         final boolean strictContentTypeValidation) throws Exception
  {
    when(defaultContentValidator.determineContentType(
            strictContentTypeValidation, contentStream, mimeRulesSource, contentName, contentType)
    ).thenReturn(contentType);
  }
}
