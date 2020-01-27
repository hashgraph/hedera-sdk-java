package com.hedera.hashgraph.sdk.consensus;

import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ConsensusTopicRunningHashTest {

    @Test
    void topicIdOnlyConstructor() {
        ConsensusTopicId topicId = new ConsensusTopicId(1, 2, 3);
        ConsensusTopicRunningHash cut = new ConsensusTopicRunningHash(topicId);
        assertAll(() -> { assertEquals(topicId, cut.topicId); },
            () -> { assertEquals(0, cut.sequenceNumber); },
            () -> { assertArrayEquals(new byte[48], cut.runningHash); }
            );
    }

    @Test
    void getUpdatedRunningHashFirstMessage() {
        // Create ConsensusTopicRunningHash for new topic.
        ConsensusTopicId topicId = new ConsensusTopicId(1, 2, 3);
        ConsensusTopicRunningHash newTopicRunningHash = new ConsensusTopicRunningHash(topicId);
        byte[] message = "first message".getBytes(StandardCharsets.UTF_8);
        Instant consensusTimestamp = Instant.ofEpochSecond(1234567890);
        String expectedNextRunningHashHexString =
            "df4162eebdb4aae3664ced2a764068508c576c6787561f44c4e4ce89ada124ba9c9db77cdc08057dd9b18759904e58aa";

        ConsensusTopicRunningHash nextTopicRunningHash = newTopicRunningHash.getUpdatedRunningHash(message,
            consensusTimestamp);

        assertAll(
            // Assert original unchanged.
            () -> { assertEquals(topicId, newTopicRunningHash.topicId); },
            () -> { assertEquals(0L, newTopicRunningHash.sequenceNumber); },
            () -> { assertArrayEquals(new byte[48], newTopicRunningHash.runningHash); },

            // Assert next running hash.
            () -> { assertEquals(topicId, nextTopicRunningHash.topicId); },
            () -> { assertEquals(1L, nextTopicRunningHash.sequenceNumber); },
            () -> { assertEquals(expectedNextRunningHashHexString, Hex.toHexString(nextTopicRunningHash.runningHash)); }
        );
    }

    @Test
    void getUpdatedRunningHashLaterMessage() {
        // Create ConsensusTopicRunningHash for an existing topic.
        ConsensusTopicId topicId = new ConsensusTopicId(1, 2, 3);
        String originalRunningHashHexString =
            "df4162eebdb4aae3664ced2a764068508c576c6787561f44c4e4ce89ada124ba9c9db77cdc08057dd9b18759904e58aa";
        Instant originalConsensusTimestamp = Instant.ofEpochSecond(1234567891L);
        ConsensusTopicRunningHash original = new ConsensusTopicRunningHash(topicId, 123L,
            Hex.decode(originalRunningHashHexString));

        byte[] message = "first message".getBytes(StandardCharsets.UTF_8);
        Instant updatedConsensusTimestamp = Instant.ofEpochSecond(1234567892L);
        String updatedRunningHashHexString =
            "608624a1d723186fa9b56147ca3786a65424b56bfe0a3de48049a3656d4eaf93fdf8eefab6c56db38ac2544c8bb6c312";

        ConsensusTopicRunningHash updated = original.getUpdatedRunningHash(message, updatedConsensusTimestamp);

        assertAll(
            // Assert original unchanged.
            () -> { assertEquals(topicId, original.topicId); },
            () -> { assertEquals(123L, original.sequenceNumber); },
            () -> { assertEquals(originalRunningHashHexString, Hex.toHexString(original.runningHash)); },

            // Assert next running hash.
            () -> { assertEquals(topicId, updated.topicId); },
            () -> { assertEquals(124L, updated.sequenceNumber); },
            () -> { assertEquals(updatedRunningHashHexString, Hex.toHexString(updated.runningHash)); }
        );
    }
}
