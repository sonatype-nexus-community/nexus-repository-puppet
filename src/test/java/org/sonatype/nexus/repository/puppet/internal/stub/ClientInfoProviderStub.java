package org.sonatype.nexus.repository.puppet.internal.stub;

import org.sonatype.nexus.security.ClientInfo;
import org.sonatype.nexus.security.ClientInfoProvider;

/**
 * @author <a href="mailto:krzysztof.suszynski@wavesoftware.pl">Krzysztof Suszynski</a>
 * @since 0.1.0
 */
final class ClientInfoProviderStub implements ClientInfoProvider {
  @Override
  public ClientInfo getCurrentThreadClientInfo() {
    return ClientInfo.builder()
      .userId("h3lly3a!")
      .remoteIP("127.0.0.1")
      .userAgent("JUnit/4.12")
      .build();
  }
}
