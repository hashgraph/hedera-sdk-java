package com.hedera.hashgraph.sdk;

import com.google.common.base.Splitter;
import java8.lang.FunctionalInterface;
import org.bouncycastle.util.encoders.DecoderException;
import org.bouncycastle.util.encoders.Hex;

import javax.annotation.Nonnegative;
import java.nio.ByteBuffer;
import java.util.Objects;

class EntityId {
    /**
     * The length of a Solidity address in bytes.
     */
    static final int SOLIDITY_ADDRESS_LEN = 20;

    /**
     * The length of a hexadecimal-encoded Solidity address, in ASCII characters (bytes).
     */
    static final int SOLIDITY_ADDRESS_LEN_HEX = SOLIDITY_ADDRESS_LEN * 2;

    /**
     * The shard number
     */
    @Nonnegative
    public final long shard;

    /**
     * The realm number
     */
    @Nonnegative
    public final long realm;

    /**
     * The id number
     */
    @Nonnegative
    public final long num;

    EntityId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num) {
        this.shard = shard;
        this.realm = realm;
        this.num = num;
    }

    static <R> R fromString(String id, WithIdNums<R> withIdNums) {
        var nums = Splitter.on('.').split(id).iterator();

        R newId;

        try {
            var shard = Long.parseLong(nums.next());
            var realm = Long.parseLong(nums.next());
            var num = Long.parseLong(nums.next());
            newId = withIdNums.apply(shard, realm, num);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Id format, should be in format {shardNum}.{realmNum}.{idNum}", e);
        }

        return newId;
    }

    static <R> R fromSolidityAddress(String address, WithIdNums<R> withAddress) {
        return fromSolidityAddress(decodeSolidityAddress(address), withAddress);
    }

    private static <R> R fromSolidityAddress(byte[] address, WithIdNums<R> withAddress) {
        if (address.length != SOLIDITY_ADDRESS_LEN) {
            throw new IllegalArgumentException(
                "Solidity addresses must be 20 bytes or 40 hex chars");
        }

        var buf = ByteBuffer.wrap(address);
        return withAddress.apply(buf.getInt(), buf.getLong(), buf.getLong());
    }

    private static byte[] decodeSolidityAddress(String address) {
        if (address.length() != SOLIDITY_ADDRESS_LEN_HEX) {
            throw new IllegalArgumentException(
                "Solidity addresses must be 20 bytes or 40 hex chars");
        }

        try {
            return Hex.decode(address);
        } catch (DecoderException e) {
            throw new IllegalArgumentException("failed to decode Solidity address as hex", e);
        }
    }

    String toSolidityAddress() {
        if (Long.highestOneBit(shard) > 32) {
            throw new IllegalStateException("shard out of 32-bit range " + shard);
        }

        return Hex.toHexString(
            ByteBuffer.allocate(20)
                .putInt((int) shard)
                .putLong(realm)
                .putLong(num)
                .array());
    }

    @Override
    public String toString() {
        return "" + shard + "." + realm + "." + num;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntityId)) return false;

        EntityId otherId = (EntityId) o;
        return shard == otherId.shard && realm == otherId.realm && num == otherId.num;
    }

    @Override
    public int hashCode() {
        return Objects.hash(shard, realm, num);
    }

    @FunctionalInterface
    interface WithIdNums<R> {
        R apply(long shard, long realm, long num);
    }
}
