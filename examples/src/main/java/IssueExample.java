/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2023 Hedera Hashgraph, LLC
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

import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public final class IssueExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(
        Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(
        Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private IssueExample() {
    }

    public static void main(String[] args) throws PrecheckStatusException, TimeoutException, InterruptedException {
        initParticularNodeAndPingIt();
        initParticularNodeAndPingIt2();
//        TLSWithoutNodeAddresses();
        TLSWithoutNodeAddresses2();
    }

    /*
    From issue:
    "There are certain times where we want to send a transaction to a particular node.
    For example to understand if a particular node is having problems,
    sending a test query to that node is something we are required to do."

    Comment:
    "Here is a first example how to ping particular node."
    */
    public static void initParticularNodeAndPingIt() throws PrecheckStatusException, TimeoutException {
        @Var Map<String, AccountId> networkMap = new HashMap<>();
        networkMap.put("0.testnet.hedera.com:50211", new AccountId(3));

        Client client = Client.forNetwork(networkMap);

        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        var network = client.getNetwork();
        var nodes = new ArrayList<>(network.values());

        var node = nodes.get(0);

        client.setMaxNodeAttempts(1);
        client.ping(node);
    }

    // Here is a second example how to ping particular node (fetch specific node from the network map and use only it).
    public static void initParticularNodeAndPingIt2()
        throws PrecheckStatusException, TimeoutException, InterruptedException {
        Client client = Client.forName(HEDERA_NETWORK);
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        var network = client.getNetwork();

        Entry<String, AccountId> firstEntry = network.entrySet().iterator().next();
        Map<String, AccountId> specificNode = Map.ofEntries(firstEntry);

        client.setNetwork(specificNode);

        client.pingAll();
    }

    /*
    From issue:
    "There are some very big downsides to this approach: for starters, it does not support TLS.
    That is because no attempt to load the address book is loaded, as that path is not executed.
    Client provides a setTransportSecurity() flag but this appears to have no effect."

    Comment:
    "There is no `NodeAddress` instance created in `Network#forNetwork` and no call for `Node#setAddressBookEntry` is made.
    Hence `certHash` is null and we're getting an exception in example below."
     */
    public static void TLSWithoutNodeAddresses() throws PrecheckStatusException, TimeoutException, InterruptedException {
        @Var Map<String, AccountId> networkMap = new HashMap<>();
        networkMap.put("0.testnet.hedera.com:50211", new AccountId(3));

        Client client = Client.forNetwork(networkMap).setTransportSecurity(true).setVerifyCertificates(true);

        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        var network = client.getNetwork();
        var nodes = new ArrayList<>(network.values());

        var node = nodes.get(0);

        client.setMaxNodeAttempts(1);
        client.ping(node);
    }

    // However here we can set network map with only specific nodes populated from address book and it would work.
    public static void TLSWithoutNodeAddresses2() throws PrecheckStatusException, TimeoutException, InterruptedException {
        Client client = Client.forName(HEDERA_NETWORK).setTransportSecurity(true).setVerifyCertificates(true);
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        var network = client.getNetwork();

        Entry<String, AccountId> firstEntry = network.entrySet().iterator().next();
        Map<String, AccountId> specificNode = Map.ofEntries(firstEntry);

        client.setNetwork(specificNode);

        client.pingAll();
    }

}
