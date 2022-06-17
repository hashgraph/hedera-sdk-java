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
import com.hedera.hashgraph.sdk.AccountStakersQuery;
import com.hedera.hashgraph.sdk.AccountUpdateTransaction;
import com.hedera.hashgraph.sdk.AddressBookQuery;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.FileId;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.NodeAddress;
import com.hedera.hashgraph.sdk.NodeAddressBook;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ProxyStaker;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.TransactionResponse;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.List;
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

        System.out.println("Staking Example");

        System.out.println("1.1 - Testnet Account Info");
        System.out.println("Operator Id:  " + OPERATOR_ID);
        System.out.println("Operator Key: " + OPERATOR_KEY);
        System.out.println();

        System.out.println("1.2, 1.3 - Generating Alice account");

        // Create Alice account
        PrivateKey alicePrivateKey = PrivateKey.generateED25519();
        PublicKey alicePublicKey = alicePrivateKey.getPublicKey();

        AccountId aliceId = new AccountCreateTransaction()
            .setKey(alicePrivateKey)
            .setInitialBalance(Hbar.from(10))
            .execute(client)
            .getReceipt(client)
            .accountId;
        Objects.requireNonNull(aliceId);

        printAccountInfo(client, "Alice", aliceId, alicePrivateKey, alicePublicKey);

        System.out.println("Example 1: Stake hbars from an existing account.");

        NodeAddress nodeInfo = fetchNode(client,0);

        // Alice stakes hbar to node account
        AccountUpdateTransaction aliceTx =  new AccountUpdateTransaction()
            .setAccountId(aliceId)
            . (nodeInfo.getNodeId());
//            .setProxyAccountId(nodeInfo.getAccountId());

        TransactionResponse aliceTxResp = aliceTx
            .freezeWith(client)
            .sign(alicePrivateKey)
            .execute(client);

        TransactionReceipt aliceReceipt = aliceTxResp.getReceipt(client);

        System.out.println("1.4 - Staking transaction status.");

        System.out.println("Alice Receipt Status: " + aliceReceipt.status);

        nodeInfo = fetchNode(client, 0);

        System.out.println("1.5 - Staking info");

        System.out.println(nodeInfo.toString());
        List<ProxyStaker> accountStakersQuery = new AccountStakersQuery()
            .setAccountId(aliceId)
            .execute(client);

        System.out.println(accountStakersQuery.toString());

        System.out.println("Example 2: Create a new account and stake the account and stake teh account's hbars.");

        // Create Bob account
        PrivateKey bobPrivateKey = PrivateKey.generateED25519();
        PublicKey bobPublicKey = bobPrivateKey.getPublicKey();

        AccountId bobId = new AccountCreateTransaction()
            .setKey(bobPrivateKey)
            .setInitialBalance(Hbar.from(10))
            .execute(client)
            .getReceipt(client)
            .accountId;
        Objects.requireNonNull(bobId);

        printAccountInfo(client, "Bob", bobId, bobPrivateKey, bobPublicKey);

        // Alice stakes hbar to bob
        /*AccountUpdateTransaction*/ aliceTx =  new AccountUpdateTransaction()
            .setAccountId(aliceId)
            .setProxyAccountId(bobId);

        /*TransactionResponse*/ aliceTxResp = aliceTx
            .freezeWith(client)
            .sign(alicePrivateKey)
            .execute(client);

        /*TransactionReceipt*/ aliceReceipt = aliceTxResp.getReceipt(client);

        System.out.println("Alice Receipt Status: " + aliceReceipt.status);

        printAccountInfo(client, "Alice", aliceId, alicePrivateKey, alicePublicKey);
        printAccountInfo(client, "Bob", bobId, bobPrivateKey, bobPublicKey);

        client.close();
    }

    private static NodeAddress fetchNode(Client client, int node) {
        NodeAddressBook nodeAddressBook = new AddressBookQuery()
            .setFileId(new FileId(0,0,101))
            .execute(client)
            ;
        return nodeAddressBook.getNodeAddresses().get(node);
    }

    private static void printAccountInfo(Client client, String who, AccountId accountId, PrivateKey privateKey, PublicKey publicKey) throws PrecheckStatusException, TimeoutException {
        AccountInfo info = new AccountInfoQuery().setAccountId(accountId).execute(client);
        System.out.println(
            who + "'s Info: \t"
                + add("Account ID", info.accountId.toString())
                + add("Balance", info.balance.toString())
                + add("Proxy Account ID", info.proxyAccountId != null ? info.proxyAccountId.toString() : "null")
                + add("Proxy Received", info.proxyReceived.toString())
                + "\n"
                + add("\tPrivate Key", privateKey.toString())
                + "\n"
                + add("\tPublic Key", publicKey.toString())
                + "\n"
        );
    }

    private static String add(String msg, String data) {
        return msg + ": " + data + " ";
    }
}
