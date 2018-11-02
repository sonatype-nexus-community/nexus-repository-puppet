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
package org.sonatype.nexus.repository.puppet.internal.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonatype.goodies.testsupport.TestSupport;
import org.sonatype.nexus.blobstore.api.Blob;
import org.sonatype.nexus.common.collect.NestedAttributesMap;
import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.storage.Asset;
import org.sonatype.nexus.repository.storage.AssetBlob;
import org.sonatype.nexus.repository.storage.Bucket;
import org.sonatype.nexus.repository.storage.Component;
import org.sonatype.nexus.repository.storage.StorageTx;
import org.sonatype.nexus.repository.view.Content;
import org.sonatype.nexus.repository.view.Payload;

import com.google.common.base.Supplier;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class PuppetDataAccessTest
    extends TestSupport
{
  @Mock
  private StorageTx tx;

  @Mock
  private Repository repository;

  @Mock
  private Component component;

  @Mock
  private Asset asset;

  @Mock
  private Bucket bucket;

  @Mock
  private Supplier<InputStream> sis;

  @Mock
  private Payload payload;

  @Mock
  private AssetBlob assetBlob;

  @Mock
  private Blob blob;

  private NestedAttributesMap nestedAttributesMap;

  private PuppetDataAccess underTest;

  @Before
  public void setUp() throws Exception {
    underTest = new PuppetDataAccess();
  }

  @Test
  public void findComponentReturnsComponent() throws Exception {
    List<Component> list = new ArrayList<>();
    list.add(component);
    when(tx.findComponents(any(), any())).thenReturn(list);
    Component result = underTest.findComponent(tx, repository, "test", "1.0.0");

    assertThat(result, is(notNullValue()));
  }

  @Test
  public void findComponentReturnsNull() throws Exception {
    List<Component> list = new ArrayList<>();
    when(tx.findComponents(any(), any())).thenReturn(list);
    Component result = underTest.findComponent(tx, repository, "test", "1.0.0");

    assertThat(result, is(nullValue()));
  }

  @Test
  public void findAssetReturnsAsset() throws Exception {
    when(tx.findAssetWithProperty("name", "test", bucket)).thenReturn(asset);

    Asset result = underTest.findAsset(tx, bucket, "test");

    assertThat(result, is(notNullValue()));
  }

  @Test
  public void saveAssetReturnsContent() throws Exception {
    InputStream is = getClass().getResourceAsStream("puppetlabs-stdlib-5.1.0.tar.gz");
    when(sis.get()).thenReturn(is);
    Map<String, Object> map = new HashMap<>();
    nestedAttributesMap = new NestedAttributesMap("content", map);
    when(asset.name()).thenReturn("name");
    when(tx.setBlob(asset, "name", sis, PuppetDataAccess.HASH_ALGORITHMS, null, null, false )).thenReturn(assetBlob);
    when(assetBlob.getBlob()).thenReturn(blob);
    when(asset.attributes()).thenReturn(nestedAttributesMap);
    Content result = underTest.saveAsset(tx, asset, sis, payload);

    assertThat(result, is(notNullValue()));
  }
}
