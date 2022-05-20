package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import org.bouncycastle.util.encoders.Hex;

import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * Internal utility class for ledger id manipulation.
 */
public class LedgerId {
    final private byte[] idBytes;

    public final static LedgerId MAINNET = new LedgerId(new byte[]{0});
    public final static LedgerId TESTNET = new LedgerId(new byte[]{1});
    public final static LedgerId PREVIEWNET = new LedgerId(new byte[]{2});

    /**
     * Constructor.
     *
     * @param idBytes                   the id (0=mainnet, 1=testnet, 2=previewnet, ...)
     */
    LedgerId(byte[] idBytes) {
        this.idBytes = idBytes;
    }

    /**
     * Assign the ledger id via a string name or Hex encoded String.
     *
     * @param string                    the string containing the ledger id
     * @return                          the ledger id
     */
    public static LedgerId fromString(String string) {
        switch (string) {
            case "mainnet":
                return MAINNET;
            case "testnet":
                return TESTNET;
            case "previewnet":
                return PREVIEWNET;
            default:
                return new LedgerId(Hex.decode(string));
        }
    }

    /**
     * Create a ledger id from a byte array.
     *
     * @param bytes                     the byte array
     * @return                          the ledger id
     */
    public static LedgerId fromBytes(byte[] bytes) {
        return new LedgerId(bytes);
    }

    /**
     * Create a ledger id from a string.
     *
     * @param byteString                the string
     * @return                          the ledger id
     */
    static LedgerId fromByteString(ByteString byteString) {
        return fromBytes(byteString.toByteArray());
    }

    @Deprecated
    public static LedgerId fromNetworkName(NetworkName networkName) {
        switch (networkName) {
            case MAINNET:
                return MAINNET;
            case TESTNET:
                return TESTNET;
            case PREVIEWNET:
                return PREVIEWNET;
            default:
                throw new IllegalArgumentException("networkName must be MAINNET, TESTNET, or PREVIEWNET");
        }
    }

    /**
     * @return                          is it mainnet
     */
    public boolean isMainnet() {
        return this.equals(MAINNET);
    }

    /**
     * @return                          is it testnet
     */
    public boolean isTestnet() {
        return this.equals(TESTNET);
    }

    /**
     * @return                          is it previewnet
     */
    public boolean isPreviewnet() {
        return this.equals(PREVIEWNET);
    }

    /**
     * @return                          is it one of the three standard networks
     */
    boolean isKnownNetwork() {
        return isMainnet() || isTestnet() || isPreviewnet();
    }

    /**
     * @return                          the string representation
     */
    public String toString() {
        if (isMainnet()) {
            return "mainnet";
        } else if (isTestnet()) {
            return "testnet";
        } else if (isPreviewnet()) {
            return "previewnet";
        } else {
            return Hex.toHexString(idBytes);
        }
    }

    /**
     * @return                          the byte array representation
     */
    public byte[] toBytes() {
        return idBytes;
    }

    /**
     * @return                          the byte string representation
     */
    ByteString toByteString() {
        return ByteString.copyFrom(idBytes);
    }

    @Deprecated
    public NetworkName toNetworkName() {
        if (isMainnet()) {
            return NetworkName.MAINNET;
        } else if (isTestnet()) {
            return NetworkName.TESTNET;
        } else if (isPreviewnet()) {
            return NetworkName.PREVIEWNET;
        } else {
            return NetworkName.OTHER;
        }
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof LedgerId)) {
            return false;
        }

        LedgerId otherId = (LedgerId) o;
        return Arrays.equals(idBytes, otherId.idBytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(idBytes);
    }
}
