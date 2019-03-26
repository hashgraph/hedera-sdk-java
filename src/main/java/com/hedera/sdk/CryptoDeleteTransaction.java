package com.hedera.sdk;

import com.hedera.sdk.proto.CryptoDeleteTransactionBody;

public class CryptoDeleteTransaction
        extends TransactionBuilder<com.hedera.sdk.CryptoDeleteTransaction> {
    private final CryptoDeleteTransactionBody.Builder builder;

    public CryptoDeleteTransaction() {
        builder = inner.getBodyBuilder().getCryptoDeleteBuilder();
    }

    public CryptoDeleteTransaction setTransferAccountId(AccountId transferAccountId) {
        builder.setTransferAccountID(transferAccountId.inner);
        return this;
    }

    public CryptoDeleteTransaction setDeleteAccountId(AccountId deleteAccountId) {
        builder.setDeleteAccountID(deleteAccountId.inner);
        return this;
    }
}
