package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.CryptoDeleteTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;

public final class AccountDeleteTransaction extends TransactionBuilder<AccountDeleteTransaction> {
    private final CryptoDeleteTransactionBody.Builder builder;

    public AccountDeleteTransaction() {
        builder = CryptoDeleteTransactionBody.newBuilder();
    }

    @Override
    protected void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setCryptoDelete(builder);
    }
}
