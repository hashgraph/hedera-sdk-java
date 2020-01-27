package com.hedera.hashgraph.sdk.consensus;

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
public final class ConsensusTopicRunningHash {
    public final ConsensusTopicId topicId;

    /**
     * Sequence number of messages on the topic. At topic creation, the sequenceNumber is 0. The topic number is
     * incremented by 1 each time a successful ConsensusMessageSubmitTransaction achieves consensus.
     */
    public final long sequenceNumber;

    /**
     * Running hash of fields from successful ConsensusMessageSubmitTransactions on the topic. A running SHA-384
     * hash of the previous runningHash (initialized to 48 bytes all 0s on creation), topic ID, sequenceNumber
     * (incremented from 0 after every successful ConsensusSubmitMessage), and consensusTimestamp when that
     * ConsensusMessageSubmitTransaction reached consensus.
     */
    public final byte[] runningHash;

    /**
     * Initialized state of the running hash upon topic creation, setting sequeceNumber to 0 and runningHash to
     * 48 bytes all 0.
     * @param topicId
     */
    public ConsensusTopicRunningHash(ConsensusTopicId topicId) {
        this.topicId = topicId;
        this.sequenceNumber = 0;
        this.runningHash = new byte[48];
    }

    /**
     * A ConsensusRunning hash at a specific point in the life of a topic, after some number of successful
     * ConsensusMessageSubmitTransactions.
     * @param topicId
     * @param sequenceNumber
     * @param runningHash
     */
    public ConsensusTopicRunningHash(ConsensusTopicId topicId, long sequenceNumber, byte[] runningHash) {
        this.topicId = topicId;
        this.sequenceNumber = sequenceNumber;
        this.runningHash = (null == runningHash) ? new byte[48] : runningHash;
    }

    /**
     * Running hash context derived from existing ConsensusTopicInfo.
     * @param topicInfo
     */
    public ConsensusTopicRunningHash(ConsensusTopicInfo topicInfo) {
        this.topicId = topicInfo.id;
        this.sequenceNumber = topicInfo.sequenceNumber;
        this.runningHash = topicInfo.runningHash.clone();
    }

    /**
     * Return a new ConsensusTopicRunningHash by combining this with the message and consensusTimestamp from a
     * successful ConsensusMessageSubmitTransaction.
     * @param message
     * @param consensusTimestamp
     * @return
     */
    public ConsensusTopicRunningHash getUpdatedRunningHash(byte[] message, Instant consensusTimestamp) {
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
            out.writeObject(message);
            out.flush();
            byte[] nextRunningHash = MessageDigest.getInstance("SHA-384").digest(bos.toByteArray());
            return new ConsensusTopicRunningHash(topicId, nextSequenceNumber, nextRunningHash);
        } catch (NoSuchAlgorithmException e) {
           throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(topicId, sequenceNumber, runningHash);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;

        if (!(other instanceof ConsensusTopicRunningHash)) return false;

        ConsensusTopicRunningHash otherRunningHash = (ConsensusTopicRunningHash) other;
        return Objects.equals(topicId, otherRunningHash.topicId)
            && sequenceNumber == otherRunningHash.sequenceNumber
            && Arrays.equals(runningHash, otherRunningHash.runningHash);
    }

    @Override
    public String toString() {
        return "ConsensusTopicRunningHash{"
            + "topicId=" + topicId
            + ", sequenceNumber=" + sequenceNumber
            + ", runningHash=" + Hex.toHexString(runningHash)
            + "}";
    }
}
