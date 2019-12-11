package com.hedera.hashgraph.sdk.file;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hedera.hashgraph.proto.FileAppendTransactionBody;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.proto.FileServiceGrpc;

import javax.annotation.Nullable;

import io.grpc.MethodDescriptor;

public final class FileAppendTransaction extends TransactionBuilder<FileAppendTransaction> {
    private final FileAppendTransactionBody.Builder builder = bodyBuilder.getFileAppendBuilder();

    /**
     * @deprecated use the no-arg constructor and pass the client to {@link #build(Client)} instead.
     */
    @Deprecated
    public FileAppendTransaction(@Nullable Client client) {
        super(client);
    }

    public FileAppendTransaction() { super(); }

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
