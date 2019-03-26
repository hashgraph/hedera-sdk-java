package com.hedera.sdk;

import javax.annotation.Nonnull;

public class CryptoDeleteTransaction
        extends TransactionBuilder<com.hedera.sdk.CryptoDeleteTransaction> {

    public CryptoDeleteTransaction() {}

    public CryptoDeleteTransaction setTransferAccountId(@Nonnull AccountId transferAccountId) {
        inner.getBodyBuilder()
                .getCryptoDeleteBuilder()
                .setTransferAccountID(transferAccountId.inner);
        return this;
    }

    public CryptoDeleteTransaction setDeleteAccountId(@Nonnull AccountId deleteAccountId) {
        inner.getBodyBuilder().getCryptoDeleteBuilder().setDeleteAccountID(deleteAccountId.inner);
        return this;
    }
}
