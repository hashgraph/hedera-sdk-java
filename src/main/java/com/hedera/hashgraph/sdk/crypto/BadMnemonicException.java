package com.hedera.hashgraph.sdk.crypto;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Thrown by {@link Mnemonic#Mnemonic(List)} on validation error.
 */
public class BadMnemonicException extends RuntimeException {

    /**
     * If not null, these are the indices in the original word
     * list that were not found in the BIP-39 standard English
     * word list.
     */
    @Nullable
    public final List<Integer> unknownIndices;

    BadMnemonicException(String message) {
        this(message, null);
    }

    BadMnemonicException(String message, @Nullable List<Integer> unknownIndices) {
        super(message);
        this.unknownIndices = unknownIndices;
    }
}
