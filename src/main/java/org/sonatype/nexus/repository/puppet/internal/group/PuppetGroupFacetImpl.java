package org.sonatype.nexus.repository.puppet.internal.group;

import org.sonatype.nexus.repository.Facet;
import org.sonatype.nexus.repository.Type;
import org.sonatype.nexus.repository.group.GroupFacetImpl;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.repository.types.GroupType;
import org.sonatype.nexus.validation.ConstraintViolationFactory;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author <a href="mailto:krzysztof.suszynski@wavesoftware.pl">Krzysztof Suszynski</a>
 * @since 0.1.0
 */
@Named
@Facet.Exposed
public class PuppetGroupFacetImpl
        extends GroupFacetImpl
        implements PuppetGroupFacet {

    @Inject
    public PuppetGroupFacetImpl(
            RepositoryManager repositoryManager,
            ConstraintViolationFactory constraintViolationFactory,
            @Named(GroupType.NAME) Type groupType
    ) {
        super(repositoryManager, constraintViolationFactory, groupType);
    }
}
