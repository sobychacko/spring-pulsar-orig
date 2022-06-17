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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import io.micrometer.common.util.StringUtils;
import org.apache.pulsar.client.api.BatchReceivePolicy;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.ConsumerBuilder;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.SubscriptionType;

import org.springframework.pulsar.listener.PulsarContainerProperties;
import org.springframework.util.CollectionUtils;

/**
 * @author Soby Chacko
 */
public class DefaultPulsarConsumerFactory<T> implements PulsarConsumerFactory<T> {

	private final Map<String, Object> consumerConfig = new HashMap<>();

	private final List<Consumer<T>> consumers = new ArrayList<>();

	private PulsarClient pulsarClient;

	public DefaultPulsarConsumerFactory(PulsarClient pulsarClient, Map<String, Object> consumerConfig) {
		this.pulsarClient = pulsarClient;
		if (!CollectionUtils.isEmpty(consumerConfig)) {
			this.consumerConfig.putAll(consumerConfig);
		}
	}

	@Override
	public Consumer<T> createConsumer(Schema<T> schema, SubscriptionType subscriptionType) throws PulsarClientException {

		final ConsumerBuilder<T> consumerBuilder = this.pulsarClient.newConsumer(schema);
		consumerBuilder.subscriptionType(subscriptionType);

		if (!CollectionUtils.isEmpty(this.consumerConfig)) {
			consumerBuilder.loadConf(this.consumerConfig);
		}
		Consumer<T> consumer = consumerBuilder.subscribe();
		consumers.add(consumer);
		return consumer;
	}

	@Override
	public Consumer<T> createConsumer(Schema<T> schema, SubscriptionType subscriptionType, PulsarContainerProperties properties) throws PulsarClientException {

		final ConsumerBuilder<T> consumerBuilder = this.pulsarClient.newConsumer(schema);
		consumerBuilder.subscriptionType(subscriptionType);

		final String[] topics1 = properties.getTopics();
		assert topics1 != null;
		final HashSet<String> strings = new HashSet<>(Arrays.stream(topics1).toList());
		synchronized (this.consumerConfig) {
			if (!strings.isEmpty()) {
				this.consumerConfig.put("topicNames", strings);
			}
			if (StringUtils.isNotEmpty(properties.getSubscriptionName())) {
				this.consumerConfig.put("subscriptionName", properties.getSubscriptionName());
			}


			if (!CollectionUtils.isEmpty(this.consumerConfig)) {
				consumerBuilder.loadConf(this.consumerConfig);
			}
			Consumer<T> consumer = consumerBuilder.subscribe();
			consumers.add(consumer);
			return consumer;
		}
	}


	@Override
	public Consumer<T> createConsumer(Schema<T> schema, SubscriptionType subscriptionType, BatchReceivePolicy batchReceivePolicy) throws PulsarClientException {

		final ConsumerBuilder<T> consumerBuilder = this.pulsarClient.newConsumer(schema);
		consumerBuilder.subscriptionType(subscriptionType);
		if (!CollectionUtils.isEmpty(this.consumerConfig)) {
			consumerBuilder.loadConf(this.consumerConfig);
		}

		consumerBuilder.batchReceivePolicy(batchReceivePolicy);
		Consumer<T> consumer = consumerBuilder.subscribe();
		consumers.add(consumer);
		return consumer;
	}

	public Map<String, Object> getConsumerConfig() {
		return consumerConfig;
	}

}
