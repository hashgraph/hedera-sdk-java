package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import java8.util.concurrent.CompletableFuture;
import java8.util.function.Function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TransactionList extends Executable<TransactionResponse> implements WithExecuteAll {
    private final Collection<Transaction> transactions;

    TransactionList(Collection<Transaction> transactions) {
        this.transactions = transactions;
    }

    public TransactionList sign(PrivateKey privateKey) {
        return signWith(privateKey.getPublicKey(), privateKey::sign);
    }

    public TransactionList signWith(PublicKey publicKey, Function<byte[], byte[]> signer) {
        for (Transaction transaction : transactions) {
            transaction.signWith(publicKey, signer);
        }

        return this;
    }

    @Override
    public final CompletableFuture<TransactionResponse> executeAsync(Client client) {
        return executeAllAsync(client).thenApply((list) -> {
            return list.get(0);
        });
    }

    @Override
    @FunctionalExecutable(type = "java.util.List<TransactionResponse>")
    public final CompletableFuture<List<TransactionResponse>> executeAllAsync(Client client) {
        List<CompletableFuture<TransactionResponse>> futures = new ArrayList<>();

        for (Transaction transaction : transactions) {
            futures.add(transaction.executeAsync(client));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply((v) -> {
                List<TransactionResponse> ids = new ArrayList<>();

                for (var future: futures) {
                    ids.add(future.join());
                }

                return ids;
            });
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("transactions", transactions)
            .toString();
    }
}
