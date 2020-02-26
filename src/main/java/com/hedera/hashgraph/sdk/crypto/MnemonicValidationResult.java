package com.hedera.hashgraph.sdk.crypto;

import java.util.List;

import javax.annotation.Nullable;

public final class MnemonicValidationResult {
    public final MnemonicValidationStatus status;

    /**
     * If not null, these are the indices in the mnemonic that were not found in the
     * BIP-39 standard English word list.
     *
     * If {@code status == MnemonicValidationStatus.UnknownWords} then this will be not null.
     */
    @Nullable
    public final List<Integer> unknownIndices;

    MnemonicValidationResult(MnemonicValidationStatus status) {
        this(status, null);
    }

    MnemonicValidationResult(MnemonicValidationStatus status, @Nullable List<Integer> unknownIndices) {
        this.status = status;
        this.unknownIndices = unknownIndices;
    }

    public boolean isOk() {
        return status == MnemonicValidationStatus.Ok;
    }

    @Override
    public String toString() {
        switch (status) {
            case Ok:
                return "OK";
            case BadLength:
                return "mnemonic was not exactly 24 words";
            case UnknownWords:
                return "mnemonic contained words that are not in the standard BIP-39 English word list";
            case ChecksumMismatch:
                return "checksum word in mnemonic did not match the rest of the mnemonic";
        }

        throw new Error("(BUG) missing branch for status: " + status);
    }
}
