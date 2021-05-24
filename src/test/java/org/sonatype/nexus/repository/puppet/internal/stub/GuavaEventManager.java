package org.sonatype.nexus.repository.puppet.internal.stub;

import com.google.common.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.nexus.common.event.EventManager;

/**
 * @author <a href="mailto:krzysztof.suszynski@wavesoftware.pl">Krzysztof Suszynski</a>
 * @since 0.1.0
 */
final class GuavaEventManager implements EventManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(GuavaEventManager.class);

  private final EventBus eventBus = new EventBus();

  @Override
  public void register(Object handler) {
    eventBus.register(handler);
  }

  @Override
  public void unregister(Object handler) {
    eventBus.unregister(handler);
  }

  @Override
  public void post(Object event) {
    LOGGER.debug("Post: {}", event);
    eventBus.post(event);
  }

  @Override
  public boolean isCalmPeriod() {
    return false;
  }

  @Override
  public boolean isAffinityEnabled() {
    return false;
  }
}
