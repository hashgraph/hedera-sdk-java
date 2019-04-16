package com.hedera.sdk;

@FunctionalInterface
interface CheckedFunction<T, R, E extends Throwable> {
    R apply(T t) throws E;
}
