package com.hedera.sdk.examples;

import com.hedera.sdk.account.AccountId;
import com.hedera.sdk.Client;
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Map;
import java.util.Objects;

class ExampleHelper {
    static Client createHederaClient() {
        // Load configuration from the environment or a $projectRoot/.env file, if present
        // See .env.sample for an example of what it is looking for
        var env = Dotenv.load();

        var operatorId = AccountId.fromString(Objects.requireNonNull(env.get("OPERATOR_ID")));
        var operatorKey = Ed25519PrivateKey.fromString(Objects.requireNonNull(env.get("OPERATOR_KEY")));

        var nodeId = AccountId.fromString(Objects.requireNonNull(env.get("NODE_ID")));
        var nodeAddress = Objects.requireNonNull(env.get("NODE_ADDRESS"));

        // To connect to a network with more nodes, add additional entries to the network map
        var client = new Client(Map.of(nodeId, nodeAddress));

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(operatorId, operatorKey);

        return client;
    }
}
