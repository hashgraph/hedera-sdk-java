package com.hedera.hashgraph.sdk;

import javax.annotation.Nullable;
import java.util.List;

public class BadMnemonicException extends Exception {
    /**
     * The mnemonic that failed validation.
     */
    public final Mnemonic mnemonic;

    /**
     * The reason for which the mnemonic failed validation.
     */
    public final BadMnemonicReason reason;

    /**
     * If not null, these are the indices in the mnemonic that were not found in the
     * BIP-39 standard English word list.
     * <p>
     * If {@code reason == BadMnemonicReason.UnknownWords} then this will be not null.
     */
    @Nullable
    public final List<Integer> unknownWordIndices;

    BadMnemonicException(Mnemonic mnemonic, BadMnemonicReason reason, List<Integer> unknownWordIndices) {
        this.mnemonic = mnemonic;
        this.reason = reason;
        this.unknownWordIndices = unknownWordIndices;
    }

    BadMnemonicException(Mnemonic mnemonic, BadMnemonicReason reason) {
        this.mnemonic = mnemonic;
        this.reason = reason;
        this.unknownWordIndices = null;
    }
}
