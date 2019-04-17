package com.hedera.sdk.file;

import com.google.protobuf.ByteString;
import com.hedera.sdk.Client;
import com.hedera.sdk.TimestampHelper;
import com.hedera.sdk.TransactionBuilder;
import com.hedera.sdk.crypto.Key;
import com.hedera.sdk.proto.*;
import com.hedera.sdk.proto.Transaction;
import io.grpc.MethodDescriptor;
import java.time.Instant;

public final class FileCreateTransaction extends TransactionBuilder<FileCreateTransaction> {
    private final FileCreateTransactionBody.Builder builder = bodyBuilder.getFileCreateBuilder();
    private final KeyList.Builder keyList = builder.getKeysBuilder();

    public FileCreateTransaction(Client client) {
        super(client);
    }

    FileCreateTransaction() {
        super(null);
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
            ".addKey() required"
        );
    }
}
