package com.hedera.hashgraph.sdk.file;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hedera.hashgraph.proto.FileDeleteTransactionBody;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.proto.FileServiceGrpc;

import javax.annotation.Nullable;

import io.grpc.MethodDescriptor;

public final class FileDeleteTransaction extends TransactionBuilder<FileDeleteTransaction> {
    private final FileDeleteTransactionBody.Builder builder = bodyBuilder.getFileDeleteBuilder();

    /**
     * @deprecated use the no-arg constructor and pass the client to {@link #build(Client)} instead.
     */
    @Deprecated
    public FileDeleteTransaction(@Nullable Client client) {
        super(client);
    }

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
