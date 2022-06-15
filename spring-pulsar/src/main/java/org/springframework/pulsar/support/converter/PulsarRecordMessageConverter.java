package org.springframework.pulsar.support.converter;

import java.lang.reflect.Type;

import org.apache.pulsar.client.api.Consumer;

import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.pulsar.support.MessageConverter;

public interface PulsarRecordMessageConverter<T> extends MessageConverter {


	@NonNull
	Message<?> toMessage(org.apache.pulsar.client.api.Message<T> record, Consumer<T> consumer,
						 Type payloadType);

	T fromMessage(Message<?> message, String defaultTopic);

}
