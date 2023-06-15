/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.sdk;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * Account Info Flow object.
 */
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
        if (key instanceof PublicKey k) {
            return k;
        }
        throw new UnsupportedOperationException("Account " + accountId + " has a KeyList key, which is not supported");
    }

    /**
     * Is the signature valid.
     *
     * @param client    the client
     * @param accountId the account id
     * @param message   the message
     * @param signature the signature
     * @return is the signature valid
     * @throws PrecheckStatusException when the precheck fails
     * @throws TimeoutException        when the transaction times out
     */
    public static boolean verifySignature(
        Client client,
        AccountId accountId,
        byte[] message,
        byte[] signature
    ) throws PrecheckStatusException, TimeoutException {
        return getAccountPublicKey(client, accountId).verify(message, signature);
    }

    /**
     * Is the transaction signature valid.
     *
     * @param client      the client
     * @param accountId   the account id
     * @param transaction the signed transaction
     * @return is the transaction signature valid
     * @throws PrecheckStatusException when the precheck fails
     * @throws TimeoutException        when the transaction times out
     */
    public static boolean verifyTransactionSignature(
        Client client,
        AccountId accountId,
        Transaction<?> transaction
    ) throws PrecheckStatusException, TimeoutException {
        return getAccountPublicKey(client, accountId).verifyTransaction(transaction);
    }

    /**
     * Asynchronously determine if the signature is valid.
     *
     * @param client    the client
     * @param accountId the account id
     * @param message   the message
     * @param signature the signature
     * @return is the signature valid
     */
    public static CompletableFuture<Boolean> verifySignatureAsync(
        Client client,
        AccountId accountId,
        byte[] message,
        byte[] signature
    ) {
        return getAccountPublicKeyAsync(client, accountId).thenApply(pubKey -> pubKey.verify(message, signature));
    }

    /**
     * Asynchronously determine if the signature is valid.
     *
     * @param client      the client
     * @param accountId   the account id
     * @param transaction the signed transaction
     * @return is the signature valid
     */
    public static CompletableFuture<Boolean> verifyTransactionSignatureAsync(
        Client client,
        AccountId accountId,
        Transaction<?> transaction
    ) {
        return getAccountPublicKeyAsync(client, accountId).thenApply(pubKey -> pubKey.verifyTransaction(transaction));
    }
}
