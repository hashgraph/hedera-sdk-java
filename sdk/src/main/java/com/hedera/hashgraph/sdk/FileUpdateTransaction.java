package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import com.hedera.hashgraph.sdk.proto.FileUpdateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.FileServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.time.Instant;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Updates a file by submitting the transaction.
 */
public final class FileUpdateTransaction extends Transaction<FileUpdateTransaction> {
    private final FileUpdateTransactionBody.Builder builder;

    @Nullable
    FileId fileId = null;

    public FileUpdateTransaction() {
        builder = FileUpdateTransactionBody.newBuilder();
    }

    FileUpdateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getFileUpdate().toBuilder();

        if (builder.hasFileID()) {
            fileId = FileId.fromProtobuf(builder.getFileID());
        }
    }

    FileUpdateTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);

        builder = bodyBuilder.getFileUpdate().toBuilder();

        if (builder.hasFileID()) {
            fileId = FileId.fromProtobuf(builder.getFileID());
        }
    }

    @Nullable
    public FileId getFileId() {
        return fileId;
    }

    /**
     * Set the ID of the file to update; required.
     *
     * @param fileId the ID of the file to update.
     * @return {@code this}
     */
    public FileUpdateTransaction setFileId(FileId fileId) {
        Objects.requireNonNull(fileId);
        requireNotFrozen();
        this.fileId = fileId;
        return this;
    }

    public Collection<Key> getKeys() {
        return KeyList.fromProtobuf(builder.getKeys(), null, null);
    }

    /**
     * <p>Set the keys which must sign any transactions modifying this file. Required.
     *
     * @return {@code this}
     * @param keys The Key or Keys to be set
     */
    public FileUpdateTransaction setKeys(Key... keys) {
        requireNotFrozen();

        var keyList = com.hedera.hashgraph.sdk.proto.KeyList.newBuilder();

        for (Key key: keys) {
            keyList.addKeys(key.toProtobufKey());
        }

        builder.setKeys(keyList);

        return this;
    }

    @Nullable
    public Instant getExpirationTime() {
        return builder.hasExpirationTime() ? InstantConverter.fromProtobuf(builder.getExpirationTime()) : null;
    }

    /**
     * If set, update the expiration time of the file.
     * <p>
     * Must be in the future (may only be used to extend the expiration).
     * To make a file inaccessible use {@link FileDeleteTransaction} instead.
     *
     * @param expirationTime the new {@link Instant} at which the transaction will expire.
     * @return {@code this}
     */
    public FileUpdateTransaction setExpirationTime(Instant expirationTime) {
        Objects.requireNonNull(expirationTime);
        requireNotFrozen();
        builder.setExpirationTime(InstantConverter.toProtobuf(expirationTime));
        return this;
    }

    public ByteString getContents() {
        return builder.getContents();
    }

    /**
     * If set, replace contents of the file identified by {@link #setFileId(FileId)}
     * with the given bytes.
     * <p>
     * If the contents of the file are longer than the given byte array, then the file will
     * be truncated.
     * <p>
     * Note that total size for a given transaction is limited to 6KiB (as of March 2020) by the
     * network; if you exceed this you may receive a {@link Status#TRANSACTION_OVERSIZE}.
     * <p>
     * In this case, you will need to break the data into chunks of less than ~6KiB and execute this
     * transaction with the first chunk and then use {@link FileAppendTransaction} with
     * {@link FileAppendTransaction#setContents(byte[])} for the remaining chunks.
     *
     * @param bytes the bytes to replace the contents of the file with.
     * @return {@code this}
     * @see #setContents(String) for an overload which takes a {@link String}.
     * @see FileAppendTransaction if you merely want to add data to a file's existing contents.
     */
    public FileUpdateTransaction setContents(byte[] bytes) {
        requireNotFrozen();
        builder.setContents(ByteString.copyFrom(bytes));

        return this;
    }

    /**
     * If set, encode the given {@link String} as UTF-8 and replace the contents of the file
     * identified by {@link #setFileId(FileId)}.
     * <p>
     * If the contents of the file are longer than the UTF-8 encoding of the given string, then the
     * file will be truncated.
     * <p>
     * The string can later be recovered from {@link FileContentsQuery#execute(Client)}
     * via {@link String#String(byte[], java.nio.charset.Charset)} using
     * {@link java.nio.charset.StandardCharsets#UTF_8}.
     * <p>
     * Note that total size for a given transaction is limited to 6KiB (as of March 2020) by the
     * network; if you exceed this you may receive a  {@link Status#TRANSACTION_OVERSIZE}.
     * <p>
     * In this case, you will need to break the data into chunks of less than ~6KiB and execute this
     * transaction with the first chunk and then use {@link FileAppendTransaction} with
     * {@link FileAppendTransaction#setContents(String)} for the remaining chunks.
     *
     * @param text the string to replace the contents of the file with.
     * @return {@code this}
     * @see #setContents(byte[]) for replacing the contents with arbitrary data.
     * @see FileAppendTransaction if you merely want to add data to a file's existing contents.
     */
    public FileUpdateTransaction setContents(String text) {
        Objects.requireNonNull(text);
        requireNotFrozen();
        builder.setContents(ByteString.copyFromUtf8(text));
        return this;
    }

    public String getFileMemo() {
        return builder.getMemo().getValue();
    }

    public FileUpdateTransaction setFileMemo(String memo) {
        Objects.requireNonNull(memo);
        requireNotFrozen();
        this.builder.setMemo(StringValue.of(memo));
        return this;
    }

    public FileUpdateTransaction clearMemo() {
        requireNotFrozen();
        this.builder.clearMemo();
        return this;
    }

    FileUpdateTransactionBody.Builder build() {
        if (fileId != null) {
            builder.setFileID(fileId.toProtobuf());
        }

        return builder;
    }

    @Override
    void validateNetworkOnIds(Client client) {
        if (fileId != null) {
            fileId.validate(client);
        }
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return FileServiceGrpc.getUpdateFileMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setFileUpdate(build());
        return true;
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setFileUpdate(build());
    }
}
