package com.hedera.hashgraph.tck.methods.sdk;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.tck.annotation.JSONRPC2Method;
import com.hedera.hashgraph.tck.annotation.JSONRPC2Service;
import com.hedera.hashgraph.tck.exception.HederaException;
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
    // TODO this is shared state to all requests so there could be race condition
    //  although the tck driver would not call these methods in such way
    private Client client;

    // TODO add logger here or in the abstract class

    @JSONRPC2Method("setup")
    public SetupResponse setup(final SetupParams params) throws HederaException {
        String clientType;
        try {
            if (params.getNodeIp() != null
                    && params.getNodeAccountId() != null
                    && params.getMirrorNetworkIp() != null) {
                // Custom client setup
                Map<String, AccountId> node = new HashMap<>();
                node.put(params.getNodeIp(), AccountId.fromString(params.getNodeAccountId()));
                client = Client.forNetwork(node);
                clientType = "custom";
                client.setMirrorNetwork(List.of(params.getMirrorNetworkIp()));
            } else {
                // Default to testnet
                client = Client.forTestnet();
                clientType = "testnet";
            }

            client.setOperator(
                    AccountId.fromString(params.getOperatorAccountId()),
                    PrivateKey.fromString(params.getOperatorPrivateKey()));
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new HederaException(e);
        }
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
