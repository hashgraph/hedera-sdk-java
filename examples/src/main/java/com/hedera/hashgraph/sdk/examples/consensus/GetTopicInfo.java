package com.hedera.hashgraph.sdk.examples.consensus;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.consensus.TopicId;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;
import org.bouncycastle.util.encoders.Hex;

public final class GetTopicInfo {
    private GetTopicInfo() { }

    public static void main(String[] args) throws HederaException {
        var client = ExampleHelper.createHederaClient();
        var topicId = TopicId.fromString("0.0.1001" /* YOUR_TOPIC_ID_HERE */);
        var topicInfo = client.getTopicInfo(topicId);
        System.out.println("Topic " + topicId + ":");
        System.out.println("  sequence number: " + topicInfo.getSequenceNumber());
        System.out.println("  runningHash: " + Hex.toHexString(topicInfo.getRunningHash()));
    }
}
