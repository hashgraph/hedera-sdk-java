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

public class FileUpdateTransaction extends TransactionBuilder<FileUpdateTransaction> {
    private final FileUpdateTransactionBody.Builder builder = inner.getBodyBuilder()
        .getFileUpdateBuilder();
    private final KeyList.Builder keyList = builder.getKeysBuilder();

    public FileUpdateTransaction(Client client) {
        super(client);
    }

    FileUpdateTransaction() {
        super(null);
    }

    public FileUpdateTransaction setFile(FileId file) {
        builder.setFileID(file.toProto());

        return this;
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

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return FileServiceGrpc.getUpdateFileMethod();
    }

    @Override
    protected void doValidate() {
        require(builder.hasFileID(), ".setFileId()");
        require(
            builder.getKeysOrBuilder()
                .getKeysOrBuilderList(),
            ".addKey()"
        );
    }
}
