package com.hedera.sdk;

import com.google.protobuf.ByteString;
import com.hedera.sdk.crypto.IPublicKey;
import com.hedera.sdk.proto.FileCreateTransactionBody;
import com.hedera.sdk.proto.KeyList;
import com.hedera.sdk.proto.Timestamp;
import java.time.Duration;
import java.time.Instant;
import javax.annotation.Nonnull;

public final class FileCreateTransaction extends TransactionBuilder<FileCreateTransaction> {
    private final FileCreateTransactionBody.Builder builder;
    private final KeyList.Builder keyList;

    public FileCreateTransaction() {
        builder = inner.getBodyBuilder().getFileCreateBuilder();
        keyList = builder.getKeysBuilder();
    }

    public FileCreateTransaction setExpiration(Instant expiration) {
        builder.setExpirationTime(TimestampHelper.timestampFrom(expiration));
        return this;
    }

    public FileCreateTransaction setKey(IPublicKey key) {
        keyList.addKeys(key.toProtoKey());
        return this;
    }

    public FileCreateTransaction setKey(ContractId key) {
        keyList.addKeys(key.toProtoKey());
        return this;
    }

    public FileCreateTransaction setContents(byte[] bytes) {
        builder.setContents(ByteString.copyFrom(bytes));
        return this;
    }
}
