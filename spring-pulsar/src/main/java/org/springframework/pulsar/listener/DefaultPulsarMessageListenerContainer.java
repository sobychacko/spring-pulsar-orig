package org.springframework.pulsar.listener;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.pulsar.client.api.BatchReceivePolicy;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageListener;
import org.apache.pulsar.client.api.Messages;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.lang.Nullable;
import org.springframework.pulsar.core.PulsarConsumerFactory;
import org.springframework.pulsar.event.ConsumerFailedToStartEvent;
import org.springframework.pulsar.event.ConsumerStartedEvent;
import org.springframework.pulsar.event.ConsumerStartingEvent;
import org.springframework.scheduling.SchedulingAwareRunnable;
import org.springframework.util.concurrent.ListenableFuture;

public class DefaultPulsarMessageListenerContainer<T> extends AbstractPulsarMessageListenerContainer<T> {

	private volatile boolean running = false;

	private String beanName;

	private volatile ListenableFuture<?> listenerConsumerFuture;

	private volatile Listener listenerConsumer;

	private volatile CountDownLatch startLatch = new CountDownLatch(1);

	private final AbstractPulsarMessageListenerContainer thisOrParentContainer;


	public DefaultPulsarMessageListenerContainer(PulsarConsumerFactory<? super T> pulsarConsumerFactory, PulsarContainerProperties pulsarContainerProperties) {
		super(pulsarConsumerFactory, pulsarContainerProperties);
		this.thisOrParentContainer = this;
	}


	@Override
	public void start() {
		try {
			doStart();
		}
		catch (PulsarClientException e) {
			e.printStackTrace(); //deal later
		}
	}

