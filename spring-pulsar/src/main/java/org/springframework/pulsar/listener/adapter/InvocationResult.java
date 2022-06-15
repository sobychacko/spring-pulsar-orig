package org.springframework.pulsar.listener.adapter;

import org.springframework.expression.Expression;
import org.springframework.lang.Nullable;

public final class InvocationResult {

	@Nullable
	private final Object result;

	@Nullable
	private final Expression sendTo;

	private final boolean messageReturnType;

	public InvocationResult(@Nullable Object result, @Nullable Expression sendTo, boolean messageReturnType) {
		this.result = result;
		this.sendTo = sendTo;
		this.messageReturnType = messageReturnType;
	}

	@Nullable
	public Object getResult() {
		return this.result;
	}

	@Nullable
	public Expression getSendTo() {
		return this.sendTo;
	}

	public boolean isMessageReturnType() {
		return this.messageReturnType;
	}

	@Override
	public String toString() {
		return "InvocationResult [result=" + this.result
				+ ", sendTo=" + (this.sendTo == null ? "null" : this.sendTo.getExpressionString())
				+ ", messageReturnType=" + this.messageReturnType + "]";
	}

}

