package org.springframework.pulsar.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.MessageListener;
import org.apache.pulsar.client.api.MessageRouter;
import org.apache.pulsar.client.api.MessageRoutingMode;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.SubscriptionType;
import org.apache.pulsar.client.api.TopicMetadata;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PulsarContainer;
import org.testcontainers.utility.DockerImageName;

import org.springframework.pulsar.listener.DefaultPulsarMessageListenerContainer;
import org.springframework.pulsar.listener.PulsarContainerProperties;

public class FailoverConsumerTests {

	private static final DockerImageName PULSAR_IMAGE = DockerImageName.parse("apachepulsar/pulsar:2.10.0");

	@Test
	public void testDefaultConsumer() throws PulsarClientException {
		try (PulsarContainer pulsar = new PulsarContainer(PULSAR_IMAGE)) {
			pulsar.start();
			Map<String, Object> config = new HashMap<>();
			final HashSet<String> strings = new HashSet<>();
			strings.add("my-part-topic-1");
			config.put("topicNames", strings);
			config.put("subscriptionName", "my-part-subscription-1");
			Map<String, Object> clientConfig = new HashMap<>();
			clientConfig.put("serviceUrl", pulsar.getPulsarBrokerUrl());
			final PulsarClient pulsarClient = PulsarClient.builder()
					.serviceUrl(pulsar.getPulsarBrokerUrl())
					.build();
			final DefaultPulsarConsumerFactory<String> pulsarConsumerFactory = new DefaultPulsarConsumerFactory<>(pulsarClient, config);
			CountDownLatch latch = new CountDownLatch(1);
			PulsarContainerProperties pulsarContainerProperties = new PulsarContainerProperties();
			pulsarContainerProperties.setMessageListener(new MessageListener() {
				@Override
				public void received(Consumer consumer, Message msg) {
					latch.countDown();
				}
			});
			pulsarContainerProperties.setSubscriptionType(SubscriptionType.Failover);
			pulsarContainerProperties.setSchema(Schema.STRING);
			DefaultPulsarMessageListenerContainer<String> container = new DefaultPulsarMessageListenerContainer(
					pulsarConsumerFactory, pulsarContainerProperties);
			container.start();
			DefaultPulsarMessageListenerContainer<String> container1 = new DefaultPulsarMessageListenerContainer(
					pulsarConsumerFactory, pulsarContainerProperties);
			container1.start();
			DefaultPulsarMessageListenerContainer<String> container2 = new DefaultPulsarMessageListenerContainer(
					pulsarConsumerFactory, pulsarContainerProperties);
			container2.start();
			Map<String, Object> prodConfig = new HashMap<>();
			prodConfig.put("topicName", "my-part-topic-1");
			prodConfig.put("messageRoutingMode", MessageRoutingMode.CustomPartition);
			final DefaultPulsarProducerFactory<String> pulsarProducerFactory = new DefaultPulsarProducerFactory<>(pulsarClient, prodConfig);
			final PulsarTemplate<String> pulsarTemplate = new PulsarTemplate<>(pulsarProducerFactory);
			CompletableFuture<MessageId> future = pulsarTemplate.sendAsync("hello john doe", new FooRouter());
			future.thenAccept(m -> System.out.println("Got " + m));
			try {
				Thread.sleep(2000);
				final MessageId messageId = future.get();
				System.out.println();
			}
			catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			future = pulsarTemplate.sendAsync("hello alice doe", new BarRouter());
			future.thenAccept(m -> System.out.println("Got " + m));
			try {
				Thread.sleep(2000);
				final MessageId messageId = future.get();
				System.out.println();
			}
			catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			future = pulsarTemplate.sendAsync("hello buzz doe", new BuzzRouter());
			future.thenAccept(m -> System.out.println("Got " + m));
			try {
				Thread.sleep(2000);
				final MessageId messageId = future.get();
				System.out.println();
			}
			catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
	}

	static class FooRouter implements MessageRouter {

		@Override
		public int choosePartition(Message<?> msg, TopicMetadata metadata) {
			return 0;
		}
	}

	static class BarRouter implements MessageRouter {

		@Override
		public int choosePartition(Message<?> msg, TopicMetadata metadata) {
			return 1;
		}
	}

	static class BuzzRouter implements MessageRouter {

		@Override
		public int choosePartition(Message<?> msg, TopicMetadata metadata) {
			return 2;
		}
	}
}
