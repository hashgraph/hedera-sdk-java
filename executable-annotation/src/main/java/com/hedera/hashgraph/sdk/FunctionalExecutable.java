package com.hedera.hashgraph.sdk;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface FunctionalExecutable {
    // additional exception types that we can throw
    String[] exceptionTypes() default {};

    // empty string means make this generic
    String type() default "";

    String inputType() default "";

    boolean onClient() default false;
}
