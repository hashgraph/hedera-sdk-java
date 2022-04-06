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
