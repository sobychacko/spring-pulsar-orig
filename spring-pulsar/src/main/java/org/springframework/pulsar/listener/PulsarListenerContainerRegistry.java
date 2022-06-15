package org.springframework.pulsar.listener;

import java.util.Collection;
import java.util.Set;

import org.springframework.lang.Nullable;
import org.springframework.pulsar.listener.PulsarMessageListenerContainer;

public interface PulsarListenerContainerRegistry {

	@Nullable
	PulsarMessageListenerContainer getListenerContainer(String id);

	Set<String> getListenerContainerIds();

	Collection<PulsarMessageListenerContainer> getListenerContainers();

	Collection<PulsarMessageListenerContainer> getAllListenerContainers();
}
