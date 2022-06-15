package org.springframework.pulsar.core;

import org.apache.pulsar.client.api.Schema;

public class SchemaUtils {

	@SuppressWarnings("unchecked")
	public static <T>  Schema<T> getSchema(T message) {
		if (message.getClass() == byte[].class) {
			return (Schema<T>) Schema.BYTES;
		}
		else if (message.getClass() == String.class) {
			return (Schema<T>) Schema.STRING;
		}
		return (Schema<T>) Schema.BYTES;
	}
}
