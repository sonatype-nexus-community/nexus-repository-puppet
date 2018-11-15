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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

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
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Bucket;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.storage.TempBlob;
import org.sonatype.nexus.repository.transaction.TransactionalStoreBlob;
import org.sonatype.nexus.repository.transaction.TransactionalTouchBlob;
import org.sonatype.nexus.repository.transaction.TransactionalTouchMetadata;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.ContentTypes;
import org.sonatype.nexus.repository.view.Parameters;
import org.sonatype.nexus.repository.view.Payload;
import org.sonatype.nexus.repository.view.payloads.BytesPayload;
import org.sonatype.nexus.transaction.UnitOfWork;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.puppet.internal.AssetKind.MODULE_DOWNLOAD;
import static org.sonatype.nexus.repository.puppet.internal.util.PuppetDataAccess.HASH_ALGORITHMS;
import static org.sonatype.nexus.repository.storage.AssetEntityAdapter.P_ASSET_KIND;

@Named
public class PuppetHostedFacetImpl
    extends FacetSupport
  implements PuppetHostedFacet
{

  private final PuppetDataAccess dataAccess;

  private final PuppetAssetAttributePopulator puppetAssetAttributePopulator;

  private PuppetAttributeParser puppetAttributeParser;

  private final ModuleReleaseResultBuilder builder;

  private final ModuleReleasesBuilder moduleReleasesBuilder;

  @Override
  protected void doInit(final Configuration configuration) throws Exception {
    super.doInit(configuration);
  }

  @Inject
  public PuppetHostedFacetImpl(final PuppetDataAccess dataAccess,
                               final PuppetAttributeParser puppetAttributeParser,
                               final PuppetAssetAttributePopulator puppetAssetAttributePopulator,
                               final ModuleReleaseResultBuilder builder,
                               final ModuleReleasesBuilder moduleReleasesBuilder) {

    this.dataAccess = checkNotNull(dataAccess);
    this.puppetAttributeParser = checkNotNull(puppetAttributeParser);
    this.puppetAssetAttributePopulator = checkNotNull(puppetAssetAttributePopulator);
    this.builder = checkNotNull(builder);
    this.moduleReleasesBuilder = checkNotNull(moduleReleasesBuilder);
  }

  @Nullable
  @TransactionalTouchBlob
  @Override
  public Content get(final String path) {
    checkNotNull(path);
    StorageTx tx = UnitOfWork.currentTx();

    Asset asset = dataAccess.findAsset(tx, tx.findBucket(getRepository()), path);
    if (asset == null) {
      return null;
    }
    if (asset.markAsDownloaded()) {
      tx.saveAsset(asset);
    }

    return dataAccess.toContent(asset, tx.requireBlob(asset.requireBlobRef()));
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
  public Content searchByName(final Parameters parameters) {
    String module = parameters.get("module");
    try {
      module = URLDecoder.decode(module, "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    StorageTx tx = UnitOfWork.currentTx();

    ModuleReleases releases = moduleReleasesBuilder.parse();

    for (Asset asset : dataAccess.findAssets(tx, getRepository(), module)) {
      ModuleReleasesResult result = builder.parse(asset);
      releases.addResult(result);
    }

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

  @TransactionalStoreBlob
  protected Content storeModule(final String path,
                                final Supplier<InputStream> moduleContent,
                                final Payload payload) throws IOException
  {
    StorageTx tx = UnitOfWork.currentTx();
    Bucket bucket = tx.findBucket(getRepository());

    Asset asset = createModuleAsset(path, tx, bucket, moduleContent.get());

    return dataAccess.saveAsset(tx, asset, moduleContent, payload);
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
    Asset asset = dataAccess.findAsset(tx, bucket, path);
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
    Component component = dataAccess.findComponent(tx, getRepository(), module.getName(), module.getVersion());
    if (component == null) {
      component = tx.createComponent(bucket, getRepository().getFormat())
          .name(module.getName())
          .version(module.getVersion());
      tx.saveComponent(component);
    }
    return component;
  }
}
