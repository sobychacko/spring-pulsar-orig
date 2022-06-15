package org.springframework.pulsar.config;

import org.springframework.pulsar.config.PulsarListenerEndpoint;
import org.springframework.pulsar.listener.PulsarMessageListenerContainer;

public interface PulsarListenerContainerFactory<C extends PulsarMessageListenerContainer> {

	C createListenerContainer(PulsarListenerEndpoint endpoint);

	C createContainer(String... topics);

}
