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

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.NetworkName;
import com.hedera.hashgraph.sdk.PrivateKey;
import io.github.cdimascio.dotenv.Dotenv;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * How to construct and configure a client in different ways.
 * <p>
 * A client has a network and an operator.
 * <p>
 * A Hedera network is made up of nodes -- individual servers who participate
 * in the process of reaching consensus on the order and validity of transactions
 * on the network. Three networks you likely know of are previewnet, testnet, and mainnet.
 * <p>
 * For the purpose of connecting to it, each node has an IP address or URL and a port number.
 * Each node also has an AccountId used to refer to that node for several purposes,
 * including the paying of fees to that node when a client submits requests to it.
 * <p>
 * You can configure what network you want a client to use -- in other words, you can specify
 * a list of URLs and port numbers with associated account IDs, and
 * when that client is used to execute queries and transactions, the client will
 * submit requests only to nodes in that list.
 * <p>
 * A Client has an operator, which has an AccountId and a PublicKey, and which can
 * sign requests. A client's operator can also be configured.
 */
class ConstructClientExample {

    /*
     * See .env.sample in the examples folder root for how to specify values below
     * or set environment variables with the same names.
     */

    /**
     * Path to .json config file.
     */
    // TODO: its not defined in anyway in .env.sample or in docs -- need to define or write an additional doc here
    @Nullable
    private static final String CONFIG_FILE = Dotenv.load().get("CONFIG_FILE");

    private static final String HEDERA_NETWORK = "testnet";

    public static void main(String[] args) throws Exception {
        System.out.println("Construct Client Example Start!");

        /*
         * Here's the simplest way to construct a client.
         * These clients' networks are filled with default lists of nodes that are baked into the SDK.
         * Their operators are not yet set, and trying to use them now will result in exceptions.
         */
        Client testnetClient = Client.forTestnet();
        Client previewnetClient = Client.forPreviewnet();
        Client mainnetClient = Client.forMainnet();

        /*
         * We can also construct a client for testnet, previewnet or mainnet depending on the value of a
         * network name string. If, for example, the input string equals "testnet", this client will be
         * configured to connect to testnet.
         */
        Client namedNetworkClient = Client.forName(HEDERA_NETWORK);

        // Let's set the operator on testnetClient.
        // (The AccountId and PrivateKey here are fake, this is just an example.)
        testnetClient.setOperator(
            AccountId.fromString("0.0.3"),
            PrivateKey.fromString("302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10")
        );

        // Let's create a client with a custom network.
        Map<String, AccountId> customNetwork = new HashMap<>();
        customNetwork.put("2.testnet.hedera.com:50211", new AccountId(5));
        customNetwork.put("3.testnet.hedera.com:50211", new AccountId(6));
        Client customClient = Client.forNetwork(customNetwork);

        /*
         * Since our customClient's network is in this case a subset of testnet, we should set the
         * network's name to testnet. If we don't do this, checksum validation won't work.
         * See ValidateChecksumExample. You can use customClient.getNetworkName()
         * to check the network name. If not set, it will return null.
         * If you attempt to validate a checksum against a client whose networkName is not set,
         * an IllegalStateException will be thrown.
         */
        customClient.setNetworkName(NetworkName.TESTNET);

        /*
         * Let's generate a client from a config.json file.
         * A config file may specify a network by name, or it may provide a custom network
         * in the form of a list of nodes.
         * The config file should specify the operator, so you can use a client constructed
         * using fromConfigFile() immediately.
         */
        if (CONFIG_FILE != null) {
            Client configClient = Client.fromConfigFile(CONFIG_FILE);
            configClient.close();
        }

        // Always close a client when you're done with it.
        testnetClient.close();
        previewnetClient.close();
        mainnetClient.close();
        namedNetworkClient.close();
        customClient.close();

        System.out.println("Construct Client Example Complete!");
    }
}
