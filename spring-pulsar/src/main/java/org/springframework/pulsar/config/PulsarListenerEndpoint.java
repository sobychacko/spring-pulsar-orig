package org.springframework.pulsar.config;

import java.util.Collection;

import org.springframework.lang.Nullable;
import org.springframework.pulsar.support.MessageConverter;
import org.springframework.pulsar.listener.PulsarMessageListenerContainer;

public interface PulsarListenerEndpoint {

	@Nullable
	String getSubscriptionName();

	Collection<String> getTopics();

	@Nullable
	Boolean getAutoStartup();

	void setupListenerContainer(PulsarMessageListenerContainer listenerContainer,
								@Nullable MessageConverter messageConverter);

	boolean isBatchListener();
}
