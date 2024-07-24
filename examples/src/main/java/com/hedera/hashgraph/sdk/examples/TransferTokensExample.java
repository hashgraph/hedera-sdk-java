/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2024 Hedera Hashgraph, LLC
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
package com.hedera.hashgraph.sdk.examples;

import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TokenAssociateTransaction;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenDeleteTransaction;
import com.hedera.hashgraph.sdk.TokenGrantKycTransaction;
import com.hedera.hashgraph.sdk.TokenId;
import com.hedera.hashgraph.sdk.TokenWipeTransaction;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.TransactionResponse;
import com.hedera.hashgraph.sdk.TransferTransaction;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Collections;
import java.util.Objects;

public final class TransferTokensExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private TransferTokensExample() {
    }

    public static void main(String[] args) throws Exception {
        Client client = ClientHelper.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // Generate a Ed25519 private, public key pair
        PrivateKey key1 = PrivateKey.generateED25519();
        PrivateKey key2 = PrivateKey.generateED25519();

        System.out.println("private key = " + key1);
        System.out.println("public key = " + key1.getPublicKey());
        System.out.println("private key = " + key2);
        System.out.println("public key = " + key2.getPublicKey());

        @Var TransactionResponse response = new AccountCreateTransaction()
            // The only _required_ property here is `key`
            .setKey(key1.getPublicKey())
            .setInitialBalance(Hbar.fromTinybars(1000))
            .execute(client);

        // This will wait for the receipt to become available
        @Var TransactionReceipt receipt = response.getReceipt(client);

        AccountId accountId1 = Objects.requireNonNull(receipt.accountId);

        System.out.println("accountId1 = " + accountId1);

        response = new AccountCreateTransaction()
            // The only _required_ property here is `key`
            .setKey(key2.getPublicKey())
            .setInitialBalance(Hbar.fromTinybars(1000))
            .execute(client);

        // This will wait for the receipt to become available
        receipt = response.getReceipt(client);

        AccountId accountId2 = Objects.requireNonNull(receipt.accountId);

        System.out.println("accountId2 = " + accountId1);

        response = new TokenCreateTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .setTokenName("ffff")
            .setTokenSymbol("F")
            .setDecimals(3)
            .setInitialSupply(1000000)
            .setTreasuryAccountId(OPERATOR_ID)
            .setAdminKey(OPERATOR_KEY.getPublicKey())
            .setFreezeKey(OPERATOR_KEY.getPublicKey())
            .setWipeKey(OPERATOR_KEY.getPublicKey())
            .setKycKey(OPERATOR_KEY.getPublicKey())
            .setSupplyKey(OPERATOR_KEY.getPublicKey())
            .setFreezeDefault(false)
            .execute(client);

        TokenId tokenId = Objects.requireNonNull(response.getReceipt(client).tokenId);
        System.out.println("token = " + tokenId);

        new TokenAssociateTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .setAccountId(accountId1)
            .setTokenIds(Collections.singletonList(tokenId))
            .freezeWith(client)
            .sign(OPERATOR_KEY)
            .sign(key1)
            .execute(client)
            .getReceipt(client);

        System.out.println("Associated account " + accountId1 + " with token " + tokenId);

        new TokenAssociateTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .setAccountId(accountId2)
            .setTokenIds(Collections.singletonList(tokenId))
            .freezeWith(client)
            .sign(OPERATOR_KEY)
            .sign(key2)
            .execute(client)
            .getReceipt(client);

        System.out.println("Associated account " + accountId2 + " with token " + tokenId);

        new TokenGrantKycTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .setAccountId(accountId1)
            .setTokenId(tokenId)
            .execute(client)
            .getReceipt(client);

        System.out.println("Granted KYC for account " + accountId1 + " on token " + tokenId);

        new TokenGrantKycTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .setAccountId(accountId2)
            .setTokenId(tokenId)
            .execute(client)
            .getReceipt(client);

        System.out.println("Granted KYC for account " + accountId2 + " on token " + tokenId);

        new TransferTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .addTokenTransfer(tokenId, OPERATOR_ID, -10)
            .addTokenTransfer(tokenId, accountId1, 10)
            .execute(client)
            .getReceipt(client);

        System.out.println("Sent 10 tokens from account " + OPERATOR_ID + " to account " + accountId1 + " on token " + tokenId);

        new TransferTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .addTokenTransfer(tokenId, accountId1, -10)
            .addTokenTransfer(tokenId, accountId2, 10)
            .freezeWith(client)
            .sign(key1)
            .execute(client)
            .getReceipt(client);

        System.out.println("Sent 10 tokens from account " + accountId1 + " to account " + accountId2 + " on token " + tokenId);

        new TransferTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .addTokenTransfer(tokenId, accountId2, -10)
            .addTokenTransfer(tokenId, accountId1, 10)
            .freezeWith(client)
            .sign(key2)
            .execute(client)
            .getReceipt(client);

        System.out.println("Sent 10 tokens from account " + accountId2 + " to account " + accountId1 + " on token " + tokenId);

        new TokenWipeTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .setTokenId(tokenId)
            .setAccountId(accountId1)
            .setAmount(10)
            .execute(client)
            .getReceipt(client);

        System.out.println("Wiped balance of account " + accountId1);

        new TokenDeleteTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .setTokenId(tokenId)
            .execute(client)
            .getReceipt(client);

        System.out.println("Deleted token " + tokenId);

        new AccountDeleteTransaction()
            .setAccountId(accountId1)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(OPERATOR_KEY)
            .sign(key1)
            .execute(client)
            .getReceipt(client);

        System.out.println("Deleted accountId1 " + accountId1);

        new AccountDeleteTransaction()
            .setAccountId(accountId2)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(OPERATOR_KEY)
            .sign(key2)
            .execute(client)
            .getReceipt(client);

        System.out.println("Deleted accountId2" + accountId2);
    }
}
