package org.springframework.pulsar.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.springframework.pulsar.config.AbstractPulsarListenerContainerFactory;
import org.springframework.pulsar.config.PulsarListenerEndpoint;
import org.springframework.pulsar.config.PulsarListenerEndpointAdapter;
import org.springframework.pulsar.listener.DefaultPulsarMessageListenerContainer;
import org.springframework.pulsar.listener.PulsarContainerProperties;

public class PulsarListenerContainerFactoryImpl<C, T> extends AbstractPulsarListenerContainerFactory<DefaultPulsarMessageListenerContainer<T>, T> {

	@Override
	protected DefaultPulsarMessageListenerContainer<T> createContainerInstance(PulsarListenerEndpoint endpoint) {

		Collection<String> topics = endpoint.getTopics();


		if (!topics.isEmpty()) {
			final String[] topics1 = topics.toArray(new String[0]);
			Map<String, Object> config = new HashMap<>();
			final HashSet<String> strings = new HashSet<>(topics);
			config.put("topicNames", strings);
			config.put("subscriptionName", endpoint.getSubscriptionName());


			PulsarContainerProperties properties = new PulsarContainerProperties(topics1);
			properties.setSubscriptionName(endpoint.getSubscriptionName());
			properties.setBatchReceive(endpoint.isBatchListener()); //TODO - ONLY FOR TESTING
			final Map<String, Object> consumerConfig = getPulsarConsumerFactory().getConsumerConfig();
			consumerConfig.putAll(config);

			return new DefaultPulsarMessageListenerContainer<T>(getPulsarConsumerFactory(), properties);
		}

		return null;
	}

	@Override
	protected void initializeContainer(DefaultPulsarMessageListenerContainer<T> instance,
									   PulsarListenerEndpoint endpoint) {

		super.initializeContainer(instance, endpoint);
	}

	@Override
	public DefaultPulsarMessageListenerContainer<T> createContainer(String... topics) {
		PulsarListenerEndpoint endpoint = new PulsarListenerEndpointAdapter() {

			@Override
			public Collection<String> getTopics() {
				return Arrays.asList(topics);
			}

		};
		DefaultPulsarMessageListenerContainer<T> container = createContainerInstance(endpoint);
		initializeContainer(container, endpoint);
		//customizeContainer(container);
		return container;
	}
}
