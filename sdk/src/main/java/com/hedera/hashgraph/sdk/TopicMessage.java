/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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

/**
 * Topic message records.
 */
public final class TopicMessage {
    /**
     * The consensus timestamp of the message in seconds.nanoseconds
     */
    public final Instant consensusTimestamp;
    /**
     * The content of the message
     */
    public final byte[] contents;
    /**
     * The new running hash of the topic that received the message
     */
    public final byte[] runningHash;
    /**
     * The sequence number of the message relative to all other messages
     * for the same topic
     */
    public final long sequenceNumber;
    /**
     * Array of topic message chunks.
     */
    @Nullable
    public final TopicMessageChunk[] chunks;
    /**
     * The transaction id
     */
    @Nullable
    public final TransactionId transactionId;

    /**
     * Constructor.
     *
     * @param lastConsensusTimestamp    the last consensus time
     * @param message                   the message
     * @param lastRunningHash           the last running hash
     * @param lastSequenceNumber        the last sequence number
     * @param chunks                    the array of chunks
     * @param transactionId             the transaction id
     */
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

    /**
     * Create a new topic message from a response protobuf.
     *
     * @param response                  the protobuf response
     * @return                          the new topic message
     */
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

    /**
     * Create a new topic message from a list of response's protobuf.
     *
     * @param responses                 the protobuf response
     * @return                          the new topic message
     */
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
