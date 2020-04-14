package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.SystemDeleteTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;

public final class SystemDeleteTransaction extends TransactionBuilder<SystemDeleteTransaction> {
    private final SystemDeleteTransactionBody.Builder builder;

    public SystemDeleteTransaction() {
        builder = SystemDeleteTransactionBody.newBuilder();
    }

    @Override
    protected void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setSystemDelete(builder);
    }
}
