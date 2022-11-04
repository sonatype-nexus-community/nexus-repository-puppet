package org.sonatype.nexus.repository.puppet.internal.hosted;

class SearchByNameFailed extends RuntimeException {
  SearchByNameFailed(final Exception cause) {
    super(cause);
  }
}
