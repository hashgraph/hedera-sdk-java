package com.hedera.hashgraph.sdk.mirror;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.proto.mirror.ConsensusTopicResponse;
import com.hedera.hashgraph.sdk.TimestampHelper;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class MirrorConsensusTopicResponse {
    public final Instant consensusTimestamp;

    public final byte[] message;

    public final byte[] runningHash;

    public final long sequenceNumber;

    public final MirrorConsensusMessageChunk[] chunks;

    MirrorConsensusTopicResponse(
        Instant lastConsensusTimestamp,
        byte[] message,
        byte[] lastRunningHash,
        long lastSequenceNumber,
        MirrorConsensusMessageChunk[] chunks
    ) {
        this.consensusTimestamp = lastConsensusTimestamp;
        this.message = message;
        this.runningHash = lastRunningHash;
        this.sequenceNumber = lastSequenceNumber;
        this.chunks = chunks;
    }

    static MirrorConsensusTopicResponse ofSingle(ConsensusTopicResponse response) {
        return new MirrorConsensusTopicResponse(
            TimestampHelper.timestampTo(response.getConsensusTimestamp()),
            response.getMessage().toByteArray(),
            response.getRunningHash().toByteArray(),
            response.getSequenceNumber(),
            new MirrorConsensusMessageChunk[]{new MirrorConsensusMessageChunk(response)}
        );
    }

    static MirrorConsensusTopicResponse ofMany(List<ConsensusTopicResponse> responses) {
        // response should be in the order of oldest to newest (not chunk order)
        MirrorConsensusMessageChunk[] chunks = new MirrorConsensusMessageChunk[responses.size()];
        ByteString[] contents = new ByteString[responses.size()];
        long totalSize = 0;

        for (ConsensusTopicResponse r : responses) {
            int index = r.getChunkInfo().getNumber() - 1;

            chunks[index] = new MirrorConsensusMessageChunk(r);
            contents[index] = r.getMessage();
            totalSize += r.getMessage().size();
        }

        ByteBuffer wholeMessage = ByteBuffer.allocate((int) totalSize);

        for (ByteString content : contents) {
            wholeMessage.put(content.asReadOnlyByteBuffer());
        }

        ConsensusTopicResponse lastReceived = responses.get(responses.size() - 1);

        return new MirrorConsensusTopicResponse(
            TimestampHelper.timestampTo(lastReceived.getConsensusTimestamp()),
            wholeMessage.array(),
            lastReceived.getRunningHash().toByteArray(),
            lastReceived.getSequenceNumber(),
            chunks
        );
    }

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
