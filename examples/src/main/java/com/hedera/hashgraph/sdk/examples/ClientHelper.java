/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2023 - 2024 Hedera Hashgraph, LLC
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

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import java.util.HashMap;
import java.util.List;

public class ClientHelper {

    public static final String LOCAL_NETWORK_NAME = "localhost";

    private static final String LOCAL_CONSENSUS_NODE_ENDPOINT = "127.0.0.1:50211";

    private static final String LOCAL_MIRROR_NODE_GRPC_ENDPOINT = "127.0.0.1:5600";

    private static final AccountId LOCAL_CONSENSUS_NODE_ACCOUNT_ID = new AccountId(3);

    public static Client forName(String network) throws InterruptedException {
        if (network.equals(LOCAL_NETWORK_NAME)) {
            return forLocalNetwork();
        } else {
            return Client.forName(network);
        }
    }

    public static Client forLocalNetwork() throws InterruptedException {
        var network = new HashMap<String, AccountId>();
        network.put(LOCAL_CONSENSUS_NODE_ENDPOINT, LOCAL_CONSENSUS_NODE_ACCOUNT_ID);

        return Client.forNetwork(network)
            .setMirrorNetwork(List.of(LOCAL_MIRROR_NODE_GRPC_ENDPOINT));
    }
}
