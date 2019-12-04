package com.hedera.hashgraph.sdk.consensus;

@FunctionalInterface
public interface TopicListener {
    void onMessage(ConsensusMessage message);
}
