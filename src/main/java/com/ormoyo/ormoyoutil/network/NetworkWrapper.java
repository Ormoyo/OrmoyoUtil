package com.ormoyo.ormoyoutil.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NetworkWrapper {
	Class<? extends AbstractMessage<?>>[] value() default {};
}
