package org.springframework.pulsar.core;

import java.util.Map;

import org.apache.pulsar.client.api.BatchReceivePolicy;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.SubscriptionType;

import org.springframework.pulsar.listener.PulsarContainerProperties;

public interface PulsarConsumerFactory<T> {

	default Consumer<T> createConsumer(Schema<T> schema) throws PulsarClientException {
		return createConsumer(schema, SubscriptionType.Exclusive);
	}

	Consumer<T> createConsumer(Schema<T> schema, SubscriptionType subscriptionType) throws PulsarClientException;

	Consumer<T> createConsumer(Schema<T> schema, SubscriptionType subscriptionType, PulsarContainerProperties properties) throws PulsarClientException;

	Consumer<T> createConsumer(Schema<T> schema, SubscriptionType subscriptionType, BatchReceivePolicy batchReceivePolicy) throws PulsarClientException;

	Map<String, Object> getConsumerConfig();
}
