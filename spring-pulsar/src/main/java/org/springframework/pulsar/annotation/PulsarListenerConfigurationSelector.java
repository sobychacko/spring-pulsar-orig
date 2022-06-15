package org.springframework.pulsar.annotation;

import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.pulsar.autoconfig.PulsarBootstrapConfiguration;

@Order
public class PulsarListenerConfigurationSelector implements DeferredImportSelector {

	@Override
	public String[] selectImports(AnnotationMetadata importingClassMetadata) {
		return new String[] { PulsarBootstrapConfiguration.class.getName() };
	}

}
