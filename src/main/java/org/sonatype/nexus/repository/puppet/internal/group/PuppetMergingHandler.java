package org.sonatype.nexus.repository.puppet.internal.group;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.group.GroupFacet;
import org.sonatype.nexus.repository.group.GroupHandler;
import org.sonatype.nexus.repository.http.HttpResponses;
import org.sonatype.nexus.repository.puppet.internal.metadata.ModulePagination;
import org.sonatype.nexus.repository.puppet.internal.metadata.ModuleReleases;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Matcher;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.payloads.StringPayload;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import static org.sonatype.nexus.repository.puppet.internal.PuppetRecipeSupport.moduleReleaseByNameAndVersionMatcher;
import static org.sonatype.nexus.repository.puppet.internal.PuppetRecipeSupport.moduleReleasesSearchByNameMatcher;

/**
 * @author <a href="mailto:krzysztof.suszynski@wavesoftware.pl">Krzysztof Suszynski</a>
 * @since 0.1.0
 */
@Named
@Singleton
final class PuppetMergingHandler extends GroupHandler {

  private static final String NOT_FOUND_JSON;

  private final Map<Matcher, Operation> operations =
    ImmutableMap.<Matcher, Operation>builder()
      .put(moduleReleasesSearchByNameMatcher(), this::firstWithResults)
      .put(moduleReleaseByNameAndVersionMatcher(), this::firstWithResults)
      .build();

  static {
    ModuleReleases releases = new ModuleReleases();
    ModulePagination pagination = new ModulePagination();
    releases.setPagination(pagination);
    Gson gson = new Gson();
    NOT_FOUND_JSON = gson.toJson(releases);
  }

  static ModuleReleases readReleases(Response response) throws IOException {
    try (InputStream is = Objects.requireNonNull(response.getPayload()).openInputStream();
         Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
      Gson gson = new Gson();
      return gson.fromJson(reader, ModuleReleases.class);
    }
  }

  @Override
  protected Response doGet(
    @Nonnull Context context,
    @Nonnull DispatchedRepositories dispatched
  ) throws Exception {
    return operations.entrySet()
      .stream()
      .map(entry -> withContext(context, entry))
      .filter(PuppetMergingHandler::matches)
      .map(Entry::getValue)
      .map(Entry::getValue)
      .findFirst()
      .orElse(this::takeFirst)
      .perform(context, dispatched);
  }

  private static boolean matches(Entry<Context, Entry<Matcher, Operation>> entry) {
    Context ctx = entry.getKey();
    Matcher matcher = entry.getValue().getKey();
    return matcher.matches(ctx);
  }

  private static Entry<Context, Entry<Matcher, Operation>> withContext(
    Context context, Entry<Matcher, Operation> entry
  ) {
    return new AbstractMap.SimpleEntry<>(context, entry);
  }

  private Response takeFirst(
    Context context, GroupFacet groupFacet, DispatchedRepositories repositories
  ) {
    try {
      return getFirst(context, groupFacet.members(), repositories);
    } catch (Exception ex) {
      throw new IllegalArgumentException(ex);
    }
  }

  private Response firstWithResults(
    Context context,
    GroupFacet groupFacet,
    DispatchedRepositories repositories
  ) {
    try {
      LinkedHashMap<Repository, Response> collected =
        getAll(context, groupFacet.members(), repositories);
      for (Entry<Repository, Response> entry : collected.entrySet()) {
        Response response = entry.getValue();
        if (!response.getStatus().isSuccessful()) {
          continue;
        }
        ModuleReleases releases = readReleases(response);
        if (!releases.getResults().isEmpty()) {
          return response;
        }
      }
      return notFoundResponse(context);
    } catch (Exception ex) {
      throw new IllegalArgumentException(ex);
    }
  }

  @Override
  protected Response notFoundResponse(Context context) {
    return HttpResponses.ok(new StringPayload(NOT_FOUND_JSON, "application/json"));
  }

  private interface Operation {
    default Response perform(Context context, DispatchedRepositories repositories) {
      GroupFacet groupFacet = context.getRepository().facet(GroupFacet.class);
      return doPerform(context, groupFacet, repositories);
    }

    Response doPerform(Context context,
                       GroupFacet groupFacet,
                       DispatchedRepositories repositories);
  }
}
