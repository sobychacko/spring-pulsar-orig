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

import java.util.Map;

import org.apache.pulsar.client.api.BatchReceivePolicy;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.SubscriptionType;

import org.springframework.pulsar.listener.PulsarContainerProperties;

/**
 * @author Soby Chacko
 */
public interface PulsarConsumerFactory<T> {

	default Consumer<T> createConsumer(Schema<T> schema) throws PulsarClientException {
		return createConsumer(schema, SubscriptionType.Exclusive);
	}

	Consumer<T> createConsumer(Schema<T> schema, SubscriptionType subscriptionType) throws PulsarClientException;

	Consumer<T> createConsumer(Schema<T> schema, SubscriptionType subscriptionType, PulsarContainerProperties properties) throws PulsarClientException;

	Consumer<T> createConsumer(Schema<T> schema, SubscriptionType subscriptionType, BatchReceivePolicy batchReceivePolicy) throws PulsarClientException;

	Map<String, Object> getConsumerConfig();
}
