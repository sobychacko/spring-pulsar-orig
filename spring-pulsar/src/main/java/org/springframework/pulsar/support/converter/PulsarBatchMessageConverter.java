package org.springframework.pulsar.support.converter;

import java.lang.reflect.Type;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Messages;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.pulsar.support.MessageConverter;

public interface PulsarBatchMessageConverter<T> extends MessageConverter {

	@NonNull
	Message<?> toMessage(Messages<T> records, Consumer<T> consumer, Type payloadType);

	T fromMessage(Messages<T> message, String defaultTopic);

	@Nullable
	default PulsarRecordMessageConverter<T> getRecordMessageConverter() {
		return null;
	}
}
