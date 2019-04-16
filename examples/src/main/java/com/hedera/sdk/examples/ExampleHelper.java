package com.hedera.sdk.examples;

import com.hedera.sdk.account.AccountId;
import com.hedera.sdk.Client;
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Map;
import java.util.Objects;

public class ExampleHelper {
    private static Dotenv getEnv() {
        // Load configuration from the environment or a $projectRoot/.env file, if present
        // See .env.sample for an example of what it is looking for
        return Dotenv.load();
    }

    public static AccountId getNodeId() {
        return AccountId.fromString(Objects.requireNonNull(getEnv().get("NODE_ID")));
    }

    public static AccountId getOperatorId() {
        return AccountId.fromString(Objects.requireNonNull(getEnv().get("OPERATOR_ID")));
    }

    public static Ed25519PrivateKey getOperatorKey() {
        return Ed25519PrivateKey.fromString(Objects.requireNonNull(getEnv().get("OPERATOR_KEY")));
    }

    public static Client createHederaClient() {
        // To connect to a network with more nodes, add additional entries to the network map
        var nodeAddress = Objects.requireNonNull(getEnv().get("NODE_ADDRESS"));
        var client = new Client(Map.of(getNodeId(), nodeAddress));

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(getOperatorId(), getOperatorKey());

        return client;
    }
}
