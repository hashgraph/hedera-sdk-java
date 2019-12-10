package com.hedera.hashgraph.sdk.examples.advanced;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.Transaction;
import com.hedera.hashgraph.sdk.consensus.ConsensusClient;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicCreateTransaction;
import com.hedera.hashgraph.sdk.consensus.TopicId;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;

public final class ConsensusPubSub {
    private ConsensusPubSub() { }

    public static void main(String[] args) throws InterruptedException, HederaException {
        final ConsensusClient consensusClient = new ConsensusClient("34.66.214.12:6552");

        final Transaction topicTxn = new ConsensusTopicCreateTransaction(ExampleHelper.createHederaClient())
            .build();

        topicTxn.execute();

        final TopicId topicId = topicTxn.queryReceipt().getTopicId();

        consensusClient.subscribe(topicId, message -> {
            System.out.println("received topic message: " + message.getMessageString());
        });

        // keep the main thread from exiting because the listeners run on daemon threads
        for (;;) {
            Thread.sleep(2500);
        }
    }
}
