package org.springframework.pulsar.annotation;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.pulsar.annotation.PulsarListenerAnnotationBeanPostProcessor;
import org.springframework.pulsar.config.PulsarListenerConfigUtils;
import org.springframework.pulsar.config.PulsarListenerEndpointRegistry;

public class PulsarBootstrapConfiguration implements ImportBeanDefinitionRegistrar {

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		if (!registry.containsBeanDefinition(
				PulsarListenerConfigUtils.PULSAR_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME)) {

			registry.registerBeanDefinition(PulsarListenerConfigUtils.PULSAR_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME,
					new RootBeanDefinition(PulsarListenerAnnotationBeanPostProcessor.class));
		}

		if (!registry.containsBeanDefinition(PulsarListenerConfigUtils.PULSAR_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME)) {
			registry.registerBeanDefinition(PulsarListenerConfigUtils.PULSAR_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME,
					new RootBeanDefinition(PulsarListenerEndpointRegistry.class));
		}
	}

}