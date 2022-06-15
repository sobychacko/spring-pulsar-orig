package org.springframework.pulsar.autoconfig;

import org.apache.pulsar.client.api.PulsarClient;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.pulsar.core.DefaultPulsarConsumerFactory;
import org.springframework.pulsar.core.DefaultPulsarProducerFactory;
import org.springframework.pulsar.config.PulsarClientConfiguration;
import org.springframework.pulsar.config.PulsarClientFactoryBean;
import org.springframework.pulsar.core.PulsarConsumerFactory;
import org.springframework.pulsar.core.PulsarProducerFactory;
import org.springframework.pulsar.core.PulsarTemplate;

@AutoConfiguration
@ConditionalOnClass(PulsarTemplate.class)
@EnableConfigurationProperties(PulsarProperties.class)
@Import({ PulsarAnnotationDrivenConfiguration.class })
public class PulsarAutoConfiguration {

	private final PulsarProperties properties;

	public PulsarAutoConfiguration(PulsarProperties properties) {
		this.properties = properties;
	}

	@Bean
	public PulsarClientFactoryBean pulsarClientFactoryBean(PulsarClientConfiguration pulsarClientConfiguration) {
		return new PulsarClientFactoryBean(pulsarClientConfiguration);
	}

	@Bean
	public PulsarClientConfiguration pulsarClientConfiguration() {
		return new PulsarClientConfiguration(this.properties.buildClientProperties());
	}

	@Bean
	public PulsarProducerFactory<?> pulsarProducerFactory(PulsarClient pulsarClient) {
		return new DefaultPulsarProducerFactory<>(pulsarClient, this.properties.buildProducerProperties());
	}

	@Bean
	public PulsarTemplate<?> pulsarTemplate(PulsarProducerFactory<?> pulsarProducerFactory) {
		return new PulsarTemplate<>(pulsarProducerFactory);
	}

	@Bean
	@ConditionalOnMissingBean(PulsarConsumerFactory.class)
	public PulsarConsumerFactory<?> pulsarConsumerFactory(PulsarClient pulsarClient) {
		DefaultPulsarConsumerFactory<Object> factory = new DefaultPulsarConsumerFactory<>(pulsarClient,
				this.properties.buildConsumerProperties());
		return factory;
	}

}
