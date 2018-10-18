package org.sonatype.nexus.repository.puppet.internal.util;

import java.io.InputStream;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.repository.storage.TempBlob;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

public class TgzParserTest
    extends TestSupport
{
  private TgzParser underTest;

  @Mock
  private TempBlob tempBlob;

  @Before
  public void setUp() throws Exception {
    this.underTest = new TgzParser();
  }

  @Test
  public void getMetadataFromTgzTest() throws Exception {
    InputStream is = getClass().getResourceAsStream("puppetlabs-stdlib-5.1.0.tar.gz");
    when(tempBlob.get()).thenReturn(is);

    InputStream metadata = underTest.getMetadataFromInputStream(tempBlob.get());
    assertThat(metadata, is(instanceOf(InputStream.class)));
  }
}