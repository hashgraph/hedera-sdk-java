// SPDX-License-Identifier: Apache-2.0
package org.hiero.tck.methods.sdk;

import org.hiero.sdk.AccountId;
import org.hiero.sdk.Client;
import org.hiero.sdk.PrivateKey;
import org.hiero.tck.annotation.JSONRPC2Method;
import org.hiero.tck.annotation.JSONRPC2Service;
import org.hiero.tck.methods.AbstractJSONRPC2Service;
import org.hiero.tck.methods.sdk.param.SetupParams;
import org.hiero.tck.methods.sdk.response.SetupResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

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
        var clientExecutor = Executors.newFixedThreadPool(16);
        String clientType;
        if (params.getNodeIp().isPresent()
                && params.getNodeAccountId().isPresent()
                && params.getMirrorNetworkIp().isPresent()) {
            // Custom client setup
            Map<String, AccountId> node = new HashMap<>();
            var nodeId = AccountId.fromString(params.getNodeAccountId().get());
            node.put(params.getNodeIp().get(), nodeId);
            client = Client.forNetwork(node, clientExecutor);
            clientType = "custom";
            client.setMirrorNetwork(List.of(params.getMirrorNetworkIp().get()));
        } else {
            // Default to testnet
            client = Client.forTestnet(clientExecutor);
            clientType = "testnet";
        }

        client.setOperator(
                AccountId.fromString(params.getOperatorAccountId()),
                PrivateKey.fromString(params.getOperatorPrivateKey()));
        return new SetupResponse("Successfully setup " + clientType + " client.");
    }

    @JSONRPC2Method("reset")
    public SetupResponse reset() throws Exception {
        client.close();
        client = null;
        return new SetupResponse("");
    }

    public Client getClient() {
        return this.client;
    }
}
