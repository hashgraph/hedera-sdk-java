package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.hedera.hashgraph.sdk.proto.mirror.ConsensusTopicResponse;
import org.threeten.bp.Instant;

import java.nio.charset.StandardCharsets;

public final class MirrorTopicResponse {
    public final Instant consensusTimestamp;

    public final byte[] message;

    public final byte[] runningHash;

    public final long sequenceNumber;

    MirrorTopicResponse(ConsensusTopicResponse response) {
        this.consensusTimestamp = InstantConverter.fromProtobuf(response.getConsensusTimestamp());
        this.message = response.getMessage().toByteArray();
        this.runningHash = response.getRunningHash().toByteArray();
        this.sequenceNumber = response.getSequenceNumber();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("consensusTimestamp", consensusTimestamp)
            .add("message", new String(message, StandardCharsets.UTF_8))
            .add("runningHash", runningHash)
            .add("sequenceNumber", sequenceNumber)
            .toString();
    }
}
