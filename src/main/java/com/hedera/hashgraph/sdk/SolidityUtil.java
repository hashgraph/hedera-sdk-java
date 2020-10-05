package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.contract.ContractId;
import com.hedera.hashgraph.sdk.file.FileId;

import com.hedera.hashgraph.sdk.token.TokenId;
import org.bouncycastle.util.encoders.DecoderException;
import org.bouncycastle.util.encoders.Hex;

import java.nio.ByteBuffer;

@Internal
public final class SolidityUtil {

    /**
     * The length of a Solidity address in bytes.
     */
    public static final int ADDRESS_LEN = 20;
    /**
     * The length of a hexadecimal-encoded Solidity address, in ASCII characters (bytes).
     */
    public static final int ADDRESS_LEN_HEX = ADDRESS_LEN * 2;

    private SolidityUtil() { }

    public static String addressForEntity(long shardNum, long realmNum, long entityNum) {
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

    public static String addressFor(AccountId accountId) {
        return addressForEntity(
            accountId.shard,
            accountId.realm,
            accountId.account);
    }

    public static String addressFor(ContractId contractId) {
        return addressForEntity(
            contractId.shard,
            contractId.realm,
            contractId.contract);
    }

    public static String addressFor(FileId fileId) {
        return addressForEntity(
            fileId.shard,
            fileId.realm,
            fileId.file);
    }

    public static String addressFor(TokenId tokenId) {
        return addressForEntity(
            tokenId.shard,
            tokenId.realm,
            tokenId.token);
    }

    public static <T> T parseAddress(String address, WithAddress<T> withAddress) {
        return decodeAddress(decodeAddress(address), withAddress);
    }

    public static <T> T decodeAddress(byte[] address, WithAddress<T> withAddress) {
        checkAddressLen(address);
        final ByteBuffer buf = ByteBuffer.wrap(address);
        return withAddress.apply(buf.getInt(), buf.getLong(), buf.getLong());
    }

    static void checkAddressLen(byte[] address) {
        if (address.length != ADDRESS_LEN) {
            throw new IllegalArgumentException(
                "Solidity addresses must be 20 bytes or 40 hex chars");
        }
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
    public interface WithAddress<T> {
        T apply(long shardNum, long realmNum, long entityNum);
    }
}
