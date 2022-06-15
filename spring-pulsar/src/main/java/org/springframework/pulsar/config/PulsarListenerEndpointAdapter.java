package org.springframework.pulsar.config;

import java.util.Collection;
import java.util.Collections;

import org.springframework.pulsar.support.MessageConverter;
import org.springframework.pulsar.listener.PulsarMessageListenerContainer;


public class PulsarListenerEndpointAdapter implements PulsarListenerEndpoint {
	@Override
	public String getSubscriptionName() {
		return null;
	}

	@Override
	public Collection<String> getTopics() {
		return Collections.emptyList();
	}

	@Override
	public Boolean getAutoStartup() {
		return null;
	}

	@Override
	public void setupListenerContainer(PulsarMessageListenerContainer listenerContainer, MessageConverter messageConverter) {

	}

	@Override
	public boolean isBatchListener() {
		return false;
	}
}
