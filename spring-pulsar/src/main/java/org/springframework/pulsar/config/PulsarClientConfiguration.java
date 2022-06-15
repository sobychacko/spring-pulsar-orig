package org.springframework.pulsar.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

public class PulsarClientConfiguration {

	private final Map<String, Object> configs = new HashMap<>();

	public PulsarClientConfiguration() {
		this(Map.of("serviceUrl", "pulsar://localhost:6650"));
	}

	public PulsarClientConfiguration(Map<String, Object> configs) {
		Assert.notNull(configs, "Configuration map cannot be null");
		this.configs.putAll(configs);
		this.configs.putIfAbsent("serviceUrl", "pulsar://localhost:6650");
	}

	public Map<String, Object> getConfigs() {
		return configs;
	}
}
