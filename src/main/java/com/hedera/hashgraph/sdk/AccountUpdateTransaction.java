package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.CryptoUpdateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;

public final class AccountUpdateTransaction extends TransactionBuilder<AccountUpdateTransaction> {
    private final CryptoUpdateTransactionBody.Builder builder;

    public AccountUpdateTransaction() {
        builder = CryptoUpdateTransactionBody.newBuilder();
    }

    @Override
    protected void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setCryptoUpdateAccount(builder);
    }
}
