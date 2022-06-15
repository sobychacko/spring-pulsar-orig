package org.springframework.pulsar.annotation;

import org.springframework.pulsar.config.PulsarListenerEndpointRegistrar;

public interface PulsarListenerConfigurer {

	void configurePulsarListeners(PulsarListenerEndpointRegistrar registrar);
}
