package org.springframework.pulsar.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.MessageListener;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Schema;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PulsarContainer;
import org.testcontainers.utility.DockerImageName;

import org.springframework.pulsar.listener.DefaultPulsarMessageListenerContainer;
import org.springframework.pulsar.listener.PulsarContainerProperties;

public class DefaultConsumerTests {

	public static final String TEST_TOPIC = "test_topic";
	private static final DockerImageName PULSAR_IMAGE = DockerImageName.parse("apachepulsar/pulsar:2.10.0");

	@Test
	public void testDefaultConsumer() throws Exception {
		try (PulsarContainer pulsar = new PulsarContainer(PULSAR_IMAGE)) {
			pulsar.start();
			Map<String, Object> config = new HashMap<>();
			final HashSet<String> strings = new HashSet<String>();
			strings.add("foobar-012");
			config.put("topicNames", strings);
			config.put("subscriptionName", "foobar-sb-012");
			final PulsarClient pulsarClient = PulsarClient.builder()
					.serviceUrl(pulsar.getPulsarBrokerUrl())
					.build();
			final DefaultPulsarConsumerFactory<String> pulsarConsumerFactory = new DefaultPulsarConsumerFactory<>(pulsarClient, config);
			CountDownLatch latch = new CountDownLatch(1);
			PulsarContainerProperties pulsarContainerProperties = new PulsarContainerProperties();
			pulsarContainerProperties.setMessageListener(
					(MessageListener<?>) (consumer, msg) -> latch.countDown());
			pulsarContainerProperties.setSchema(Schema.STRING);
			DefaultPulsarMessageListenerContainer<String> container = new DefaultPulsarMessageListenerContainer<>(
					pulsarConsumerFactory, pulsarContainerProperties);
			container.start();
			Map<String, Object> prodConfig = new HashMap<>();
			prodConfig.put("topicName", "foobar-012");
			final DefaultPulsarProducerFactory<String> pulsarProducerFactory = new DefaultPulsarProducerFactory<>(pulsarClient, prodConfig);
			final PulsarTemplate<String> pulsarTemplate = new PulsarTemplate<>(pulsarProducerFactory);
			final CompletableFuture<MessageId> future = pulsarTemplate.sendAsync("hello john doe");
			latch.await(10, TimeUnit.SECONDS);
		}
	}

}