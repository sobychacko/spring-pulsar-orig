/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

/**
 * @author Soby Chacko
 */
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
