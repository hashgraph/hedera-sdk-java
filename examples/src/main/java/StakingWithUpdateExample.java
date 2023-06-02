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

import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.AccountInfo;
import com.hedera.hashgraph.sdk.AccountInfoQuery;
import com.hedera.hashgraph.sdk.AccountUpdateTransaction;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Objects;
import java.util.concurrent.TimeoutException;

public final class StakingWithUpdateExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private StakingWithUpdateExample() {
    }

    public static void main(String[] args) throws TimeoutException, PrecheckStatusException, ReceiptStatusException {
        Client client = Client.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // Create Alice account
        PrivateKey newKey = PrivateKey.generateED25519();

        System.out.println("private key: " + newKey);
        System.out.println("public key: " + newKey.getPublicKey());

        // Create an account and stake to an acount ID
        // In this case we're staking to account ID 3 which happens to be
        // the account ID of node 0, we're only doing this as an example.
        // If you really want to stake to node 0, you should use
        // `.setStakedNodeId()` instead
        AccountId newAccountId = new AccountCreateTransaction()
            .setKey(newKey)
            .setInitialBalance(Hbar.from(10))
            .setStakedAccountId(AccountId.fromString("0.0.3"))
            .execute(client)
            .getReceipt(client)
            .accountId;
        Objects.requireNonNull(newAccountId);

        System.out.println("new account ID: " + newAccountId);
        // Show the required key used to sign the account update transaction to
        // stake the accounts hbar i.e. the fee payer key and key to authorize
        // changes to the account should be different
        System.out.println(
            "key required to update staking information: " + newKey.getPublicKey()
        );
        System.out.println(
            "fee payer aka operator key: " + client.getOperatorPublicKey()
        );

        // Query the account info, it should show the staked account ID
        // to be 0.0.3 just like what we set it to
        AccountInfo info = new AccountInfoQuery()
            .setAccountId(newAccountId)
            .execute(client);

        System.out.println("staking info: " + info.stakingInfo);

        // Use the `AccountUpdateTransaction` to unstake the account's hbars
        //
        // If this succeeds then we should no longer have a staked account ID
        new AccountUpdateTransaction()
            .setAccountId(newAccountId)
            .clearStakedAccountId()
            .freezeWith(client)
            .sign(newKey)
            .execute(client);

        // Query the account info, it should show the staked account ID
        // to be 0.0.3 just like what we set it to
        info = new AccountInfoQuery()
            .setAccountId(newAccountId)
            .execute(client);

        System.out.println("staking info: " + info.stakingInfo);

        client.close();
    }
}
