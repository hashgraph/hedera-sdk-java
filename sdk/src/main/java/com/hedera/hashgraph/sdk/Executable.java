package com.hedera.hashgraph.sdk;

import java.util.concurrent.CompletableFuture;

// note: most of the functionality here is generated in @FunctionalExecutable
abstract class Executable<O> implements WithExecute<O> {
    Executable() {
    }

    @Override
    @FunctionalExecutable
    public abstract CompletableFuture<O> executeAsync(Client client);
}
