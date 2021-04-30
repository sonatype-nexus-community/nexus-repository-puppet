package org.sonatype.nexus.repository.puppet.internal.stub;

import javax.annotation.Nullable;
import javax.inject.Named;

import org.sonatype.nexus.httpclient.HttpClientManager;
import org.sonatype.nexus.httpclient.HttpClientPlan;
import org.sonatype.nexus.httpclient.config.HttpClientConfiguration;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

/**
 * @author <a href="mailto:krzysztof.suszynski@wavesoftware.pl">Krzysztof Suszynski</a>
 * @since 0.1.0
 */
//@Named
public final class HttpClientManagerStub implements HttpClientManager {
  @Override
  public HttpClientConfiguration getConfiguration() {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void setConfiguration(HttpClientConfiguration httpClientConfiguration) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public CloseableHttpClient create(HttpClientPlan.Customizer customizer) {
    return create();
  }

  @Override
  public CloseableHttpClient create() {
    return HttpClients.createDefault();
  }

  @Override
  public HttpClientBuilder prepare(@Nullable HttpClientPlan.Customizer customizer) {
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public HttpClientConfiguration newConfiguration() {
    throw new UnsupportedOperationException("Not yet implemented");
  }
}
