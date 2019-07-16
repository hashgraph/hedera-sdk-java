package com.hedera.hashgraph.sdk.examples.consensus;

import java.nio.charset.Charset;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;

public final class SimpleWorkflow {

	private SimpleWorkflow() {
    }

    public static void main(String[] args) throws HederaException {
        var client = ExampleHelper.createHederaClient();

        var memo = "My topic";
        var message = "Message #1";
        var topicId = client.createTopic(memo);
        System.out.println("Created topic: " + topicId);

        var topicInfo = client.getTopicInfo(topicId);
        System.out.println("Retrieved topic info: " + topicInfo);

        var transactionId = client.submitMessage(topicId, message.getBytes(Charset.forName("UTF-8")));
        System.out.println("Submitted message: " + transactionId);

        transactionId = client.deleteTopic(topicId);
        System.out.println("Deleted topic: " + transactionId);
    }
}
