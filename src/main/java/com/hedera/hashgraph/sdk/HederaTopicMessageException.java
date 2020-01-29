package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;

import java.time.Instant;

/**
 * A {@link HederaStatusException}, thrown on error when an invalid message is received by a MirrorConsensusTopicQuery
 * response.
 */
public class HederaTopicMessageException extends Exception implements HederaThrowable {
    /**
     * The topic where an invalid message was received.
     */
    public final ConsensusTopicId topicId;

    /**
     * The consensus timestamp of the invalid message received.
     */
    public final Instant consensusTimestamp;

    public HederaTopicMessageException(ConsensusTopicId topicId, Instant consensusTimestamp) {
        this.topicId = topicId;
        this.consensusTimestamp = consensusTimestamp;
    }

    @Override
    public String getMessage() {
        return "message received on topic " + topicId + " with consensus timestamp " + consensusTimestamp
            + "is not the next valid message on this topic";
    }
}
