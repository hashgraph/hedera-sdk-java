package com.hedera.hashgraph.sdk.mirror;

import com.hedera.hashgraph.proto.mirror.ConsensusTopicResponse;
import com.hedera.hashgraph.sdk.TimestampHelper;

import java.time.Instant;

public final class MirrorConsensusMessageChunk {
    public final Instant consensusTimestamp;

    public final long contentSize;

    public final byte[] runningHash;

    public final long sequenceNumber;

    MirrorConsensusMessageChunk(
        ConsensusTopicResponse response
    ) {
        consensusTimestamp = TimestampHelper.timestampTo(response.getConsensusTimestamp());
        contentSize = response.getMessage().size();
        runningHash = response.getRunningHash().toByteArray();
        sequenceNumber = response.getSequenceNumber();
    }
}
