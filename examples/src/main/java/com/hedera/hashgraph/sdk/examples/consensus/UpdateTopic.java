package com.hedera.hashgraph.sdk.examples.consensus;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.consensus.UpdateTopicTransaction;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public final class UpdateTopic {

    private UpdateTopic() {
    }

    public static void main(String[] args) throws HederaException {
        var client = ExampleHelper.createHederaClient();
        var adminKey = Ed25519PrivateKey.generate().getPublicKey();
        var submitKey = Ed25519PrivateKey.generate().getPublicKey();

        var topicId = client.createTopic("test1");
        System.out.println("Topic created: " + topicId);

        new UpdateTopicTransaction(client)
            .setAdminKey(adminKey)
            .setCreationTime(Instant.now().minus(1, ChronoUnit.HOURS))
            .setExpirationDuration(Duration.ofHours(2))
            .setSubmitKey(submitKey)
            .setTopicId(topicId)
            .setTopicMemo("test2")
            .executeForRecord();

        System.out.println("Topic updated");
    }
}
