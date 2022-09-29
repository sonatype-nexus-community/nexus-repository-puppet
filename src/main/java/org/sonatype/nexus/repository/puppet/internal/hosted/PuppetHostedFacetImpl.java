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
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.sonatype.nexus.common.io.InputStreamSupplier;
import org.sonatype.nexus.repository.FacetSupport;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.puppet.internal.AssetKind;
import org.sonatype.nexus.repository.puppet.internal.PuppetAssetAttributePopulator;
import org.sonatype.nexus.repository.puppet.internal.metadata.ModuleReleaseResultBuilder;
import org.sonatype.nexus.repository.puppet.internal.metadata.ModuleReleases;
import org.sonatype.nexus.repository.puppet.internal.metadata.ModuleReleasesBuilder;
import org.sonatype.nexus.repository.puppet.internal.metadata.ModuleReleasesResult;
import org.sonatype.nexus.repository.puppet.internal.metadata.PuppetAttributes;
import org.sonatype.nexus.repository.puppet.internal.util.PuppetAttributeParser;
import org.sonatype.nexus.repository.puppet.internal.util.PuppetDataAccess;
import org.sonatype.nexus.repository.search.ElasticSearchService;
import org.sonatype.nexus.repository.storage.Asset;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.puppet.internal.AssetKind.MODULE_DOWNLOAD;
import static org.sonatype.nexus.repository.puppet.internal.util.PuppetDataAccess.HASH_ALGORITHMS;
import static org.sonatype.nexus.repository.storage.AssetEntityAdapter.P_ASSET_KIND;

@Named
public class PuppetHostedFacetImpl
    extends FacetSupport
  implements PuppetHostedFacet
{

  private final PuppetDataAccess puppetDataAccess;

  private final PuppetAssetAttributePopulator puppetAssetAttributePopulator;

  private PuppetAttributeParser puppetAttributeParser;

  private final ModuleReleaseResultBuilder builder;

  private final ModuleReleasesBuilder moduleReleasesBuilder;

  private final ObjectMapper objectMapper;

  private final ElasticSearchService searchService;

  private final List<SortBuilder> sorting;

  @Override
  protected void doInit(final Configuration configuration) throws Exception {
    super.doInit(configuration);
  }

  @Inject
  public PuppetHostedFacetImpl(final PuppetDataAccess dataAccess,
                               final PuppetAttributeParser puppetAttributeParser,
                               final PuppetAssetAttributePopulator puppetAssetAttributePopulator,
                               final ModuleReleaseResultBuilder builder,
                               final ModuleReleasesBuilder moduleReleasesBuilder,
                               final ObjectMapper objectMapper,
                               final ElasticSearchService searchService) {

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
    if (asset.markAsDownloaded()) {
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

    ObjectMapper objectMapper = new ObjectMapper();

    try {
      String results = objectMapper.writeValueAsString(releases);

      return new Content(new BytesPayload(results.getBytes(), ContentTypes.APPLICATION_JSON));
    }
    catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return null;
  }

  private ModuleReleases getModuleReleasesFromSearchResponse(final Parameters parameters,
                                                             final Context context,
                                                             final StorageTx tx)
  {
    long totalResults, resultsPerPage, resultsOffset;

    QueryBuilder queryBuilder = puppetDataAccess.buildNameQuery(
        context.getRepository(),
        parameters.get("module")
    );

    // Compute pagination information
    totalResults = searchService.countUnrestricted(queryBuilder);

    try {
      resultsPerPage = Long.parseLong(parameters.get("limit"));
    }
    catch (NumberFormatException e) {
      resultsPerPage = 20;
    }

    try {
      resultsOffset = Long.parseLong(parameters.get("offset"));
    }
    catch (NumberFormatException e){
      resultsOffset = 0;
    }

    ModuleReleases releases = moduleReleasesBuilder.parse(totalResults, resultsPerPage, resultsOffset, context);

    SearchResponse searchResponse = searchService.searchUnrestricted(queryBuilder, sorting, (int)resultsOffset, (int)resultsPerPage);

    for (SearchHit hit : searchResponse.getHits()) {

      //searchResults.results = searchResponse.hits.hits?.collect { hit ->
      //    new V1SearchResult(
      //        name: "${host}/${hit.source[MetadataNodeEntityAdapter.P_NAME]}:${hit.source[ComponentEntityAdapter.P_VERSION]}"
      //  )
      //}

      // String path = hit.field("name").toString();
      Map<String, Object> attributes = hit.sourceAsMap();
      String name = attributes.get("name").toString();
      String version = attributes.get("version").toString();
      String path = String.format("/v3/files/%s-%s.tar.gz", name, version);
      ModuleReleasesResult result = builder.parse(puppetDataAccess.findAsset(tx, tx.findBucket(getRepository()), path));
      releases.addResult(result);
    }
    return releases;
  }


  @Override
  @TransactionalTouchMetadata
  public Content moduleByNameAndVersion(final String user,
                                        final String module,
                                        final String version)
  {
    StorageTx tx = UnitOfWork.currentTx();

    String assetName = user + "-" + module;

    Component component = puppetDataAccess.findComponent(tx, getRepository(), assetName, version);

    if (component != null) {
      Asset asset = puppetDataAccess.findAssetByComponent(tx, tx.findBucket(getRepository()), component);

      ModuleReleasesResult result = builder.parse(asset);

      try {
        String results = objectMapper.writeValueAsString(result);

        return new Content(new BytesPayload(results.getBytes(), ContentTypes.APPLICATION_JSON));
      }
      catch (JsonProcessingException e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  @TransactionalStoreBlob
  protected Content storeModule(final String path,
                                final InputStreamSupplier moduleContent,
                                final Payload payload) throws IOException
  {
    StorageTx tx = UnitOfWork.currentTx();
    Bucket bucket = tx.findBucket(getRepository());

    Asset asset = createModuleAsset(path, tx, bucket, moduleContent.get());

    return puppetDataAccess.saveAsset(tx, asset, moduleContent, payload);
  }

  private Asset createModuleAsset(final String path,
                                  final StorageTx tx,
                                  final Bucket bucket,
                                  final InputStream inputStream) throws IOException
  {
    PuppetAttributes module;
    module = puppetAttributeParser.getAttributesFromInputStream(inputStream);

    return findOrCreateAssetAndComponent(path, tx, bucket, module);
  }

  private Asset findOrCreateAssetAndComponent(final String path,
                                              final StorageTx tx,
                                              final Bucket bucket,
                                              final PuppetAttributes module)
  {
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
}
