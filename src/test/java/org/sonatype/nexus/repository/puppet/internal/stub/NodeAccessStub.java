package org.sonatype.nexus.repository.puppet.internal.stub;

import org.sonatype.nexus.common.node.NodeAccess;

import javax.inject.Named;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:krzysztof.suszynski@wavesoftware.pl">Krzysztof Suszynski</a>
 * @since 0.1.0
 */
@Named
public final class NodeAccessStub implements NodeAccess {
    @Override
    public Certificate getCertificate() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getFingerprint() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getId() {
        return "D3817";
    }

    @Override
    public boolean isClustered() {
        return false;
    }

    @Override
    public Set<String> getMemberIds() {
        return Collections.singleton(getId());
    }

    @Override
    public boolean isOldestNode() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Map<String, String> getMemberAliases() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void start() throws Exception {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void stop() throws Exception {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
