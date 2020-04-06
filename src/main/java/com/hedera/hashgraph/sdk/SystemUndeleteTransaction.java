package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.SystemUndeleteTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;

public final class SystemUndeleteTransaction extends TransactionBuilder<SystemUndeleteTransaction> {
    private final SystemUndeleteTransactionBody.Builder builder;

    public SystemUndeleteTransaction() {
        builder = SystemUndeleteTransactionBody.newBuilder();
    }

    @Override
    protected void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setSystemUndelete(builder);
    }
}
