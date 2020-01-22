package com.hedera.hashgraph.sdk.mirror;

import com.hedera.hashgraph.proto.mirror.ConsensusTopicResponse;
import com.hedera.hashgraph.sdk.TimestampHelper;

import java.time.Instant;
import java.util.Arrays;

public class MirrorConsensusTopicResponse {
    public final Instant consensusTimestamp;

    public final byte[] message;

    public final byte[] runningHash;

    public final long sequenceNumber;

    MirrorConsensusTopicResponse(ConsensusTopicResponse response) {
        this.consensusTimestamp = TimestampHelper.timestampTo(response.getConsensusTimestamp());
        this.message = response.getMessage().toByteArray();
        this.runningHash = response.getRunningHash().toByteArray();
        this.sequenceNumber = response.getSequenceNumber();
    }

    // TODO: Use a standard debug serialization
    @Override
    public String toString() {
        return "ConsensusMessage{"
            + "consensusTimestamp=" + consensusTimestamp
            + ", message=" + Arrays.toString(message)
            + ", runningHash=" + Arrays.toString(runningHash)
            + ", sequenceNumber=" + sequenceNumber
            + '}';
    }
}
