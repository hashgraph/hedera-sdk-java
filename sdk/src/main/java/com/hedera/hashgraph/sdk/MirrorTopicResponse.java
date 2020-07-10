package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.hedera.hashgraph.sdk.proto.mirror.ConsensusTopicResponse;
import org.threeten.bp.Instant;

import java.nio.charset.StandardCharsets;

final class ChunkInfo {
    public TransactionId initialTransactionId;

    public int total;

    public int number;

    public ChunkInfo(TransactionId initialTransactionId, int total, int number) {
        this.initialTransactionId = initialTransactionId;
        this.total = total;
        this.number = number;
    }
}

public final class MirrorTopicResponse {
    public final Instant consensusTimestamp;

    public final byte[] message;

    public final byte[] runningHash;

    public final long sequenceNumber;

    public final ChunkInfo chunkInfo;

    MirrorTopicResponse(ConsensusTopicResponse response) {
        this.consensusTimestamp = InstantConverter.fromProtobuf(response.getConsensusTimestamp());
        this.message = response.getMessage().toByteArray();
        this.runningHash = response.getRunningHash().toByteArray();
        this.sequenceNumber = response.getSequenceNumber();

        if (response.hasChunkInfo()) {
            var transactionId = TransactionId.fromProtobuf(response.getChunkInfo().getInitialTransactionID());
            var total = response.getChunkInfo().getTotal();
            var number = response.getChunkInfo().getNumber();
            this.chunkInfo = new ChunkInfo(transactionId, total, number);
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
