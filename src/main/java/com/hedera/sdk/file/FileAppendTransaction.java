package com.hedera.sdk.file;

import com.google.protobuf.ByteString;
import com.hedera.sdk.Client;
import com.hedera.sdk.FileId;
import com.hedera.sdk.TransactionBuilder;
import com.hedera.sdk.proto.FileAppendTransactionBody;
import com.hedera.sdk.proto.FileServiceGrpc;
import com.hedera.sdk.proto.Transaction;
import com.hedera.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

public final class FileAppendTransaction extends TransactionBuilder<FileAppendTransaction> {
    private final FileAppendTransactionBody.Builder builder = inner.getBodyBuilder()
        .getFileAppendBuilder();

    public FileAppendTransaction(Client client) {
        super(client);
    }

    FileAppendTransaction() {
        super(null);
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
