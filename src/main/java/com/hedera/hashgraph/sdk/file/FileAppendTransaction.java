package com.hedera.hashgraph.sdk.file;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.proto.FileAppendTransactionBody;
import com.hedera.hashgraph.proto.FileServiceGrpc;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.TransactionBuilder;

import io.grpc.MethodDescriptor;

public final class FileAppendTransaction extends TransactionBuilder<FileAppendTransaction> {
    private final FileAppendTransactionBody.Builder builder = bodyBuilder.getFileAppendBuilder();

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

    /**
     * Encode the given {@link String} as UTF-8 and append it to the file's contents.
     *
     * If the whole file is UTF-8 encoded, the string can later be recovered from
     * {@link FileContentsQuery#execute(Client)} via
     * {@link String#String(byte[], java.nio.charset.Charset)} using
     * {@link java.nio.charset.StandardCharsets#UTF_8}.
     */
    public FileAppendTransaction setContents(String text) {
        builder.setContents(ByteString.copyFromUtf8(text));
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
