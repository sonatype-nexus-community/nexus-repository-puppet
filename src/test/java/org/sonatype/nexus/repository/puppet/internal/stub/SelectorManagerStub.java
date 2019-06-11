package org.sonatype.nexus.repository.puppet.internal.stub;

import org.sonatype.nexus.common.entity.EntityId;
import org.sonatype.nexus.selector.SelectorConfiguration;
import org.sonatype.nexus.selector.SelectorEvaluationException;
import org.sonatype.nexus.selector.SelectorManager;
import org.sonatype.nexus.selector.SelectorSqlBuilder;
import org.sonatype.nexus.selector.VariableSource;

import javax.annotation.Nullable;
import javax.inject.Named;
import java.util.List;

/**
 * @author <a href="mailto:krzysztof.suszynski@wavesoftware.pl">Krzysztof Suszynski</a>
 * @since 0.1.0
 */
@Named
public final class SelectorManagerStub implements SelectorManager {
    @Override
    public List<SelectorConfiguration> browse() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public List<SelectorConfiguration> browse(String selectorType) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public List<SelectorConfiguration> browseActive(List<String> repositoryNames, List<String> formats) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Nullable
    @Override
    public SelectorConfiguration read(EntityId entityId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void create(SelectorConfiguration configuration) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void update(SelectorConfiguration configuration) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void delete(SelectorConfiguration configuration) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean evaluate(SelectorConfiguration selectorConfiguration, VariableSource variableSource) throws SelectorEvaluationException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void toSql(SelectorConfiguration selectorConfiguration, SelectorSqlBuilder sqlBuilder) throws SelectorEvaluationException {
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
