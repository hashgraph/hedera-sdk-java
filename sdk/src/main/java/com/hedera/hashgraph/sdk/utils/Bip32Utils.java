package com.hedera.hashgraph.sdk.utils;

public class Bip32Utils {

    public static final int HARDENED_BIT = 0x80000000;

    private Bip32Utils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Harden the index
     *
     * @param index         the derivation index
     * @return              the hardened index
     */
    public static int toHardenedIndex(int index) {
        return index | HARDENED_BIT;
    }

    /**
     * Check if the index is hardened
     *
     * @param index         the derivation index
     * @return              true if the index is hardened
     */
    public static boolean isHardenedIndex(int index) {
        return (index & HARDENED_BIT) != 0;
    }
}
