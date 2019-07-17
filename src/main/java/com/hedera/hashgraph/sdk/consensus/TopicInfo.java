package com.hedera.hashgraph.sdk.consensus;

import org.bouncycastle.util.encoders.Hex;

import com.hedera.hashgraph.sdk.proto.*;

public class TopicInfo {
    private final ConsensusGetTopicInfoResponse inner;

    TopicInfo(Response response) {
        if (!response.hasConsensusGetTopicInfo()) {
            throw new IllegalArgumentException("query response was not `ConsensusGetTopicInfoResponse`");
        }
        inner = response.getConsensusGetTopicInfo();
    }

    public TopicId getTopicId() {
        return new TopicId(inner.getTopicIDOrBuilder());
    }

    public Long getSequenceNumber() {
        return inner.hasTopicState() ? inner.getTopicState().getSequenceNumber() : -1;
    }

    public byte[] getRunningHash() {
        return inner.hasTopicState() ? inner.getTopicState().getRunningHash().toByteArray() : new byte[0];
    }

    @Override
    public String toString() {
        return String.format("sequenceNumber: %d, runningHash: %s", getSequenceNumber(), Hex.toHexString(getRunningHash()));
    }
}
