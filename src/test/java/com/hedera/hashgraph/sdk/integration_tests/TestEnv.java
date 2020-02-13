package com.hedera.hashgraph.sdk.integration_tests;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

import java.util.Objects;

import io.github.cdimascio.dotenv.Dotenv;

public class TestEnv {

    public final AccountId operatorId;
    public final Ed25519PrivateKey operatorKey;
    public final Client client;

    public TestEnv() {
        final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        operatorId = AccountId.fromString(Objects.requireNonNull(
            dotenv.get("OPERATOR_ID"),
            "OPERATOR_ID must be set in environment or .env"));

        operatorKey = Ed25519PrivateKey.fromString(Objects.requireNonNull(
            dotenv.get("OPERATOR_KEY"),
            "OPERATOR_KEY must be set in environment or .env"));

        client = Client.forTestnet().setOperator(operatorId, operatorKey);
    }
}
