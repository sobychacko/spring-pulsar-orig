package org.springframework.pulsar.config;

public abstract class PulsarListenerConfigUtils {

	/**
	 * The bean name of the internally managed Kafka listener annotation processor.
	 */
	public static final String PULSAR_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME =
			"org.springframework.pulsar.config.internalKafkaListenerAnnotationProcessor";

	/**
	 * The bean name of the internally managed Kafka listener endpoint registry.
	 */
	public static final String PULSAR_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME =
			"org.springframework.pulsar.config.internalKafkaListenerEndpointRegistry";

	/**
	 * The bean name of the internally managed Kafka consumer back off manager.
	 */
	public static final String PULSAR_CONSUMER_BACK_OFF_MANAGER_BEAN_NAME =
			"org.springframework.pulsar.config.internalKafkaConsumerBackOffManager";

}

