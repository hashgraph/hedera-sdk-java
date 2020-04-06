package com.hedera.hashgraph.sdk;

import java8.lang.FunctionalInterface;
import org.bouncycastle.util.encoders.DecoderException;
import org.bouncycastle.util.encoders.Hex;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

final class SolidityUtil {
    /**
     * The length of a Solidity address in bytes.
     */
    static final int ADDRESS_LEN = 20;

    /**
     * The length of a hexadecimal-encoded Solidity address, in ASCII characters (bytes).
     */
    static final int ADDRESS_LEN_HEX = ADDRESS_LEN * 2;

    private SolidityUtil() {
    }

    static String addressForEntity(long shardNum, long realmNum, long entityNum) {
        if (Long.highestOneBit(shardNum) > 32) {
            throw new IllegalArgumentException("shardNum out of 32-bit range " + shardNum);
        }

        return Hex.toHexString(
            ByteBuffer.allocate(20)
                .putInt((int) shardNum)
                .putLong(realmNum)
                .putLong(entityNum)
                .array());
    }

    static String addressFor(AccountId accountId) {
        return addressForEntity(
            accountId.shard,
            accountId.realm,
            accountId.num);
    }

    static String addressFor(ContractId contractId) {
        return addressForEntity(
            contractId.shard,
            contractId.realm,
            contractId.num);
    }

    static String addressFor(FileId fileId) {
        return addressForEntity(
            fileId.shard,
            fileId.realm,
            fileId.num);
    }

    static <T> T parseAddress(String address, WithAddress<T> withAddress) {
        return decodeAddress(decodeAddress(address), withAddress);
    }

    static void checkAddressLen(byte[] address) {
        if (address.length != ADDRESS_LEN) {
            throw new IllegalArgumentException(
                "Solidity addresses must be 20 bytes or 40 hex chars");
        }
    }

    static <T> T decodeAddress(byte[] address, WithAddress<T> withAddress) {
        checkAddressLen(address);
        var buf = ByteBuffer.wrap(address);
        return withAddress.apply(buf.getInt(), buf.getLong(), buf.getLong());
    }

    static byte[] decodeAddress(String address) {
        if (address.length() != ADDRESS_LEN_HEX) {
            throw new IllegalArgumentException(
                "Solidity addresses must be 20 bytes or 40 hex chars");
        }

        try {
            return Hex.decode(address);
        } catch (DecoderException e) {
            throw new IllegalArgumentException("failed to decode Solidity address as hex", e);
        }
    }

    @FunctionalInterface
    interface WithAddress<T> {
        T apply(long shardNum, long realmNum, long entityNum);
    }
}
