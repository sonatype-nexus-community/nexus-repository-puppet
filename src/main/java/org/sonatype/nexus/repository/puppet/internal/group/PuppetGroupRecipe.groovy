package org.sonatype.nexus.repository.puppet.internal.group

import org.sonatype.nexus.repository.Format
import org.sonatype.nexus.repository.Repository
import org.sonatype.nexus.repository.Type
import org.sonatype.nexus.repository.http.HttpHandlers
import org.sonatype.nexus.repository.puppet.internal.PuppetFormat
import org.sonatype.nexus.repository.puppet.internal.PuppetRecipeSupport
import org.sonatype.nexus.repository.types.GroupType
import org.sonatype.nexus.repository.view.ConfigurableViewFacet
import org.sonatype.nexus.repository.view.Route
import org.sonatype.nexus.repository.view.Router
import org.sonatype.nexus.repository.view.ViewFacet
import org.sonatype.nexus.repository.view.handlers.BrowseUnsupportedHandler

import javax.annotation.Nonnull
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

/**
 * @author <a href="mailto:krzysztof.suszynski@wavesoftware.pl">Krzysztof Suszynski</a>
 * @since 0.1.0
 */
@Named(PuppetGroupRecipe.NAME)
@Singleton
final class PuppetGroupRecipe extends PuppetRecipeSupport {
    public static final String NAME = 'puppet-group'

    @Inject
    private Provider<PuppetGroupFacet> groupFacet

    @Inject
    private PuppetMergingHandler mergingHandler

    @Inject
    PuppetGroupRecipe(
            @Named(GroupType.NAME) final Type type,
            @Named(PuppetFormat.NAME) final Format format
    ) {
        super(type, format)
    }

    @Override
    void apply(@Nonnull Repository repository) throws Exception {
        repository.attach(securityFacet.get())
        repository.attach(httpClientFacet.get())
        repository.attach(componentMaintenanceFacet.get())
        repository.attach(storageFacet.get())
        repository.attach(groupFacet.get())
        repository.attach(searchFacet.get())
        repository.attach(attributesFacet.get())
        repository.attach(configure(viewFacet.get()))
    }

    /**
     * Configure {@link org.sonatype.nexus.repository.view.ViewFacet}.
     */
    private ViewFacet configure(final ConfigurableViewFacet facet) {
        Router.Builder builder = new Router.Builder()

        def matchers = [
                moduleReleaseByNameAndVersionMatcher(),
                moduleReleasesSearchByNameMatcher(),
                moduleDownloadMatcher(),
                moduleByNameMatcher()
        ]
        matchers.each { matcher ->
            builder.route(new Route.Builder().matcher(matcher)
                    .handler(timingHandler)
                    .handler(securityHandler)
                    .handler(exceptionHandler)
                    .handler(handlerContributor)
                    .handler(partialFetchHandler)
                    .handler(contentHeadersHandler)
                    .handler(unitOfWorkHandler)
                    .handler(mergingHandler)
                    .create())
        }

        builder.route(new Route.Builder()
                .matcher(BrowseUnsupportedHandler.MATCHER)
                .handler(browseUnsupportedHandler)
                .create())

        builder.defaultHandlers(HttpHandlers.notFound())

        facet.configure(builder.create())

        return facet
    }
}
