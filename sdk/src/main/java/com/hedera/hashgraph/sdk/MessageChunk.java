package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.mirror.ConsensusTopicResponse;
import org.threeten.bp.Instant;

final class MessageChunk {
    public final Instant consensusTimestamp;
    public final long contentSize;
    public final byte[] runningHash;
    public final long sequenceNumber;

    MessageChunk(ConsensusTopicResponse response) {
        consensusTimestamp = InstantConverter.fromProtobuf(response.getConsensusTimestamp());
        contentSize = response.getMessage().size();
        runningHash = response.getRunningHash().toByteArray();
        sequenceNumber = response.getSequenceNumber();
    }
}

