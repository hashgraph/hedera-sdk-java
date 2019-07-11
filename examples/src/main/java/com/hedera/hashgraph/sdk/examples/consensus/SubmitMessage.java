package com.hedera.hashgraph.sdk.examples.consensus;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.consensus.TopicId;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;

import java.time.Instant;

public final class SubmitMessage {
    private SubmitMessage() { }

    public static void main(String[] args) throws HederaException {
        var client = ExampleHelper.createHederaClient();

        var topicId = TopicId.fromString("0.0.1001" /* YOUR_TOPIC_ID_HERE */);
        var message = "Message #" + Instant.now().getEpochSecond();
        System.out.println("Writing " + message + " to " + topicId);
        client.submitMessage(topicId, message.getBytes());
    }
}
