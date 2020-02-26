package com.hedera.hashgraph.sdk.consensus;

import com.hedera.hashgraph.sdk.HederaTopicMessageException;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ConsensusTopicStateTest {

    @Test
    void topicIdOnlyConstructor() {
        ConsensusTopicId topicId = new ConsensusTopicId(1, 2, 3);
        ConsensusTopicState cut = ConsensusTopicState.forNewTopic(topicId);
        assertAll(() -> { assertEquals(topicId, cut.topicId); },
            () -> { assertEquals(0, cut.getSequenceNumber()); },
            () -> { assertArrayEquals(new byte[48], cut.getRunningHash()); }
            );
    }

    @Test
    void updateFirstMessageOnTopic() {
        // Create ConsensusTopicState for new topic.
        ConsensusTopicId topicId = new ConsensusTopicId(1, 2, 3);
        ConsensusTopicState topicState = ConsensusTopicState.forNewTopic(topicId);

        byte[] message = "first message".getBytes(StandardCharsets.UTF_8);
        Instant consensusTimestamp = Instant.ofEpochSecond(1234567890);
        long sequenceNumber = 1L;
        byte[] expectedNextRunningHash =
            Hex.decode("df4162eebdb4aae3664ced2a764068508c576c6787561f44c4e4ce89ada124ba9c9db77cdc08057dd9b18759904e58aa");

        assertDoesNotThrow(() ->
            topicState.update(message, sequenceNumber, expectedNextRunningHash, consensusTimestamp)
        );

        assertAll(
            () -> { assertEquals(sequenceNumber, topicState.getSequenceNumber()); },
            () -> { assertArrayEquals(expectedNextRunningHash, topicState.getRunningHash()); }
        );
    }

    @Test
    void updateInvalidNextThrows() {
        // Create ConsensusTopicState for an existing topic.
        ConsensusTopicId topicId = new ConsensusTopicId(1, 2, 3);
        long originalSequenceNumber = 123L;
        byte[] originalRunningHash =
            Hex.decode("df4162eebdb4aae3664ced2a764068508c576c6787561f44c4e4ce89ada124ba9c9db77cdc08057dd9b18759904e58aa");
        ConsensusTopicState original = new ConsensusTopicState(topicId, originalSequenceNumber, originalRunningHash);

        byte[] message = "first message".getBytes(StandardCharsets.UTF_8);
        Instant updatedConsensusTimestamp = Instant.ofEpochSecond(1234567892L);
        byte[] validRunningHash = Hex.decode("608624a1d723186fa9b56147ca3786a65424b56bfe0a3de48049a3656d4eaf93fdf8eefab6c56db38ac2544c8bb6c312");
        byte[] invalidRunningHash = Hex.decode("ff8624a1d723186fa9b56147ca3786a65424b56bfe0a3de48049a3656d4eaf93fdf8eefab6c56db38ac2544c8bb6c312");

        assertAll(
            () -> assertDoesNotThrow(() -> original.update(message, originalSequenceNumber + 1,
                validRunningHash, updatedConsensusTimestamp))
            , () -> assertThrows(HederaTopicMessageException.class,
                () -> original.update(message, originalSequenceNumber + 1, invalidRunningHash,
                    updatedConsensusTimestamp))
            , () -> assertThrows(HederaTopicMessageException.class,
                () -> original.update(message, originalSequenceNumber + 2, validRunningHash,
                    updatedConsensusTimestamp))
        );
    }
}
