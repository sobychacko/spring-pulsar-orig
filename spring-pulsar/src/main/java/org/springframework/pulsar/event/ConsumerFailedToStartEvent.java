package org.springframework.pulsar.event;

public class ConsumerFailedToStartEvent extends PulsarEvent {

	private static final long serialVersionUID = 1L;

	/**
	 * Construct an instance with the provided source and container.
	 * @param source the container instance that generated the event.
	 * @param container the container or the parent container if the container is a child.
	 */
	public ConsumerFailedToStartEvent(Object source, Object container) {
		super(source, container);
	}

	@Override
	public String toString() {
		return "ConsumerFailedToStartEvent [source=" + getSource() + "]";
	}

}
