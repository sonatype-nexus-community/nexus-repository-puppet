/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2018-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.puppet.internal.hosted;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.sonatype.nexus.common.io.InputStreamSupplier;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.puppet.internal.AssetKind;
import org.sonatype.nexus.repository.puppet.internal.PuppetAssetAttributePopulator;
import org.sonatype.nexus.repository.puppet.internal.metadata.ModuleReleaseResultBuilder;
import org.sonatype.nexus.repository.puppet.internal.metadata.ModuleReleases;
import org.sonatype.nexus.repository.puppet.internal.metadata.ModuleReleasesBuilder;
import org.sonatype.nexus.repository.puppet.internal.metadata.ModuleReleasesResult;
import org.sonatype.nexus.repository.puppet.internal.metadata.PuppetAttributes;
import org.sonatype.nexus.repository.puppet.internal.util.PuppetAttributeParser;
import org.sonatype.nexus.repository.puppet.internal.util.PuppetDataAccess;
import org.sonatype.nexus.repository.search.ComponentSearchResult;
import org.sonatype.nexus.repository.search.SearchRequest;
import org.sonatype.nexus.repository.search.SearchResponse;
import org.sonatype.nexus.repository.search.SearchService;
import org.sonatype.nexus.repository.search.SortDirection;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.AssetManager;
import org.sonatype.nexus.repository.storage.Bucket;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.ComponentEntityAdapter;
import org.sonatype.nexus.repository.storage.MetadataNodeEntityAdapter;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.transaction.TransactionalStoreBlob;
import org.sonatype.nexus.repository.transaction.TransactionalTouchBlob;
import org.sonatype.nexus.repository.transaction.TransactionalTouchMetadata;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.ContentTypes;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Parameters;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.payloads.BytesPayload;
import org.sonatype.nexus.repository.view.payloads.TempBlob;
import org.sonatype.nexus.transaction.UnitOfWork;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.puppet.internal.AssetKind.MODULE_DOWNLOAD;
import static org.sonatype.nexus.repository.puppet.internal.util.PuppetDataAccess.HASH_ALGORITHMS;
import static org.sonatype.nexus.repository.storage.AssetEntityAdapter.P_ASSET_KIND;

