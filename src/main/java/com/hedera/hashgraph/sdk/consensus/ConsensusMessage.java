package com.hedera.hashgraph.sdk.consensus;

import com.hedera.hashgraph.sdk.Experimental;
import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.proto.mirror.ConsensusTopicResponse;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;

public class ConsensusMessage {
    public final ConsensusTopicId topicId;

    public final Instant consensusTimestamp;

    public final byte[] message;

    public final byte[] runningHash;

    public final long sequenceNumber;

    ConsensusMessage(ConsensusTopicId topicId, ConsensusTopicResponse message) {
        Experimental.requireFor(ConsensusMessage.class.getName());

        this.topicId = topicId;
        this.consensusTimestamp = TimestampHelper.timestampTo(message.getConsensusTimestamp());
        this.message = message.getMessage().toByteArray();
        this.runningHash = message.getRunningHash().toByteArray();
        this.sequenceNumber = message.getSequenceNumber();
    }

    public String getMessageString() {
        return new String(message, StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return "ConsensusMessage{"
            + "topicId=" + topicId
            + ", consensusTimestamp=" + consensusTimestamp
            + ", message=" + Arrays.toString(message)
            + ", runningHash=" + Arrays.toString(runningHash)
            + ", sequenceNumber=" + sequenceNumber
            + '}';
    }
}
