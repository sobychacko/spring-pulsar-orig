package org.springframework.pulsar.listener;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageListener;
import org.apache.pulsar.client.api.Messages;

public interface PulsarBatchMessageListener<T> extends MessageListener<T> {

	default void received(Consumer<T> consumer, Message<T> msg) {
		throw new UnsupportedOperationException();
	}

	void received(Consumer<T> consumer, Messages<T> msg);
}
