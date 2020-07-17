package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.errorprone.annotations.Var;
import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.mirror.ConsensusTopicResponse;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class TopicResponse {
    public final Instant consensusTimestamp;

    public final byte[] message;

    public final byte[] runningHash;

    public final long sequenceNumber;

    @Nullable
    public final MessageChunk[] chunks;

    TopicResponse(
        Instant lastConsensusTimestamp,
        byte[] message,
        byte[] lastRunningHash,
        long lastSequenceNumber,
        MessageChunk[] chunks
    ) {
        this.consensusTimestamp = lastConsensusTimestamp;
        this.message = message;
        this.runningHash = lastRunningHash;
        this.sequenceNumber = lastSequenceNumber;
        this.chunks = chunks;
    }

    static TopicResponse ofSingle(ConsensusTopicResponse response) {
        return new TopicResponse(
            InstantConverter.fromProtobuf(response.getConsensusTimestamp()),
            response.getMessage().toByteArray(),
            response.getRunningHash().toByteArray(),
            response.getSequenceNumber(),
            new MessageChunk[]{new MessageChunk(response)}
        );
    }

    static TopicResponse ofMany(List<ConsensusTopicResponse> responses) {
        // response should be in the order of oldest to newest (not chunk order)
        @Var var chunks = new MessageChunk[responses.size()];
        var contents = new ByteString[responses.size()];
        @Var long totalSize = 0;

        for (ConsensusTopicResponse r : responses) {
            int index = r.getChunkInfo().getNumber() - 1;

            chunks[index] = new MessageChunk(r);
            contents[index] = r.getMessage();
            totalSize += r.getMessage().size();
        }

        var wholeMessage = ByteBuffer.allocate((int) totalSize);

        for (var content : contents) {
            wholeMessage.put(content.asReadOnlyByteBuffer());
        }

        var lastReceived = responses.get(responses.size() - 1);

        return new TopicResponse(
            InstantConverter.fromProtobuf(lastReceived.getConsensusTimestamp()),
            wholeMessage.array(),
            lastReceived.getRunningHash().toByteArray(),
            lastReceived.getSequenceNumber(),
            chunks
        );
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
