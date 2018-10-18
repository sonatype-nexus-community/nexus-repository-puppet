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
package org.sonatype.nexus.repository.puppet.internal.proxy;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.repository.cache.CacheInfo;
import org.sonatype.nexus.repository.config.Configuration;
import org.sonatype.nexus.repository.proxy.ProxyFacet;
import org.sonatype.nexus.repository.proxy.ProxyFacetSupport;
import org.sonatype.nexus.repository.puppet.internal.AssetKind;
import org.sonatype.nexus.repository.puppet.internal.util.PuppetDataAccess;
import org.sonatype.nexus.repository.puppet.internal.util.PuppetPathUtils;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.Bucket;
import org.sonatype.nexus.repository.storage.StorageFacet;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.storage.TempBlob;
import org.sonatype.nexus.repository.transaction.TransactionalStoreBlob;
import org.sonatype.nexus.repository.transaction.TransactionalTouchBlob;
import org.sonatype.nexus.repository.transaction.TransactionalTouchMetadata;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.ContentTypes;
import org.sonatype.nexus.repository.view.Context;
import org.sonatype.nexus.repository.view.Parameters;
import org.sonatype.nexus.repository.view.matchers.token.TokenMatcher;
import org.sonatype.nexus.transaction.UnitOfWork;

import com.google.common.base.Joiner;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.repository.storage.AssetEntityAdapter.P_ASSET_KIND;

/**
 * Puppet {@link ProxyFacet} implementation.
 *
 * @since 0.0.1
 */
@Named
public class PuppetProxyFacetImpl
    extends ProxyFacetSupport
{
  private PuppetPathUtils puppetPathUtils;

  private PuppetDataAccess puppetDataAccess;

  @Inject
  public PuppetProxyFacetImpl(final PuppetPathUtils puppetPathUtils,
                              final PuppetDataAccess puppetDataAccess) {
    this.puppetPathUtils = checkNotNull(puppetPathUtils);
    this.puppetDataAccess = checkNotNull(puppetDataAccess);
  }

  // HACK: Workaround for known CGLIB issue, forces an Import-Package for org.sonatype.nexus.repository.config
  @Override
  protected void doValidate(final Configuration configuration) throws Exception {
    super.doValidate(configuration);
  }

  @Nullable
  @Override
  protected Content getCachedContent(final Context context) throws IOException {
    AssetKind assetKind = context.getAttributes().require(AssetKind.class);
    TokenMatcher.State matcherState = puppetPathUtils.matcherState(context);
    switch (assetKind) {
      case MODULE_RELEASES_BY_NAME:
        return null;
      case MODULE_RELEASE_BY_NAME_AND_VERSION:
        return getAsset(puppetPathUtils.buildModuleReleaseByNameAndVersionPath(matcherState));
      case MODULE_DOWNLOAD:
        return null;
      default:
        throw new IllegalStateException("Received an invalid AssetKind of type: " + assetKind.name());
    }
  }

  @TransactionalTouchBlob
  protected Content getAsset(final String assetPath) {
    StorageTx tx = UnitOfWork.currentTx();

    Asset asset = puppetDataAccess.findAsset(tx, tx.findBucket(getRepository()), assetPath);
    if (asset == null) {
      return null;
    }
    if (asset.markAsDownloaded()) {
      tx.saveAsset(asset);
    }
    return puppetDataAccess.toContent(asset, tx.requireBlob(asset.requireBlobRef()));
  }

  @Override
  protected Content store(final Context context, final Content content) throws IOException {
    AssetKind assetKind = context.getAttributes().require(AssetKind.class);
    TokenMatcher.State matcherState = puppetPathUtils.matcherState(context);
    switch (assetKind) {
      case MODULE_RELEASES_BY_NAME:
        return null;
      case MODULE_RELEASE_BY_NAME_AND_VERSION:
        return putMetadata(content,
            assetKind,
            puppetPathUtils.buildModuleReleaseByNameAndVersionPath(matcherState));
      case MODULE_DOWNLOAD:
        return null;
      default:
        throw new IllegalStateException("Received an invalid AssetKind of type: " + assetKind.name());
    }
  }

  private Content putMetadata(final Content content,
                              final AssetKind assetKind,
                              final String assetPath) throws IOException
  {
    StorageFacet storageFacet = facet(StorageFacet.class);

    try (TempBlob tempBlob = storageFacet.createTempBlob(content.openInputStream(), PuppetDataAccess.HASH_ALGORITHMS)) {
      return doPutMetadata(tempBlob, content, assetKind, assetPath);
    }
  }

  @TransactionalStoreBlob
  protected Content doPutMetadata(final TempBlob tempBlob,
                                final Content content,
                                final AssetKind assetKind,
                                final String assetPath) throws IOException
  {
    StorageTx tx = UnitOfWork.currentTx();
    Bucket bucket = tx.findBucket(getRepository());

    Asset asset = puppetDataAccess.findAsset(tx, bucket, assetPath);
    if (asset == null) {
      asset = tx.createAsset(bucket, getRepository().getFormat());
      asset.name(assetPath);
      asset.contentType(ContentTypes.APPLICATION_JSON);
      asset.formatAttributes().set(P_ASSET_KIND, assetKind.name());
    }

    return puppetDataAccess.saveAsset(tx, asset, tempBlob, content);
  }

  @Override
  protected void indicateVerified(final Context context, final Content content, final CacheInfo cacheInfo)
      throws IOException
  {
    setCacheInfo(content, cacheInfo);
  }

  @TransactionalTouchMetadata
  public void setCacheInfo(final Content content, final CacheInfo cacheInfo) throws IOException {
    StorageTx tx = UnitOfWork.currentTx();
    Asset asset = Content.findAsset(tx, tx.findBucket(getRepository()), content);
    if (asset == null) {
      log.debug(
          "Attempting to set cache info for non-existent Puppet asset {}", content.getAttributes().require(Asset.class)
      );
      return;
    }
    log.debug("Updating cacheInfo of {} to {}", asset, cacheInfo);
    CacheInfo.applyToAsset(asset, cacheInfo);
    tx.saveAsset(asset);
  }

  @Override
  protected String getUrl(@Nonnull final Context context) {
    String url = context.getRequest().getPath().substring(1);
    AssetKind assetKind = context.getAttributes().require(AssetKind.class);

    if (assetKind.equals(AssetKind.MODULE_RELEASES_BY_NAME)) {
      Parameters parameters = context.getRequest().getParameters();
      url += "?" + Joiner.on("&").withKeyValueSeparator("=").join(parameters);
    }

    return url;
  }
}
