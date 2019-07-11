package com.hedera.hashgraph.sdk.examples.consensus;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;

import java.time.Instant;

public final class CreateTopic {
    private CreateTopic() { }

    public static void main(String[] args) throws HederaException {
        var client = ExampleHelper.createHederaClient();

        var topicMemo = "TestTopicMemo#" + Instant.now().getEpochSecond();
        var newTopicId = client.createTopic(topicMemo);
        System.out.println("Topic(memo = " + topicMemo + ", Topic ID = " + newTopicId + "): Created");
    }
}
