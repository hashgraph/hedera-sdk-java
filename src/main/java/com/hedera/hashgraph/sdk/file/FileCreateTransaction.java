package com.hedera.hashgraph.sdk.file;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.proto.FileCreateTransactionBody;
import com.hedera.hashgraph.proto.FileServiceGrpc;
import com.hedera.hashgraph.proto.KeyList;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hedera.hashgraph.sdk.crypto.PublicKey;

import java.time.Duration;
import java.time.Instant;

import javax.annotation.Nullable;

import io.grpc.MethodDescriptor;

public final class FileCreateTransaction extends TransactionBuilder<FileCreateTransaction> {
    private final FileCreateTransactionBody.Builder builder = bodyBuilder.getFileCreateBuilder();
    private final KeyList.Builder keyList = builder.getKeysBuilder();

    /**
     * @deprecated use the no-arg constructor and pass the client to {@link #build(Client)} instead.
     */
    @Deprecated
    public FileCreateTransaction(@Nullable Client client) {
        super(client);

        // Default expiration time to an acceptable value
        setExpirationTime(Instant.now().plus(Duration.ofMillis(7890000000L)));
    }

    public FileCreateTransaction() {
        this(null);
    }

    public FileCreateTransaction setExpirationTime(Instant expiration) {
        builder.setExpirationTime(TimestampHelper.timestampFrom(expiration));
        return this;
    }

    /**
     * Add a key which must sign any transactions modifying this file. Optional.
     *
     * A file without any keys is immutable.
     */
    public FileCreateTransaction addKey(PublicKey key) {
        keyList.addKeys(key.toKeyProto());
        return this;
    }

    public FileCreateTransaction setContents(byte[] bytes) {
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
    public FileCreateTransaction setContents(String text) {
        builder.setContents(ByteString.copyFromUtf8(text));
        return this;
    }

    /**
     * @deprecated shards and realms are not yet implemented on Hedera so this method won't
     * do anything. It will be restored when the network functionality is available.
     */
    @Deprecated
    public FileCreateTransaction setNewRealmAdminKey(PublicKey key) {
        builder.setNewRealmAdminKey(key.toKeyProto());
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return FileServiceGrpc.getCreateFileMethod();
    }

    @Override
    protected void doValidate() {
        // file without contents is allowed
        // file without key is just immutable
    }
}
