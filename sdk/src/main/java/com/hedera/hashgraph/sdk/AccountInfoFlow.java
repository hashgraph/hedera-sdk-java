package com.hedera.hashgraph.sdk;

import java8.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

public class AccountInfoFlow {

    private static PublicKey getAccountPublicKey(
        Client client,
        AccountId accountId
    ) throws PrecheckStatusException, TimeoutException {
        return requirePublicKey(accountId, new AccountInfoQuery().setAccountId(accountId).execute(client).key);
    }

    private static CompletableFuture<PublicKey> getAccountPublicKeyAsync(Client client, AccountId accountId) {
        return new AccountInfoQuery().setAccountId(accountId).executeAsync(client).thenApply(accountInfo -> {
            return requirePublicKey(accountId, accountInfo.key);
        });
    }

    private static PublicKey requirePublicKey(AccountId accountId, Key key) {
        if (key instanceof PublicKey) {
            return (PublicKey) key;
        }
        throw new UnsupportedOperationException("Account " + accountId + " has a KeyList key, which is not supported");
    }

    public static boolean verifySignature(
        Client client,
        AccountId accountId,
        byte[] message,
        byte[] signature
    ) throws PrecheckStatusException, TimeoutException {
        return getAccountPublicKey(client, accountId).verify(message, signature);
    }

    public static boolean verifyTransactionSignature(
        Client client,
        AccountId accountId,
        Transaction<?> transaction
    ) throws PrecheckStatusException, TimeoutException {
        return getAccountPublicKey(client, accountId).verifyTransaction(transaction);
    }

    public static CompletableFuture<Boolean> verifySignatureAsync(
        Client client,
        AccountId accountId,
        byte[] message,
        byte[] signature
    ) {
        return getAccountPublicKeyAsync(client, accountId).thenApply(pubKey -> pubKey.verify(message, signature));
    }

    public static CompletableFuture<Boolean> verifyTransactionSignatureAsync(
        Client client,
        AccountId accountId,
        Transaction<?> transaction
    ) {
        return getAccountPublicKeyAsync(client, accountId).thenApply(pubKey -> pubKey.verifyTransaction(transaction));
    }
}
