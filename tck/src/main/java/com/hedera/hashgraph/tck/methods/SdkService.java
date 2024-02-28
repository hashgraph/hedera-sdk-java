package com.hedera.hashgraph.tck.methods;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.tck.common.Status;
import com.hedera.hashgraph.tck.params.SetupParams;
import com.hedera.hashgraph.tck.response.SetupResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class SdkService {
    private Client client;

    public SetupResponse setup(SetupParams params) throws InterruptedException {
        String clientType;
        if (params.nodeIp() != null && params.nodeAccountId() != null && params.mirrorNetworkIp() != null) {
            // Custom client setup
            Map<String, AccountId> node = new HashMap<>();
            node.put(params.nodeIp(), AccountId.fromString(params.nodeAccountId()));
            client = Client.forNetwork(node);
            clientType = "custom";
            client.setMirrorNetwork(List.of(params.mirrorNetworkIp()));
        } else {
            // Default to testnet
            client = Client.forTestnet();
            clientType = "testnet";
        }

        client.setOperator(
                AccountId.fromString(params.operatorAccountId()), PrivateKey.fromString(params.operatorPrivateKey()));
        return new SetupResponse("Successfully setup " + clientType + " client.", Status.SUCCESS.name());
    }

    public SetupResponse reset() {
        client = null;
        return new SetupResponse("", Status.SUCCESS.name());
    }
}
