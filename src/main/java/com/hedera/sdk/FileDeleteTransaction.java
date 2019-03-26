package com.hedera.sdk;

import com.hedera.sdk.proto.FileDeleteTransactionBody;

public final class FileDeleteTransaction extends TransactionBuilder<FileDeleteTransaction> {
    private final FileDeleteTransactionBody.Builder builder;

    public FileDeleteTransaction() {
        builder = inner.getBodyBuilder().getFileDeleteBuilder();
    }

    public FileDeleteTransaction setFileId(FileId fileId) {
        builder.setFileID(fileId.inner);
        return this;
    }
}
