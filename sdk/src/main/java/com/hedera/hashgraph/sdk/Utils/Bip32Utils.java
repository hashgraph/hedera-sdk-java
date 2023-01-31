package com.hedera.hashgraph.sdk.Utils;

public class Bip32Utils {
    /**
     * Harden the index
     *
     * @param index         the derivation index
     * @return              the hardened index
     */
    public static int toHardenedIndex(int index) {
        return index | 0x80000000;
    }

    /**
     * Check if the index is hardened
     *
     * @param index         the derivation index
     * @return              true if the index is hardened
     */
    public static boolean isHardenedIndex(int index) {
        return (index & 0x80000000) != 0;
    }
}
