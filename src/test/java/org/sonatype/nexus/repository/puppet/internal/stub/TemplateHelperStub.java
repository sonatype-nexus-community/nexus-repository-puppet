package org.sonatype.nexus.repository.puppet.internal.stub;

import org.sonatype.nexus.common.template.TemplateHelper;
import org.sonatype.nexus.common.template.TemplateParameters;

import javax.inject.Named;
import java.net.URL;

/**
 * @author <a href="mailto:krzysztof.suszynski@wavesoftware.pl">Krzysztof Suszynski</a>
 * @since 0.1.0
 */
@Named
public final class TemplateHelperStub implements TemplateHelper {
    @Override
    public TemplateParameters parameters() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String render(URL template, TemplateParameters parameters) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
