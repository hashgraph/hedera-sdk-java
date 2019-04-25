package com.hedera.hashgraph.sdk;

@FunctionalInterface
interface CheckedFunction<T, R, E extends Throwable> {
    R apply(T t) throws E;
}
