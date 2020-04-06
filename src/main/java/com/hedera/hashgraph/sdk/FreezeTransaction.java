package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.FreezeTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;

public final class FreezeTransaction extends TransactionBuilder<FreezeTransaction> {
    private final FreezeTransactionBody.Builder builder;

    public FreezeTransaction() {
        builder = FreezeTransactionBody.newBuilder();
    }

    @Override
    protected void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setFreeze(builder);
    }
}
