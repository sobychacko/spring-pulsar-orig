package org.springframework.pulsar;

import org.springframework.core.NestedRuntimeException;

public class PulsarException extends NestedRuntimeException {

	public PulsarException(String msg) {
		super(msg);
	}

	public PulsarException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
