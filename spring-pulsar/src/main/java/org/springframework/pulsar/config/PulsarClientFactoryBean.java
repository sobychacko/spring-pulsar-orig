package org.springframework.pulsar.config;

import org.apache.pulsar.client.api.PulsarClient;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.pulsar.config.PulsarClientConfiguration;

public class PulsarClientFactoryBean extends AbstractFactoryBean<PulsarClient> {

	private final PulsarClientConfiguration pulsarClientConfiguration;

	public PulsarClientFactoryBean(PulsarClientConfiguration pulsarClientConfiguration) {
		this.pulsarClientConfiguration = pulsarClientConfiguration;
	}

	@Override
	public Class<?> getObjectType() {
		return PulsarClient.class;
	}

	@Override
	protected PulsarClient createInstance() throws Exception {
		return PulsarClient.builder()
				.loadConf(this.pulsarClientConfiguration.getConfigs())
				.build();
	}
}
