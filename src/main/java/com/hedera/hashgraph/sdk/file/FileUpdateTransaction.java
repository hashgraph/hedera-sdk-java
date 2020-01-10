package com.hedera.hashgraph.sdk.file;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.proto.FileServiceGrpc;
import com.hedera.hashgraph.proto.FileUpdateTransactionBody;
import com.hedera.hashgraph.proto.KeyList;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hedera.hashgraph.sdk.crypto.PublicKey;

import java.time.Instant;

import io.grpc.MethodDescriptor;

public class FileUpdateTransaction extends TransactionBuilder<FileUpdateTransaction> {
    private final FileUpdateTransactionBody.Builder builder = bodyBuilder.getFileUpdateBuilder();
    private final KeyList.Builder keyList = builder.getKeysBuilder();

    public FileUpdateTransaction() { super(); }

    public FileUpdateTransaction setFileId(FileId file) {
        builder.setFileID(file.toProto());

        return this;
    }

    public FileUpdateTransaction setExpirationTime(Instant expiration) {
        builder.setExpirationTime(TimestampHelper.timestampFrom(expiration));

        return this;
    }

    public FileUpdateTransaction addKey(PublicKey key) {
        keyList.addKeys(key.toKeyProto());

        return this;
    }

    public FileUpdateTransaction setContents(byte[] bytes) {
        builder.setContents(ByteString.copyFrom(bytes));

        return this;
    }

    /**
     * Encode the given {@link String} as UTF-8 and set it as the file's contents.
     *
     * The string can later be recovered from {@link FileContentsQuery#execute(Client)}
     * via {@link String#String(byte[], java.nio.charset.Charset)} using
     * {@link java.nio.charset.StandardCharsets#UTF_8}.
     */
    public FileUpdateTransaction setContents(String text) {
        builder.setContents(ByteString.copyFromUtf8(text));
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return FileServiceGrpc.getUpdateFileMethod();
    }

    @Override
    protected void doValidate() {
        require(builder.hasFileID(), ".setFileId() required");
    }
}
