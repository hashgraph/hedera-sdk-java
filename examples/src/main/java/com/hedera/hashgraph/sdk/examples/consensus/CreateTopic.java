package com.hedera.hashgraph.sdk.examples.consensus;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.consensus.CreateTopicTransaction;
import com.hedera.hashgraph.sdk.crypto.Key;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public final class CreateTopic {

    private CreateTopic() {
    }

    public static void main(String[] args) throws HederaException {
        var client = ExampleHelper.createHederaClient();
        var adminKey = Key.fromString(Ed25519PrivateKey.generate().getPublicKey().toString());
        var submitKey = Key.fromString(Ed25519PrivateKey.generate().getPublicKey().toString());

        var record = new CreateTopicTransaction(client)
            .setAdminKey(adminKey)
            .setCreationTime(Instant.now().minus(1, ChronoUnit.HOURS))
            .setExpirationDuration(Duration.ofHours(2))
            .setSubmitKey(submitKey)
            .setTopicMemo("test")
            .executeForRecord();

        System.out.println("Topic created: " + record.getReceipt().getTopicId());
    }
}
