package org.springframework.pulsar.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.MessageListener;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PulsarContainer;
import org.testcontainers.utility.DockerImageName;

import org.springframework.pulsar.listener.DefaultPulsarMessageListenerContainer;
import org.springframework.pulsar.listener.PulsarContainerProperties;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PulsarTemplateTests {

	public static final String TEST_TOPIC = "test_topic";
	private static final DockerImageName PULSAR_IMAGE = DockerImageName.parse("apachepulsar/pulsar:2.10.0");

	@Test
	public void testUsage() throws Exception {
		try (PulsarContainer pulsar = new PulsarContainer(PULSAR_IMAGE)) {
			pulsar.start();
			testPulsarFunctionality(pulsar.getPulsarBrokerUrl());
		}
	}

	@Test
	public void testDefaultConsumer() throws PulsarClientException {
		try (PulsarContainer pulsar = new PulsarContainer(PULSAR_IMAGE)) {
			pulsar.start();
			Map<String, Object> config = new HashMap<>();
			final HashSet<String> strings = new HashSet<String>();
			strings.add("foobar-012");
			config.put("topicNames", strings);
			config.put("subscriptionName", "foobar-sb-012");
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
			pulsarContainerProperties.setSchema(Schema.STRING);
			DefaultPulsarMessageListenerContainer<String> container = new DefaultPulsarMessageListenerContainer(
					pulsarConsumerFactory,pulsarContainerProperties);
			container.start();
			Map<String, Object> prodConfig = new HashMap<>();
			prodConfig.put("topicName", "foobar-012");
			final DefaultPulsarProducerFactory<String> pulsarProducerFactory = new DefaultPulsarProducerFactory<>(pulsarClient, prodConfig);
			final PulsarTemplate<String> pulsarTemplate = new PulsarTemplate<>(pulsarProducerFactory);
			final CompletableFuture<MessageId> future = pulsarTemplate.sendAsync("hello john doe");
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

	@Test
	public void testSendAsync() throws Exception {
		try (PulsarContainer pulsar = new PulsarContainer(PULSAR_IMAGE)) {
			pulsar.start();
			Map<String, Object> config = new HashMap<>();
			config.put("topicName", "foo-bar-123");
			Map<String, Object> clientConfig = new HashMap<>();
			clientConfig.put("serviceUrl", pulsar.getPulsarBrokerUrl());
			try (
					PulsarClient client = PulsarClient.builder()
							.loadConf(clientConfig)
							.build();
					Consumer<String> consumer = client.newConsumer(Schema.STRING)
							.topic("foo-bar-123")
							.subscriptionName("xyz-test-subs-123")
							.subscribe()
			) {
				final DefaultPulsarProducerFactory<String> pulsarProducerFactory = new DefaultPulsarProducerFactory<>(client, config);
				final PulsarTemplate<String> pulsarTemplate = new PulsarTemplate<>(pulsarProducerFactory);
				final CompletableFuture<MessageId> future = pulsarTemplate.sendAsync("hello john doe");
				future.thenAccept(m -> System.out.println("Got " + m));
				try {
					Thread.sleep(2000);
					final MessageId messageId = future.get();
					System.out.println();
				}
				catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
				CompletableFuture<Message<String>> future0 = consumer.receiveAsync();
				Message<String> message = future0.get(5, TimeUnit.SECONDS);
				assertThat(new String(message.getData()))
						.isEqualTo("hello john doe");
			}
		}
	}

	@Test
	public void testSendSync() throws Exception {
		try (PulsarContainer pulsar = new PulsarContainer(PULSAR_IMAGE)) {
			pulsar.start();
			Map<String, Object> config = new HashMap<>();
			config.put("topicName", "foo-bar-123");
			Map<String, Object> clientConfig = new HashMap<>();
			clientConfig.put("serviceUrl", pulsar.getPulsarBrokerUrl());
			try (
					PulsarClient client = PulsarClient.builder()
							.loadConf(clientConfig)
							.build();
					Consumer<String> consumer = client.newConsumer(Schema.STRING)
							.topic("foo-bar-123")
							.subscriptionName("xyz-test-subs-123")
							.subscribe();
			) {
				final DefaultPulsarProducerFactory<String> pulsarProducerFactory = new DefaultPulsarProducerFactory<>(client, config);
				final PulsarTemplate<String> pulsarTemplate = new PulsarTemplate<>(pulsarProducerFactory);
				final MessageId messageId = pulsarTemplate.send("hello john doe");
				CompletableFuture<Message<String>> future0 = consumer.receiveAsync();
				Message<String> message = future0.get(5, TimeUnit.SECONDS);
				assertThat(new String(message.getData()))
						.isEqualTo("hello john doe");
			}
		}
	}

	private void testPulsarFunctionality(String pulsarBrokerUrl) throws Exception {
		try (
				PulsarClient client = PulsarClient.builder()
						.serviceUrl(pulsarBrokerUrl)
						.build();
				Consumer consumer = client.newConsumer()
						.topic(TEST_TOPIC)
						.subscriptionName("test-subs")
						.subscribe();
				Producer<byte[]> producer = client.newProducer()
						.topic(TEST_TOPIC)
						.create()
		) {
			producer.send("test containers".getBytes());
			CompletableFuture<Message> future = consumer.receiveAsync();
			Message message = future.get(5, TimeUnit.SECONDS);

			assertThat(new String(message.getData()))
					.isEqualTo("test containers");
		}
	}
}
