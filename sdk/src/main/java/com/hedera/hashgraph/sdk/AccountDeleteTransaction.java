package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.CryptoDeleteTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;

public final class AccountDeleteTransaction extends TransactionBuilder<AccountDeleteTransaction> {
    private final CryptoDeleteTransactionBody.Builder builder;

    public AccountDeleteTransaction() {
        builder = CryptoDeleteTransactionBody.newBuilder();
    }

    public AccountDeleteTransaction setDeleteAccountId(AccountId deleteAccountId) {
        builder.setDeleteAccountID(deleteAccountId.toProtobuf());
        return this;
    }

    public AccountDeleteTransaction setTransferAccountId(AccountId transferAccountId) {
        builder.setTransferAccountID(transferAccountId.toProtobuf());
        return this;
    }

    @Override
    void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setCryptoDelete(builder);
    }
}
