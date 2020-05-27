package org.sonatype.nexus.repository.puppet.internal.stub;

import org.sonatype.nexus.security.ClientInfo;
import org.sonatype.nexus.security.ClientInfoProvider;

import javax.inject.Named;

/**
 * @author <a href="mailto:krzysztof.suszynski@wavesoftware.pl">Krzysztof Suszynski</a>
 * @since 0.1.0
 */
@Named
public final class ClientInfoProviderStub implements ClientInfoProvider {
    @Override
    public ClientInfo getCurrentThreadClientInfo() {
        return new ClientInfo(
          "h3lly3a!", "127.0.0.1", "JUnit/4.12"
        );
    }
}
