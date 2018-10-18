package org.sonatype.nexus.repository.puppet.internal.util;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.repository.puppet.internal.metadata.PuppetAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;

import static com.google.common.base.Preconditions.checkNotNull;

@Named
@Singleton
public class PuppetAttributeParser
{
  private TgzParser tgzParser;

  private ObjectMapper objectMapper;

  @Inject
  public PuppetAttributeParser(final TgzParser tgzParser,
                               final ObjectMapper objectMapper) {
    this.tgzParser = checkNotNull(tgzParser);
    this.objectMapper = checkNotNull(objectMapper);
  }

  public PuppetAttributes getAttributesFromInputStream(final InputStream inputStream) throws IOException {
    try (InputStream is = tgzParser.getMetadataFromInputStream(inputStream)) {
      PuppetAttributes puppetAttributes = this.objectMapper.readValue(is, PuppetAttributes.class);

      return puppetAttributes;
    }
  }
}
