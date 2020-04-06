package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.FileCreateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;

public final class FileCreateTransaction extends TransactionBuilder<FileCreateTransaction> {
    private final FileCreateTransactionBody.Builder builder;

    public FileCreateTransaction() {
        builder = FileCreateTransactionBody.newBuilder();
    }

    @Override
    protected void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setFileCreate(builder);
    }
}
