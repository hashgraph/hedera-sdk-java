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
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.TransactionResponse;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Objects;
import java.util.concurrent.TimeoutException;

public class StakingExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private StakingExample() {
    }

    public static void main(String[] args) throws TimeoutException, PrecheckStatusException, ReceiptStatusException {
        Client client = Client.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        System.out.println("Generating accounts for Staking example...");

        // Create Alice account
        PrivateKey aliceKey = PrivateKey.generateED25519();
        AccountId aliceId = new AccountCreateTransaction()
            .setKey(aliceKey)
            .setInitialBalance(Hbar.from(10))
            .execute(client)
            .getReceipt(client)
            .accountId;
        Objects.requireNonNull(aliceId);

        // Create Bob account
        PrivateKey bobKey = PrivateKey.generateED25519();
        AccountId bobId = new AccountCreateTransaction()
            .setKey(bobKey)
            .setInitialBalance(Hbar.from(10))
            .execute(client)
            .getReceipt(client)
            .accountId;
        Objects.requireNonNull(bobId);

        printAccountInfo(client, aliceId, "Alice");
        printAccountInfo(client, bobId, "Bob");

        // Alice stakes hbar to bob
        AccountUpdateTransaction aliceTx =  new AccountUpdateTransaction()
            .setAccountId(aliceId)
            .setProxyAccountId(bobId);

        TransactionResponse aliceTxResp = aliceTx
            .freezeWith(client)
            .sign(aliceKey)
            .execute(client);

        TransactionReceipt aliceReceipt = aliceTxResp.getReceipt(client);

        System.out.println("Alice Receipt Status: " + aliceReceipt.status);

        printAccountInfo(client, aliceId, "Alice");
        printAccountInfo(client, bobId, "Bob");

        client.close();
    }

    private static void printAccountInfo(Client client, AccountId accountId, String who) throws PrecheckStatusException, TimeoutException {
        AccountInfo info = new AccountInfoQuery().setAccountId(accountId).execute(client);
        System.out.println(
            who + "'s Info: \t"
                + add("Account ID", info.accountId.toString())
                + add("Balance", info.balance.toString())
                + add("Proxy Account ID", info.proxyAccountId != null ? info.proxyAccountId.toString() : "null")
                + add("Proxy Received", info.proxyReceived.toString())
                + "\n"
        );
    }

    private static String add(String msg, String data) {
        return msg + ": " + data + " ";
    }
}
