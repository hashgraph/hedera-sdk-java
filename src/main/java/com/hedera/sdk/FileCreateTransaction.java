package com.hedera.sdk;

import com.google.protobuf.ByteString;
import com.hedera.sdk.crypto.Key;
import com.hedera.sdk.proto.FileCreateTransactionBody;
import com.hedera.sdk.proto.KeyList;
import java.time.Instant;

public final class FileCreateTransaction extends TransactionBuilder<FileCreateTransaction> {
    private final FileCreateTransactionBody.Builder builder;
    private final KeyList.Builder keyList;

    public FileCreateTransaction() {
        builder = inner.getBodyBuilder().getFileCreateBuilder();
        keyList = builder.getKeysBuilder();
    }

    public FileCreateTransaction setExpirationTime(Instant expiration) {
        builder.setExpirationTime(TimestampHelper.timestampFrom(expiration));
        return this;
    }

    public FileCreateTransaction addKey(Key key) {
        keyList.addKeys(key.toProtoKey());
        return this;
    }

    public FileCreateTransaction setContents(byte[] bytes) {
        builder.setContents(ByteString.copyFrom(bytes));
        return this;
    }

    public FileCreateTransaction setNewRealmAdminKey(Key key) {
        builder.setNewRealmAdminKey(key.toProtoKey());

        return this;
    }

}
