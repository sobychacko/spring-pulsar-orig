package org.springframework.pulsar.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.pulsar.client.api.MessageRouter;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.ProducerBuilder;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.CollectionUtils;

public class DefaultPulsarProducerFactory<T> implements PulsarProducerFactory<T>, DisposableBean {

	private final Map<String, Object> producerConfig = new HashMap<>();

	private Producer<T> producer;

	private final PulsarClient pulsarClient;

	public DefaultPulsarProducerFactory(PulsarClient pulsarClient, Map<String, Object> config) {
		this.pulsarClient = pulsarClient;
		if (!CollectionUtils.isEmpty(config)) {
			this.producerConfig.putAll(config);
		}
	}

	@Override
	public Producer<T> createProducer(Schema<T> schema) throws PulsarClientException {

		final ProducerBuilder<T> producerBuilder = this.pulsarClient.newProducer(schema);

		if (!CollectionUtils.isEmpty(this.producerConfig)) {
			producerBuilder.loadConf(this.producerConfig);
		}
		this.producer = producerBuilder.create();
		return producer;
	}

	@Override
	public Producer<T> createProducer(Schema<T> schema, MessageRouter messageRouter) throws PulsarClientException {

		final ProducerBuilder<T> producerBuilder = this.pulsarClient.newProducer(schema);

		if (!CollectionUtils.isEmpty(this.producerConfig)) {
			producerBuilder.loadConf(this.producerConfig);
		}
		producerBuilder.messageRouter(messageRouter);
		this.producer = producerBuilder.create();
		return producer;
	}

	@Override
	public Map<String, Object> getProducerConfig() {
		return producerConfig;
	}

	@Override
	public void destroy() throws Exception {
		this.producer.close();
	}
}
