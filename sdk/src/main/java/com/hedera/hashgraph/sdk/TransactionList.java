package com.hedera.hashgraph.sdk;

import java8.util.concurrent.CompletableFuture;
import java8.util.function.Function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TransactionList extends Executable<TransactionId> {
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
    public final CompletableFuture<TransactionId> executeAsync(Client client) {
        return executeAll(client).thenApply((list) -> {
            return list.get(0);
        });
    }

    public final CompletableFuture<List<TransactionId>> executeAll(Client client) {
        List<CompletableFuture<TransactionId>> futures = new ArrayList<>();

        for (Transaction transaction : transactions) {
            futures.add(transaction.executeAsync(client));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply((v) -> {
                List<TransactionId> ids = new ArrayList<>();

                for (var future: futures) {
                    ids.add(future.join());
                }

                return ids;
            });
    }
}
