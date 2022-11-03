package org.sonatype.nexus.repository.puppet.internal.stub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.blobstore.api.metrics.BlobStoreMetricsEntity;
import org.sonatype.nexus.blobstore.api.metrics.BlobStoreMetricsStore;

final class BlobStoreMetricsStoreStub implements BlobStoreMetricsStore {
  private static final Logger LOGGER = LoggerFactory.getLogger(BlobStoreMetricsStoreStub.class);
  @Override
  public void updateMetrics(BlobStoreMetricsEntity blobStoreMetricsEntity) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public BlobStoreMetricsEntity get(String blobStoreName) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void remove(String blobStoreName) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void clearOperationMetrics(String blobStoreName) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void initializeMetrics(String blobStoreName) {
    LOGGER.info("Initializing metrics for blob store {}", blobStoreName);
  }
}
