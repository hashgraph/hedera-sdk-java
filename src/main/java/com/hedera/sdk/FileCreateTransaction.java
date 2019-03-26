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
        keyList = KeyList.newBuilder();
    }

    public FileCreateTransaction setExpiration(@Nonnull Instant expiration) {
        builder.setExpirationTime(
                Timestamp.newBuilder()
                        .setSeconds(expiration.getEpochSecond())
                        .setNanos(expiration.getNano())
                        .build());
        return this;
    }

    public FileCreateTransaction setExpiration(@Nonnull Duration toExpiration) {
        this.setExpiration(Instant.now().plus(toExpiration));

        return this;
    }

    public FileCreateTransaction setKey(@Nonnull IPublicKey key) {
        keyList.addKeys(key.toProtoKey());

        return this;
    }

    public FileCreateTransaction setContents(@Nonnull byte[] bytes) {
        builder.setContents(ByteString.copyFrom(bytes));

        return this;
    }

    //todo: Needs to call builder.setKeys(this.keyList); at some point

}
