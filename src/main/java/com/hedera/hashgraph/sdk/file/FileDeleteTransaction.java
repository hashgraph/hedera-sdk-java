package com.hedera.hashgraph.sdk.file;

import com.hedera.hashgraph.proto.FileDeleteTransactionBody;
import com.hedera.hashgraph.proto.FileServiceGrpc;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.TransactionBuilder;

import io.grpc.MethodDescriptor;

public final class FileDeleteTransaction extends TransactionBuilder<FileDeleteTransaction> {
    private final FileDeleteTransactionBody.Builder builder = bodyBuilder.getFileDeleteBuilder();

    public FileDeleteTransaction() { super(); }

    public FileDeleteTransaction setFileId(FileId fileId) {
        builder.setFileID(fileId.toProto());
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return FileServiceGrpc.getDeleteFileMethod();
    }

    @Override
    protected void doValidate() {
        require(builder.hasFileID(), ".setFileId() required");
    }
}
