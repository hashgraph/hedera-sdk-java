/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2024 Hedera Hashgraph, LLC
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
package com.hedera.hashgraph.tck.methods.sdk;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.tck.annotation.JSONRPC2Method;
import com.hedera.hashgraph.tck.annotation.JSONRPC2Service;
import com.hedera.hashgraph.tck.methods.AbstractJSONRPC2Service;
import com.hedera.hashgraph.tck.methods.sdk.param.SetupParams;
import com.hedera.hashgraph.tck.methods.sdk.response.SetupResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SdkService for managing the {@link Client} setup and reset
 */
@JSONRPC2Service
public class SdkService extends AbstractJSONRPC2Service {
    // this is shared state to all requests so there could be race conditions
    // although the tck driver would not call these methods in such way
    private Client client;

    @JSONRPC2Method("setup")
    public SetupResponse setup(final SetupParams params) throws Exception {
        String clientType;
        if (params.getNodeIp().isPresent()
                && params.getNodeAccountId().isPresent()
                && params.getMirrorNetworkIp().isPresent()) {
            // Custom client setup
            Map<String, AccountId> node = new HashMap<>();
            var nodeId = AccountId.fromString(params.getNodeAccountId().get());
            node.put(params.getNodeIp().get(), nodeId);
            client = Client.forNetwork(node);
            clientType = "custom";
            client.setMirrorNetwork(List.of(params.getMirrorNetworkIp().get()));
        } else {
            // Default to testnet
            client = Client.forTestnet();
            clientType = "testnet";
        }

        client.setOperator(
                AccountId.fromString(params.getOperatorAccountId()),
                PrivateKey.fromString(params.getOperatorPrivateKey()));
        return new SetupResponse("Successfully setup " + clientType + " client.");
    }

    @JSONRPC2Method("reset")
    public SetupResponse reset() {
        client = null;
        return new SetupResponse("");
    }

    public Client getClient() {
        return this.client;
    }
}
