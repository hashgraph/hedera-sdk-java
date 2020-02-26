package com.hedera.hashgraph.sdk.consensus;

import com.hedera.hashgraph.sdk.HederaTopicMessageException;
import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

/**
 * The state of a consensus topic relating to the running hash of messages on that topic.
 * Used to verify the topic running hash of messages returned from the Hedera Hashgraph network.
 */
public final class ConsensusTopicState {
    public final ConsensusTopicId topicId;

    /**
     * Sequence number of messages on the topic. At topic creation, the sequenceNumber is 0. The topic number is
     * incremented by 1 each time a successful ConsensusMessageSubmitTransaction achieves consensus.
     */
    private long sequenceNumber;

    /**
     * Running hash of fields from successful ConsensusMessageSubmitTransactions on the topic. A running SHA-384
     * hash of the previous runningHash (initialized to 48 bytes all 0s on creation), topic ID, sequenceNumber
     * (incremented from 0 after every successful ConsensusSubmitMessage), and consensusTimestamp when that
     * ConsensusMessageSubmitTransaction reached consensus.
     */
    private byte[] runningHash;

    /**
     * State of a ConsensusTopic at a specific point in the life of a topic, relating to the Topic's sequence and
     * runningHash.
     * @param topicId
     * @param sequenceNumber
     * @param runningHash
     */
    public ConsensusTopicState(ConsensusTopicId topicId, long sequenceNumber, byte[] runningHash) {
        this.topicId = topicId;
        this.sequenceNumber = sequenceNumber;
        this.runningHash = (null == runningHash) ? new byte[48] : runningHash;
    }

    /**
     * Initialized state of the running hash upon topic creation, setting sequenceNumber to 0 and runningHash to
     * 48 bytes all 0.
     * @param topicId
     * @return
     */
    public static ConsensusTopicState forNewTopic(ConsensusTopicId topicId) {
        return new ConsensusTopicState(topicId, 0, null);
    }

    /**
     * State derived from existing ConsensusTopicInfo.
     * @param topicInfo
     */
    public static ConsensusTopicState fromConsensusTopicInfo(ConsensusTopicInfo topicInfo) {
        return new ConsensusTopicState(topicInfo.id, topicInfo.sequenceNumber, topicInfo.runningHash.clone());
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public byte[] getRunningHash() {
        return runningHash;
    }

    /**
     * Are the message, runningHash, sequenceNumber, and consensusTimestamp of a message the correct next values for
     * this topic?
     * @param nextMessage
     * @param nextSequenceNumber
     * @param nextRunningHash
     * @param consensusTimestamp
     * @return
     */
    public boolean verify(byte[] nextMessage, long nextSequenceNumber, byte[] nextRunningHash,
                          Instant consensusTimestamp)
    {
        if ((sequenceNumber + 1) != nextSequenceNumber) {
            return false;
        }
        byte[] expectedNextRunningHash = calculateNextRunningHash(nextMessage, consensusTimestamp);
        return Arrays.equals(nextRunningHash, expectedNextRunningHash);
    }

    /**
     * Update this state with the next message on the topic, if verified.
     * @param nextMessage
     * @param nextSequenceNumber
     * @param nextRunningHash
     * @param consensusTimestamp
     * @throws HederaTopicMessageException
     */
    public void update(byte[] nextMessage, long nextSequenceNumber, byte[] nextRunningHash,
                       Instant consensusTimestamp) throws HederaTopicMessageException
    {
        if (!verify(nextMessage, nextSequenceNumber, nextRunningHash, consensusTimestamp)) {
            throw new HederaTopicMessageException(topicId, consensusTimestamp);
        }
        runningHash = calculateNextRunningHash(nextMessage, consensusTimestamp);
        ++sequenceNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(topicId, sequenceNumber, runningHash);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;

        if (!(other instanceof ConsensusTopicState)) return false;

        ConsensusTopicState otherRunningHash = (ConsensusTopicState) other;
        return Objects.equals(topicId, otherRunningHash.topicId)
            && sequenceNumber == otherRunningHash.sequenceNumber
            && Arrays.equals(runningHash, otherRunningHash.runningHash);
    }

    @Override
    public String toString() {
        return "ConsensusTopicState{"
            + "topicId=" + topicId
            + ", sequenceNumber=" + sequenceNumber
            + ", runningHash=" + Hex.toHexString(runningHash)
            + "}";
    }

    /**
     * Calculate the running hash for a message/consensusTimestamp combination that are the next message in a
     * sequence on a topic.
     * @param nextMessage
     * @param consensusTimestamp
     * @return
     */
    private byte[] calculateNextRunningHash(byte[] nextMessage, Instant consensusTimestamp) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        long nextSequenceNumber = sequenceNumber + 1;
        try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(runningHash);
            out.writeLong(topicId.shard);
            out.writeLong(topicId.realm);
            out.writeLong(topicId.topic);
            out.writeLong(consensusTimestamp.getEpochSecond());
            out.writeInt(consensusTimestamp.getNano());
            out.writeLong(nextSequenceNumber);
            out.writeObject(nextMessage);
            out.flush();
            return MessageDigest.getInstance("SHA-384").digest(bos.toByteArray());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
