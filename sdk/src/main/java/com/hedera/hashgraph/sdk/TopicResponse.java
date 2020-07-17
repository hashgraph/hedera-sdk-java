package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.hedera.hashgraph.sdk.proto.mirror.ConsensusTopicResponse;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;

public final class TopicResponse {
    public final Instant consensusTimestamp;

    public final byte[] message;

    public final byte[] runningHash;

    public final long sequenceNumber;

    @Nullable
    public final MessageChunkInfo chunkInfo;

    TopicResponse(ConsensusTopicResponse response) {
        this.consensusTimestamp = InstantConverter.fromProtobuf(response.getConsensusTimestamp());
        this.message = response.getMessage().toByteArray();
        this.runningHash = response.getRunningHash().toByteArray();
        this.sequenceNumber = response.getSequenceNumber();

        if (response.hasChunkInfo()) {
            var transactionId = TransactionId.fromProtobuf(response.getChunkInfo().getInitialTransactionID());
            var total = response.getChunkInfo().getTotal();
            var number = response.getChunkInfo().getNumber();
            this.chunkInfo = new MessageChunkInfo(transactionId, total, number);
        } else {
            this.chunkInfo = null;
        }
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
