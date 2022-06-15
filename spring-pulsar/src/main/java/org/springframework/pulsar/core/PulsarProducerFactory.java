package org.springframework.pulsar.core;

import java.util.Map;

import org.apache.pulsar.client.api.MessageRouter;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;

public interface PulsarProducerFactory<T> {

	Producer<T> createProducer(Schema<T> schema) throws PulsarClientException;

	Producer<T> createProducer(Schema<T> schema, MessageRouter messageRouter) throws PulsarClientException;

	Map<String, Object> getProducerConfig();
}
