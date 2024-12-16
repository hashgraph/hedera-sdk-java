// SPDX-License-Identifier: Apache-2.0
package com.hiero.sdk;

import com.hiero.sdk.proto.mirror.ConsensusTopicResponse;
import java.time.Instant;

/**
 * A chunk of the topic message.
 */
final class TopicMessageChunk {
    public final Instant consensusTimestamp;
    public final long contentSize;
    public final byte[] runningHash;
    public final long sequenceNumber;

    /**
     * Create a topic message chunk from a protobuf.
     *
     * @param response                  the protobuf
     */
    TopicMessageChunk(ConsensusTopicResponse response) {
        consensusTimestamp = InstantConverter.fromProtobuf(response.getConsensusTimestamp());
        contentSize = response.getMessage().size();
        runningHash = response.getRunningHash().toByteArray();
        sequenceNumber = response.getSequenceNumber();
    }
}
