package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.errorprone.annotations.Var;
import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.mirror.ConsensusTopicResponse;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

public final class TopicMessage {
    public final Instant consensusTimestamp;

    public final byte[] contents;

    public final byte[] runningHash;

    public final long sequenceNumber;

    @Nullable
    public final TopicMessageChunk[] chunks;

    @Nullable
    public final TransactionId transactionId;

    TopicMessage(
        Instant lastConsensusTimestamp,
        byte[] message,
        byte[] lastRunningHash,
        long lastSequenceNumber,
        @Nullable TopicMessageChunk[] chunks,
        @Nullable TransactionId transactionId
    ) {
        this.consensusTimestamp = lastConsensusTimestamp;
        this.contents = message;
        this.runningHash = lastRunningHash;
        this.sequenceNumber = lastSequenceNumber;
        this.chunks = chunks;
        this.transactionId = transactionId;
    }

    static TopicMessage ofSingle(ConsensusTopicResponse response) {
        return new TopicMessage(
            InstantConverter.fromProtobuf(response.getConsensusTimestamp()),
            response.getMessage().toByteArray(),
            response.getRunningHash().toByteArray(),
            response.getSequenceNumber(),
            new TopicMessageChunk[]{new TopicMessageChunk(response)},
            response.hasChunkInfo() && response.getChunkInfo().hasInitialTransactionID() ?
                TransactionId.fromProtobuf(response.getChunkInfo().getInitialTransactionID()) :
                null
        );
    }

    static TopicMessage ofMany(List<ConsensusTopicResponse> responses) {
        // response should be in the order of oldest to newest (not chunk order)
        var chunks = new TopicMessageChunk[responses.size()];
        @Var TransactionId transactionId = null;
        var contents = new ByteString[responses.size()];
        @Var long totalSize = 0;

        for (ConsensusTopicResponse r : responses) {
            if (transactionId == null && r.getChunkInfo().hasInitialTransactionID()) {
                transactionId = TransactionId.fromProtobuf(r.getChunkInfo().getInitialTransactionID());
            }

            int index = r.getChunkInfo().getNumber() - 1;

            chunks[index] = new TopicMessageChunk(r);
            contents[index] = r.getMessage();
            totalSize += r.getMessage().size();
        }

        var wholeMessage = ByteBuffer.allocate((int) totalSize);

        for (var content : contents) {
            wholeMessage.put(content.asReadOnlyByteBuffer());
        }

        var lastReceived = responses.get(responses.size() - 1);

        return new TopicMessage(
            InstantConverter.fromProtobuf(lastReceived.getConsensusTimestamp()),
            wholeMessage.array(),
            lastReceived.getRunningHash().toByteArray(),
            lastReceived.getSequenceNumber(),
            chunks,
            transactionId
        );
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("consensusTimestamp", consensusTimestamp)
            .add("contents", new String(contents, StandardCharsets.UTF_8))
            .add("runningHash", runningHash)
            .add("sequenceNumber", sequenceNumber)
            .toString();
    }
}