	private void doStart() throws PulsarClientException {

		PulsarContainerProperties containerProperties = getPulsarContainerProperties();

		Object messageListener = containerProperties.getMessageListener();
		AsyncListenableTaskExecutor consumerExecutor = containerProperties.getConsumerTaskExecutor();

		@SuppressWarnings("unchecked")
		MessageListener<T> messageListener1 = (MessageListener<T>) messageListener;

		if (consumerExecutor == null) {
			consumerExecutor = new SimpleAsyncTaskExecutor(
					(getBeanName() == null ? "" : getBeanName()) + "-C-");
			containerProperties.setConsumerTaskExecutor(consumerExecutor);
		}

		this.listenerConsumer = new Listener(messageListener1);
		setRunning(true);
		this.startLatch = new CountDownLatch(1);
		this.listenerConsumerFuture = consumerExecutor.submitListenable(this.listenerConsumer);

		try {
			if (!this.startLatch.await(containerProperties.getConsumerStartTimeout().toMillis(), TimeUnit.MILLISECONDS)) {
				this.logger.error("Consumer thread failed to start - does the configured task executor "
						+ "have enough threads to support all containers and concurrency?");
				publishConsumerFailedToStart();
			}
		}
		catch (@SuppressWarnings("UNUSED") InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public void stop() {

	}

	@Override
	public boolean isRunning() {
		return this.running;
	}

	protected void setRunning(boolean running) {
		this.running = running;
	}

	/**
	 * Return the bean name.
	 * @return the bean name.
	 */
	@Nullable
	public String getBeanName() {
		return this.beanName;
	}

	@Override
	public void destroy() {

	}

	private void publishConsumerStartingEvent() {
		this.startLatch.countDown();
		ApplicationEventPublisher publisher = getApplicationEventPublisher();
		if (publisher != null) {
			publisher.publishEvent(new ConsumerStartingEvent(this, this.thisOrParentContainer));
		}
	}

	private void publishConsumerStartedEvent() {
		ApplicationEventPublisher publisher = getApplicationEventPublisher();
		if (publisher != null) {
			publisher.publishEvent(new ConsumerStartedEvent(this, this.thisOrParentContainer));
		}
	}

	private void publishConsumerFailedToStart() {
		ApplicationEventPublisher publisher = getApplicationEventPublisher();
		if (publisher != null) {
			publisher.publishEvent(new ConsumerFailedToStartEvent(this, this.thisOrParentContainer));
		}
	}

	private final class Listener implements SchedulingAwareRunnable {

		private final MessageListener<T> listener;
		private final PulsarBatchMessageListener<T> batchMessageHandler;
		Consumer<T> consumer;

		private final PulsarContainerProperties containerProperties = getPulsarContainerProperties();

		private volatile Thread consumerThread;

		@SuppressWarnings("unchecked")
		Listener(MessageListener<?> messageListener) {
			if (messageListener instanceof PulsarBatchMessageListener) {
				this.batchMessageHandler = (PulsarBatchMessageListener<T>) messageListener;
				this.listener = null;

			}
			else if (messageListener != null) {
				this.listener = (MessageListener<T>) messageListener;
				this.batchMessageHandler = null;
			}
			else {
				this.listener = null;
				this.batchMessageHandler = null;
			}

		}

		@Override
		public boolean isLongLived() {
			return true;
		}

		@Override
		@SuppressWarnings({"unchecked", "rawtypes"})
		public void run() {
			publishConsumerStartingEvent();
			this.consumerThread = Thread.currentThread();
			try {
				final PulsarContainerProperties pulsarContainerProperties = getPulsarContainerProperties();
				if (this.containerProperties.isBatchReceive() || this.containerProperties.isBatchAsyncReceive()) {

					final BatchReceivePolicy batchReceivePolicy = BatchReceivePolicy.DEFAULT_POLICY;

					this.consumer = getPulsarConsumerFactory().createConsumer(
							(Schema) pulsarContainerProperties.getSchema(), pulsarContainerProperties.getSubscriptionType(),
							batchReceivePolicy);
				}
				else if (this.containerProperties.isAsyncReceive()){
					this.consumer = getPulsarConsumerFactory().createConsumer(
							(Schema) pulsarContainerProperties.getSchema(), pulsarContainerProperties.getSubscriptionType());
				}
				else {
					this.consumer = getPulsarConsumerFactory().createConsumer(
							(Schema) pulsarContainerProperties.getSchema(), pulsarContainerProperties.getSubscriptionType(), pulsarContainerProperties);
				}
			}
			catch (PulsarClientException e) {
				e.printStackTrace(); //TODO - Proper logging
			}
			publishConsumerStartedEvent();
			while (isRunning()) {
				Message<T> msg = null;
				Messages<T> messages = null;
				try {
					// Wait for a message
					if (this.containerProperties.isBatchReceive()) {
						messages = consumer.batchReceive();
						this.batchMessageHandler.received(consumer, messages);
						consumer.acknowledge(messages);
					}
					else if (this.containerProperties.isBatchAsyncReceive()) {
						final CompletableFuture<Messages<T>> messagesCompletableFuture = consumer.batchReceiveAsync();

						messagesCompletableFuture.thenAccept(messages1 -> {
							if (messages1 != null) {
								this.batchMessageHandler.received(consumer, messages1);
								if (this.containerProperties.getAckMode() != PulsarContainerProperties.AckMode.MANUAL) {
									try {
										consumer.acknowledge(messages1);
									}
									catch (PulsarClientException e) {
										consumer.negativeAcknowledge(messages1);
									}
								}
							}
						});
						//consumer can do other things - but nothing ATM.
					}
					else if (this.containerProperties.isAsyncReceive()) {
						final CompletableFuture<Message<T>> messageCompletableFuture = consumer.receiveAsync();
						messageCompletableFuture.thenAccept(messages1 -> {
							if (messages1 != null) {
								this.listener.received(consumer, messages1);
								if (this.containerProperties.getAckMode() != PulsarContainerProperties.AckMode.MANUAL) {
									try {
										consumer.acknowledge(messages1);
									}
									catch (PulsarClientException e) {
										consumer.negativeAcknowledge(messages1);
									}
								}
							}
						});
						//consumer can do other things - but nothing ATM.
						//we may make this mode as the default rather than the sync mode below.
					}
					else {
						try {
							msg = consumer.receive();
							this.listener.received(consumer, msg);
							if (this.containerProperties.getAckMode() != PulsarContainerProperties.AckMode.MANUAL) {
								consumer.acknowledge(msg);
							}
						}
						catch (Exception e) {
							consumer.negativeAcknowledge(msg);
						}
					}
				}
				catch (Exception e) {
					// Message failed to process, redeliver later
					consumer.negativeAcknowledge(msg);
				}
			}
			try {
				this.consumer.close();
			}
			catch (PulsarClientException e) {
				e.printStackTrace(); //TODO - Proper logging
			}
		}
	}

}
