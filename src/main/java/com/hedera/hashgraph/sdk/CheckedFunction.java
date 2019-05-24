package com.hedera.hashgraph.sdk;

@FunctionalInterface
interface CheckedFunction<T, R> {
    R apply(T t) throws HederaException, HederaNetworkException;
}
