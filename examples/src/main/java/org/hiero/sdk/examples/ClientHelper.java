// SPDX-License-Identifier: Apache-2.0
package org.hiero.sdk.examples;

import java.util.HashMap;
import java.util.List;
import org.hiero.sdk.AccountId;
import org.hiero.sdk.Client;
import org.hiero.sdk.Hbar;

public class ClientHelper {

    public static final String LOCAL_NETWORK_NAME = "localhost";

    private static final String LOCAL_CONSENSUS_NODE_ENDPOINT = "127.0.0.1:50211";

    private static final String LOCAL_MIRROR_NODE_GRPC_ENDPOINT = "127.0.0.1:5600";

    private static final AccountId LOCAL_CONSENSUS_NODE_ACCOUNT_ID = new AccountId(3);

    public static Client forName(String network) throws InterruptedException {
        Client client;

        if (network.equals(LOCAL_NETWORK_NAME)) {
            client = forLocalNetwork();
        } else {
            client = Client.forName(network);
        }
        return client.setDefaultMaxTransactionFee(new Hbar(50));
    }

    public static Client forLocalNetwork() throws InterruptedException {
        var network = new HashMap<String, AccountId>();
        network.put(LOCAL_CONSENSUS_NODE_ENDPOINT, LOCAL_CONSENSUS_NODE_ACCOUNT_ID);

        return Client.forNetwork(network).setMirrorNetwork(List.of(LOCAL_MIRROR_NODE_GRPC_ENDPOINT));
    }
}