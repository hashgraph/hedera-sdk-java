package com.hedera.hashgraph.sdk.examples.consensus;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.consensus.UpdateTopicTransaction;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;

import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public final class SimpleWorkflow {

    private SimpleWorkflow() {
    }

    public static void main(String[] args) throws HederaException {
        var client = ExampleHelper.createHederaClient();
        var newKey = Ed25519PrivateKey.generate();

        var topicId = client.createTopic("test1");
        System.out.println("Created topic: " + topicId);

        var topicInfo = client.getTopicInfo(topicId);
        System.out.println("Retrieved topic info: " + topicInfo);

        var transactionId = client.submitMessage(topicId, "Message #1".getBytes(Charset.forName("UTF-8")));
        System.out.println("Submitted message: " + transactionId);

        var newAccount = client.createAccount(newKey.getPublicKey(), 10);
        System.out.println("Created new account: " + newAccount);

        new UpdateTopicTransaction(client)
            .setAdminKey(newKey.getPublicKey())
            .setCreationTime(Instant.now().minus(1, ChronoUnit.HOURS))
            .setExpirationDuration(Duration.ofHours(2))
            .setSubmitKey(newKey.getPublicKey())
            .setTopicId(topicId)
            .setTopicMemo("test2")
            .executeForRecord();
        System.out.println("Updated topic: ");

        client.setOperator(newAccount, newKey);
        transactionId = client.submitMessage(topicId, "Message #2".getBytes(Charset.forName("UTF-8")));
        System.out.println("Submitted message: " + transactionId);

        transactionId = client.deleteTopic(topicId);
        System.out.println("Deleted topic: " + transactionId);
    }
}