@Named
public class PuppetHostedFacetImpl
  extends FacetSupport
  implements PuppetHostedFacet {

  private final PuppetDataAccess puppetDataAccess;

  private final PuppetAssetAttributePopulator puppetAssetAttributePopulator;

  private final PuppetAttributeParser puppetAttributeParser;

  private final ModuleReleaseResultBuilder builder;

  private final ModuleReleasesBuilder moduleReleasesBuilder;

  private final ObjectMapper objectMapper;

  private final SearchService searchService;

  private final List<SortBuilder> sorting;

  @Inject
  public PuppetHostedFacetImpl(final PuppetDataAccess dataAccess,
                               final PuppetAttributeParser puppetAttributeParser,
                               final PuppetAssetAttributePopulator puppetAssetAttributePopulator,
                               final ModuleReleaseResultBuilder builder,
                               final ModuleReleasesBuilder moduleReleasesBuilder,
                               final ObjectMapper objectMapper,
                               final SearchService searchService) {

    this.puppetDataAccess = checkNotNull(dataAccess);
    this.puppetAttributeParser = checkNotNull(puppetAttributeParser);
    this.puppetAssetAttributePopulator = checkNotNull(puppetAssetAttributePopulator);
    this.builder = checkNotNull(builder);
    this.moduleReleasesBuilder = checkNotNull(moduleReleasesBuilder);
    this.objectMapper = checkNotNull(objectMapper);
    this.searchService = checkNotNull(searchService);
    this.sorting = new ArrayList<>();
    this.sorting.add(SortBuilders.fieldSort(MetadataNodeEntityAdapter.P_NAME).order(SortOrder.ASC));
    this.sorting.add(SortBuilders.fieldSort(ComponentEntityAdapter.P_VERSION).order(SortOrder.ASC));
  }

  @Nullable
  @TransactionalTouchBlob
  @Override
  public Content get(final String path) {
    checkNotNull(path);
    StorageTx tx = UnitOfWork.currentTx();

    Asset asset = puppetDataAccess.findAsset(tx, tx.findBucket(getRepository()), path);
    if (asset == null) {
      return null;
    }
    if (asset.markAsDownloaded(AssetManager.DEFAULT_LAST_DOWNLOADED_INTERVAL)) {
      tx.saveAsset(asset);
    }

    return puppetDataAccess.toContent(asset, tx.requireBlob(asset.requireBlobRef()));
  }

  @Override
  public void upload(final String path, final Payload payload, final AssetKind assetKind) throws IOException {
    checkNotNull(path);
    checkNotNull(payload);

    if (assetKind != MODULE_DOWNLOAD) {
      throw new IllegalArgumentException("Unsupported AssetKind");
    }
    try (TempBlob tempBlob = facet(StorageFacet.class).createTempBlob(payload, HASH_ALGORITHMS)) {
      storeModule(path, tempBlob, payload);
    }
  }

  @Override
  @TransactionalTouchMetadata
  public Content searchByName(final Parameters parameters, final Context context) {
    StorageTx tx = UnitOfWork.currentTx();

    ModuleReleases releases = getModuleReleasesFromSearchResponse(parameters, context, tx);

    ObjectMapper om = new ObjectMapper();

    try {
      String results = om.writeValueAsString(releases);

      return new Content(new BytesPayload(
        results.getBytes(StandardCharsets.UTF_8),
        ContentTypes.APPLICATION_JSON
      ));
    } catch (JsonProcessingException e) {
      throw new SearchByNameFailed(e);
    }
  }

  private ModuleReleases getModuleReleasesFromSearchResponse(final Parameters parameters,
                                                             final Context context,
                                                             final StorageTx tx) {
    long totalResults;

    SearchRequest.Builder srb = puppetDataAccess.buildNameSearchRequest(
      context.getRepository(), parameters.get("module")
    );

    // Compute pagination information
    totalResults = searchService.count(srb.build());

    Pagination pagination = computePagination(parameters);

    ModuleReleases releases = moduleReleasesBuilder.parse(totalResults, pagination.limit, pagination.offset, context);

    SearchResponse searchResponse = searchService.search(
      srb.offset(pagination.offset)
        .limit(pagination.limit)
        .sortField(MetadataNodeEntityAdapter.P_NAME)
        .sortDirection(SortDirection.ASC)
        .build()
    );

    for (ComponentSearchResult hit : searchResponse.getSearchResults()) {

      //searchResults.results = searchResponse.hits.hits?.collect { hit ->
      //    new V1SearchResult(
      //        name: "${host}/${hit.source[MetadataNodeEntityAdapter.P_NAME]}:${hit.source[ComponentEntityAdapter.P_VERSION]}"
      //  )
      //}

      // String path = hit.field("name").toString();
      String name = hit.getName();
      String version = hit.getVersion();
      String path = String.format("/v3/files/%s-%s.tar.gz", name, version);
      Asset asset = puppetDataAccess.findAsset(tx, tx.findBucket(getRepository()), path);
      ModuleReleasesResult result = builder.parse(Objects.requireNonNull(asset));
      releases.addResult(result);
    }
    return releases;
  }

  @Override
  @TransactionalTouchMetadata
  public Content moduleByNameAndVersion(final String user,
                                        final String module,
                                        final String version) {
    StorageTx tx = UnitOfWork.currentTx();

    String assetName = user + "-" + module;

    Component component = puppetDataAccess.findComponent(tx, getRepository(), assetName, version);

    if (component == null) {
      return null;
    }
    Asset asset = puppetDataAccess.findAssetByComponent(
      tx, tx.findBucket(getRepository()), component
    );

    ModuleReleasesResult result = builder.parse(Objects.requireNonNull(asset));

    try {
      String results = objectMapper.writeValueAsString(result);

      return new Content(new BytesPayload(
        results.getBytes(StandardCharsets.UTF_8),
        ContentTypes.APPLICATION_JSON
      ));
    } catch (JsonProcessingException e) {
      throw new SearchByNameFailed(e);
    }
  }

  @TransactionalStoreBlob
  protected Content storeModule(final String path,
                                final InputStreamSupplier moduleContent,
                                final Payload payload) throws IOException {
    StorageTx tx = UnitOfWork.currentTx();
    Bucket bucket = tx.findBucket(getRepository());

    Asset asset = createModuleAsset(path, tx, bucket, moduleContent.get());

    return puppetDataAccess.saveAsset(tx, asset, moduleContent, payload);
  }

  private Asset createModuleAsset(final String path,
                                  final StorageTx tx,
                                  final Bucket bucket,
                                  final InputStream inputStream) throws IOException {
    PuppetAttributes module;
    module = puppetAttributeParser.getAttributesFromInputStream(inputStream);

    return findOrCreateAssetAndComponent(path, tx, bucket, module);
  }

  private Asset findOrCreateAssetAndComponent(final String path,
                                              final StorageTx tx,
                                              final Bucket bucket,
                                              final PuppetAttributes module) {
    Asset asset = puppetDataAccess.findAsset(tx, bucket, path);
    if (asset == null) {
      Component component = findOrCreateComponent(tx, bucket, module);
      asset = tx.createAsset(bucket, component);
      asset.name(path);
      asset.formatAttributes().set(P_ASSET_KIND, MODULE_DOWNLOAD.name());
    }

    puppetAssetAttributePopulator.populate(asset.formatAttributes(), module);

    return asset;
  }

  private Component findOrCreateComponent(final StorageTx tx,
                                          final Bucket bucket,
                                          final PuppetAttributes module) {
    Component component = puppetDataAccess.findComponent(tx, getRepository(), module.getName(), module.getVersion());
    if (component == null) {
      component = tx.createComponent(bucket, getRepository().getFormat())
        .name(module.getName())
        .version(module.getVersion());
      tx.saveComponent(component);
    }
    return component;
  }

  private static Pagination computePagination(final Parameters parameters) {
    final Map<String, String> pm = toMap(parameters);
    final int DEFAULT_LIMIT = 20;
    final int DEFAULT_OFFSET = 0;
    final int limit = Integer.parseInt(pm.getOrDefault("limit", DEFAULT_LIMIT + ""));
    final int offset = Integer.parseInt(pm.getOrDefault("offset", DEFAULT_OFFSET + ""));
    return new Pagination(limit, offset);
  }

  private static Map<String, String> toMap(Parameters parameters) {
    Map<String, String> map = new LinkedHashMap<>(parameters.size());
    for (Map.Entry<String, String> entry : parameters.entries()) {
      map.put(entry.getKey(), entry.getValue());
    }
    return map;
  }

  private static final class Pagination {
    private final int offset;
    private final int limit;

    private Pagination(final int offset, final int limit) {
      this.offset = offset;
      this.limit = limit;
    }
  }
}
