package com.hedera.hashgraph.sdk.file;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hedera.hashgraph.sdk.crypto.PublicKey;
import com.hedera.hashgraph.proto.FileUpdateTransactionBody;
import com.hedera.hashgraph.proto.KeyList;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.proto.FileServiceGrpc;

import java.time.Instant;

import javax.annotation.Nullable;

import io.grpc.MethodDescriptor;

public class FileUpdateTransaction extends TransactionBuilder<FileUpdateTransaction> {
    private final FileUpdateTransactionBody.Builder builder = bodyBuilder.getFileUpdateBuilder();
    private final KeyList.Builder keyList = builder.getKeysBuilder();

    /**
     * @deprecated use the no-arg constructor and pass the client to {@link #build(Client)} instead.
     */
    @Deprecated
    public FileUpdateTransaction(@Nullable Client client) {
        super(client);
    }

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

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return FileServiceGrpc.getUpdateFileMethod();
    }

    @Override
    protected void doValidate() {
        require(builder.hasFileID(), ".setFileId() required");
    }
}
