package com.hedera.sdk;

import com.google.protobuf.ByteString;
import com.hedera.sdk.crypto.Key;
import com.hedera.sdk.proto.FileUpdateTransactionBody;
import com.hedera.sdk.proto.KeyList;
import java.time.Instant;

public class FileUpdateTransaction extends TransactionBuilder<FileUpdateTransaction> {
    private final FileUpdateTransactionBody.Builder builder;
    private final KeyList.Builder keyList;

    public FileUpdateTransaction() {
        builder = inner.getBodyBuilder().getFileUpdateBuilder();
        keyList = builder.getKeysBuilder();
    }

    public FileUpdateTransaction setExpirationTime(Instant expiration) {
        builder.setExpirationTime(TimestampHelper.timestampFrom(expiration));

        return this;
    }

    public FileUpdateTransaction addKey(Key key) {
        keyList.addKeys(key.toKeyProto());

        return this;
    }

    public FileUpdateTransaction setContents(byte[] bytes) {
        builder.setContents(ByteString.copyFrom(bytes));

        return this;
    }

    public FileUpdateTransaction setFile(FileId file) {
        builder.setFileID(file.inner);

        return this;
    }
}
