package com.hedera.hashgraph.sdk.file;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hederahashgraph.api.proto.java.FileAppendTransactionBody;
import com.hederahashgraph.api.proto.java.Transaction;
import com.hederahashgraph.api.proto.java.TransactionResponse;
import com.hederahashgraph.service.proto.java.FileServiceGrpc;

import javax.annotation.Nullable;

import io.grpc.MethodDescriptor;

public final class FileAppendTransaction extends TransactionBuilder<FileAppendTransaction> {
    private final FileAppendTransactionBody.Builder builder = bodyBuilder.getFileAppendBuilder();

    public FileAppendTransaction(@Nullable Client client) {
        super(client);
    }

    public FileAppendTransaction setFileId(FileId fileId) {
        builder.setFileID(fileId.toProto());
        return this;
    }

    public FileAppendTransaction setContents(byte[] contents) {
        // TODO: there is a maximum length for contents. What is it?
        builder.setContents(ByteString.copyFrom(contents));
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return FileServiceGrpc.getAppendContentMethod();
    }

    @Override
    protected void doValidate() {
        require(builder.hasFileID(), ".setFileId() required");
        require(builder.getContents(), ".setContents() required");
    }
}
