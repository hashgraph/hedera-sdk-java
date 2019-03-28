package com.hedera.sdk;

import com.hedera.sdk.proto.FileDeleteTransactionBody;
import com.hedera.sdk.proto.FileServiceGrpc;
import com.hedera.sdk.proto.Transaction;
import com.hedera.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

public final class FileDeleteTransaction extends TransactionBuilder<FileDeleteTransaction> {
    private final FileDeleteTransactionBody.Builder builder;

    public FileDeleteTransaction() {
        builder = inner.getBodyBuilder().getFileDeleteBuilder();
    }

    public FileDeleteTransaction setFileId(FileId fileId) {
        builder.setFileID(fileId.inner);
        return this;
    }

    @Override
    MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return FileServiceGrpc.getDeleteFileMethod();
    }
}
