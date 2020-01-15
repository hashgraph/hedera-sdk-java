package com.hedera.hashgraph.sdk.examples.advanced;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Objects;

/**
 * Base class for advanced examples.
 * Responsible for setting up the HAPI Client based on environment variable settings.
 */
class AdvancedExample {
    private Client hapiClient;

    public AdvancedExample() {
        setupHapiClient();
    }

    public Client getHapiClient() {
        return hapiClient;
    }

    private void setupHapiClient() {
        // The Hedera Hashgrpah node's IP address, port, and account ID.
        AccountId nodeID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("NODE_ID")));
        String nodeAddress = Objects.requireNonNull(Dotenv.load().get("NODE_ADDRESS"));

        // Transaction payer's account ID and ED25519 private key.
        AccountId payerId = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
        Ed25519PrivateKey payerPrivateKey =
            Ed25519PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

        // Interface used to publish messages on the HCS topic.
        hapiClient = new Client(nodeID, nodeAddress);

        // Defaults the operator account ID and key such that all generated transactions will be paid for by this
        // account and be signed by this key
        hapiClient.setOperator(payerId, payerPrivateKey);
    }
}
