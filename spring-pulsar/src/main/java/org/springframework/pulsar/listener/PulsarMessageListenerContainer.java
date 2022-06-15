package org.springframework.pulsar.listener;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.SmartLifecycle;

public interface PulsarMessageListenerContainer extends SmartLifecycle, DisposableBean {

	void setupMessageListener(Object messageListener);

	@Override
	default void destroy() {
		stop();
	}

	default void setAutoStartup(boolean autoStartup) {
		// empty
	}

}
