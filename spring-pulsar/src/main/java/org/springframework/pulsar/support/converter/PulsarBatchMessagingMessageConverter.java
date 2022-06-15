package org.springframework.pulsar.support.converter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Messages;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.pulsar.support.converter.PulsarBatchMessageConverter;
import org.springframework.pulsar.support.converter.PulsarRecordMessageConverter;

public class PulsarBatchMessagingMessageConverter<T> implements PulsarBatchMessageConverter<T> {

	private final PulsarRecordMessageConverter recordConverter;


	public PulsarBatchMessagingMessageConverter() {
		this(null);
	}

	public PulsarBatchMessagingMessageConverter(PulsarRecordMessageConverter recordConverter) {
		this.recordConverter = recordConverter;
	}

	@Override
	public Message<?> toMessage(Messages<T> records, Consumer<T> consumer, Type type) {
		List<Object> payloads = new ArrayList<>();
		List<Exception> conversionFailures = new ArrayList<>();
		for (org.apache.pulsar.client.api.Message<T> message : records) {
			payloads.add(obtainPayload(type, message, conversionFailures));
		}

		return MessageBuilder.createMessage(payloads, new MessageHeaders(Collections.emptyMap()));
	}

	private Object obtainPayload(Type type, org.apache.pulsar.client.api.Message<T> record, List<Exception> conversionFailures) {
		return this.recordConverter == null || !containerType(type)
				? extractAndConvertValue(record, type)
				: convert(record, type, conversionFailures);
	}

	private boolean containerType(Type type) {
		return type instanceof ParameterizedType
				&& ((ParameterizedType) type).getActualTypeArguments().length == 1;
	}

	protected Object extractAndConvertValue(org.apache.pulsar.client.api.Message<T> record, Type type) {
		return record.getValue();
	}

	protected Object convert(org.apache.pulsar.client.api.Message<T> record, Type type, List<Exception> conversionFailures) {
		try {
			Object payload = this.recordConverter
					.toMessage(record, null, ((ParameterizedType) type).getActualTypeArguments()[0]).getPayload();
			conversionFailures.add(null);
			return payload;
		}
		catch (Exception ex) {
			throw new RuntimeException("The batch converter can only report conversion failures to the listener "
					+ "if the record.value() is byte[], Bytes, or String", ex);
		}
	}

	@Override
	public T fromMessage(Messages<T> message, String defaultTopic) {
		throw new UnsupportedOperationException();
	}
}
