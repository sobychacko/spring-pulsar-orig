package org.springframework.pulsar.event;

import java.time.Clock;

import org.springframework.context.ApplicationEvent;
import org.springframework.util.Assert;

public class PulsarEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	private final Object container;

	public PulsarEvent(Object source, Object container) {
		super(source);
		this.container = container;
	}

	@SuppressWarnings("unchecked")
	public <T> T getContainer(Class<T> type) {
		Assert.isInstanceOf(type, this.container);
		return (T) this.container;
	}

	@SuppressWarnings("unchecked")
	public <T> T getSource(Class<T> type) {
		Assert.isInstanceOf(type, getSource());
		return (T) getSource();
	}

}
