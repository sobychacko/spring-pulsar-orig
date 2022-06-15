package org.springframework.pulsar.core;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.MessageRouter;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;

import org.springframework.util.Assert;

public class PulsarTemplate<T> {

	//TODO: Cache the producer created from the "createProducer" call
	private Map<SchemaTopic, Producer<T>> producerCache = new ConcurrentHashMap<>();

	private final PulsarProducerFactory<T> pulsarProducerFactory;

	private String defaultTopicName;

	public PulsarTemplate(PulsarProducerFactory<T> pulsarProducerFactory) {
		this.pulsarProducerFactory = pulsarProducerFactory;
	}

	public MessageId send(T message) throws PulsarClientException {
		final Schema<T> schema = SchemaUtils.getSchema(message);
		final SchemaTopic schemaTopic = getSchemaTopic(schema, this.pulsarProducerFactory);
		Producer<T> producer = producerCache.get(schemaTopic);
		if (producer == null) {
			producer = this.pulsarProducerFactory.createProducer(schema);
			producerCache.put(schemaTopic, producer);
		}
		return producer.send(message);
	}

	private SchemaTopic getSchemaTopic(Schema<T> schema, PulsarProducerFactory<T> pulsarProducerFactory) {
		return new SchemaTopic(schema, (String) pulsarProducerFactory.getProducerConfig().get("topicName"));
	}

	public CompletableFuture<MessageId> sendAsync(T message) throws PulsarClientException {
		final Schema<T> schema = SchemaUtils.getSchema(message);
		final Producer<T> producer = this.pulsarProducerFactory.createProducer(schema);
		return producer.sendAsync(message);
	}

	public CompletableFuture<MessageId> sendAsync(T message, MessageRouter messageRouter) throws PulsarClientException {
		final Schema<T> schema = SchemaUtils.getSchema(message);
		final Producer<T> producer = this.pulsarProducerFactory.createProducer(schema, messageRouter);
		return producer.sendAsync(message);
	}

	public void setDefaultTopicName(String defaultTopicName) {
		this.defaultTopicName = defaultTopicName;
		this.pulsarProducerFactory.getProducerConfig().put("topicName", defaultTopicName);
	}

	private class SchemaTopic {

		final Schema<T> schema;
		final String topicName;

		public SchemaTopic(Schema<T> schema, String topicName) {
			this.schema = schema;
			this.topicName = topicName;
		}

		public Schema<T> getSchema() {
			return schema;
		}

		public String getTopicName() {
			return topicName;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			SchemaTopic that = (SchemaTopic) o;
			return Objects.equals(schema, that.schema) && Objects.equals(topicName, that.topicName);
		}

		@Override
		public int hashCode() {
			return Objects.hash(schema, topicName);
		}
	}
}
