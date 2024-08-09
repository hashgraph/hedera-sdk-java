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

import com.hedera.hashgraph.sdk.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Objects;

/**
 * How to stake to some account.
 */
class StakingExample {

    // See `.env.sample` in the `examples` folder root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));

    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    public static void main(String[] args) throws Exception {
        /*
         * Step 0:
         * Create and configure the SDK Client.
         */
        Client client = ClientHelper.forName(HEDERA_NETWORK);
        // All generated transactions will be paid by this account and be signed by this key.
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        /*
         * Step 1:
         * Generate an ED25519 key pair for an account.
         */
        PrivateKey alicePrivateKey = PrivateKey.generateED25519();
        PublicKey alicePublicKey = alicePrivateKey.getPublicKey();

        System.out.println("private key: " + alicePrivateKey);
        System.out.println("public key: " + alicePublicKey);

        /*
         * Step 2:
         * Create an account and stake to an account ID.
         * In this case we're staking to account ID 3 which happens to be
         * the account ID of node 0, we're only doing this as an example.
         * If you really want to stake to node 0, you should use
         * `.setStakedNodeId()` instead.
         */
        AccountId newAccountId = new AccountCreateTransaction()
            .setKey(alicePublicKey)
            .setInitialBalance(Hbar.from(10))
            .setStakedAccountId(AccountId.fromString("0.0.3"))
            .execute(client)
            .getReceipt(client)
            .accountId;
        Objects.requireNonNull(newAccountId);

        System.out.println("new account ID: " + newAccountId);
        // Show the required key used to sign the account update transaction to
        // stake the accounts Hbar i.e. the fee payer key and key to authorize
        // changes to the account should be different.
        System.out.println("key required to update staking information: " + alicePublicKey);
        System.out.println("fee payer aka operator key: " + client.getOperatorPublicKey());

        /*
         * Step 3:
         * Query the account info, it should show the staked account ID
         * to be 0.0.3 just like what we set it to,
         */
        AccountInfo info = new AccountInfoQuery()
            .setAccountId(newAccountId)
            .execute(client);

        System.out.println("staking info: " + info.stakingInfo);

        /*
         * Clean up:
         * Delete created account.
         */
        new AccountDeleteTransaction()
            .setAccountId(newAccountId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(alicePrivateKey)
            .execute(client)
            .getReceipt(client);

        client.close();

        System.out.println("Example complete!");
    }
}
