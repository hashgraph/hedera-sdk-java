package com.hedera.hashgraph.sdk;

import com.google.common.base.Splitter;
import com.google.errorprone.annotations.Var;
import java8.lang.FunctionalInterface;
import org.bouncycastle.util.encoders.DecoderException;
import org.bouncycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.util.Objects;

class EntityIdHelper {
    /**
     * The length of a Solidity address in bytes.
     */
    static final int SOLIDITY_ADDRESS_LEN = 20;

    /**
     * The length of a hexadecimal-encoded Solidity address, in ASCII characters (bytes).
     */
    static final int SOLIDITY_ADDRESS_LEN_HEX = SOLIDITY_ADDRESS_LEN * 2;

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

    private static byte[] decodeSolidityAddress(@Var String address) {
        address = address.startsWith("0x") ? address.substring(2) : address;

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

    static String toSolidityAddress(long shard, long realm, long num) {
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

    @FunctionalInterface
    interface WithIdNums<R> {
        R apply(long shard, long realm, long num);
    }
}
