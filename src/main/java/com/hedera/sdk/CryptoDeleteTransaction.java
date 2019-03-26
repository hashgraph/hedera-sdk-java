package com.hedera.sdk;

import javax.annotation.Nonnull;

public class CryptoDeleteTransaction
        extends TransactionBuilder<com.hedera.sdk.CryptoDeleteTransaction> {
    public CryptoDeleteTransaction(
            @Nonnull AccountId transferAccountId, @Nonnull AccountId deleteAccountId) {
        inner.getBodyBuilder()
                .getCryptoDeleteBuilder()
                .setTransferAccountID(transferAccountId.inner)
                .setDeleteAccountID(deleteAccountId.inner);
    }
}
