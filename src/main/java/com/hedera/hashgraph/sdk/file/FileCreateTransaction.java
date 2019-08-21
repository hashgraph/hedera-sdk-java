package com.hedera.hashgraph.sdk.file;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hedera.hashgraph.sdk.crypto.Key;
import com.hederahashgraph.api.proto.java.FileCreateTransactionBody;
import com.hederahashgraph.api.proto.java.KeyList;
import com.hederahashgraph.api.proto.java.Transaction;
import com.hederahashgraph.api.proto.java.TransactionResponse;
import com.hederahashgraph.service.proto.java.FileServiceGrpc;

import java.time.Instant;

import javax.annotation.Nullable;

import io.grpc.MethodDescriptor;

public final class FileCreateTransaction extends TransactionBuilder<FileCreateTransaction> {
    private final FileCreateTransactionBody.Builder builder = bodyBuilder.getFileCreateBuilder();
    private final KeyList.Builder keyList = builder.getKeysBuilder();

    public FileCreateTransaction(@Nullable Client client) {
        super(client);
    }

    public FileCreateTransaction setExpirationTime(Instant expiration) {
        builder.setExpirationTime(TimestampHelper.timestampFrom(expiration));
        return this;
    }

    public FileCreateTransaction addKey(Key key) {
        keyList.addKeys(key.toKeyProto());
        return this;
    }

    public FileCreateTransaction setContents(byte[] bytes) {
        builder.setContents(ByteString.copyFrom(bytes));
        return this;
    }

    public FileCreateTransaction setNewRealmAdminKey(Key key) {
        builder.setNewRealmAdminKey(key.toKeyProto());
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return FileServiceGrpc.getCreateFileMethod();
    }

    @Override
    protected void doValidate() {
        require(
            builder.getKeysOrBuilder()
                .getKeysOrBuilderList(),
            ".addKey() required");
    }
}
