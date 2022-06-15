package org.springframework.pulsar.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.messaging.handler.annotation.MessageMapping;

@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@MessageMapping
@Documented
public @interface PulsarListener {

	String subscriptionName() default "";

	String containerFactory() default "";

	String[] topics() default {};

	String topicPattern() default "";

	String autoStartup() default "";

	String batch() default "";

	String beanRef() default "__listener";

	String[] properties() default {};
}
