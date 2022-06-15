package org.springframework.pulsar.listener.adapter;

import java.lang.reflect.Method;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageListener;

public class PulsarRecordMessagingMessageListenerAdapter<V> extends PulsarMessagingMessageListenerAdapter<V>
		implements MessageListener<V> {

	public PulsarRecordMessagingMessageListenerAdapter(Object bean, Method method) {
		super(bean, method);
	}

	@Override
	public void received(Consumer<V> consumer, Message<V> record) {
		org.springframework.messaging.Message<?> message = null;
		if (isConversionNeeded()) {
			message = toMessagingMessage(record, consumer);
		}
		else {
			//message = NULL_MESSAGE;
		}
		if (logger.isDebugEnabled()) {
			this.logger.debug("Processing [" + message + "]");
		}
		try {
			Object result = invokeHandler(record, message, consumer);
			if (result != null) {
				//handleResult(result, record, message);
			}
		}
		catch (Exception e) { // NOSONAR ex flow control
			throw e;
		}
	}

}
