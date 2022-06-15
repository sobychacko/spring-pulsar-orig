package org.springframework.pulsar.autoconfig;

import org.apache.pulsar.client.api.Schema;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.pulsar.annotation.EnablePulsar;
import org.springframework.pulsar.core.PulsarConsumerFactory;
import org.springframework.pulsar.listener.PulsarContainerProperties;
import org.springframework.pulsar.config.PulsarListenerConfigUtils;
import org.springframework.pulsar.config.PulsarListenerContainerFactoryImpl;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(EnablePulsar.class)
public class PulsarAnnotationDrivenConfiguration {

	private final PulsarProperties pulsarProperties;

	public PulsarAnnotationDrivenConfiguration(PulsarProperties pulsarProperties) {
		this.pulsarProperties = pulsarProperties;
	}

	@Bean
	@ConditionalOnMissingBean(name = "pulsarListenerContainerFactory")
	PulsarListenerContainerFactoryImpl<?, ?> pulsarListenerContainerFactory(
			ObjectProvider<PulsarConsumerFactory<Object>> pulsarConsumerFactory) {
		PulsarListenerContainerFactoryImpl<Object, Object> factory = new PulsarListenerContainerFactoryImpl<>();

		factory.setPulsarConsumerFactory(pulsarConsumerFactory.getIfAvailable());

		final PulsarContainerProperties containerProperties = factory.getContainerProperties();

		PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
		PulsarProperties.Listener properties = this.pulsarProperties.getListener();

		map.from(properties::getSchema).as(
				schema1 -> switch (schema1) {
					case STRING -> Schema.STRING;
					case BYTES -> Schema.BYTES;
					case BYTEBUFFER -> Schema.BYTEBUFFER;
				}).to(containerProperties::setSchema);

		return factory;
	}

	@Configuration(proxyBeanMethods = false)
	@EnablePulsar
	@ConditionalOnMissingBean(name = PulsarListenerConfigUtils.PULSAR_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME)
	static class EnableKafkaConfiguration {

	}

}
