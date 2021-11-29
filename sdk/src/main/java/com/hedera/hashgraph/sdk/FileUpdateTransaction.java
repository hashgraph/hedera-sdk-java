package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import com.hedera.hashgraph.sdk.proto.FileServiceGrpc;
import com.hedera.hashgraph.sdk.proto.FileUpdateTransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Updates a file by submitting the transaction.
 */
public final class FileUpdateTransaction extends Transaction<FileUpdateTransaction> {

    @Nullable
    private FileId fileId = null;
    @Nullable
    private KeyList keys = null;
    @Nullable
    private Instant expirationTime = null;
    private byte[] contents = {};
    @Nullable
    private String fileMemo = null;

    public FileUpdateTransaction() {
    }

    FileUpdateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    FileUpdateTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
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

    @Nullable
    public Collection<Key> getKeys() {
        return keys;
    }

    /**
     * <p>Set the keys which must sign any transactions modifying this file. Required.
     *
     * @param keys The Key or Keys to be set
     * @return {@code this}
     */
    public FileUpdateTransaction setKeys(Key... keys) {
        requireNotFrozen();

        this.keys = KeyList.of(keys);

        return this;
    }

    @Nullable
    public Instant getExpirationTime() {
        return expirationTime;
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
        this.expirationTime = expirationTime;
        return this;
    }

    public ByteString getContents() {
        return ByteString.copyFrom(contents);
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
     * In this case, you will need to keep the initial file contents under ~6KiB and
     * then use {@link FileAppendTransaction}, which automatically breaks the contents
     * into chunks for you, to append contents of arbitrary size.
     *
     * @param bytes the bytes to replace the contents of the file with.
     * @return {@code this}
     * @see #setContents(String) for an overload which takes a {@link String}.
     * @see FileAppendTransaction if you merely want to add data to a file's existing contents.
     */
    public FileUpdateTransaction setContents(byte[] bytes) {
        requireNotFrozen();
        Objects.requireNonNull(bytes);
        contents = bytes;
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
     * In this case, you will need to keep the initial file contents under ~6KiB and
     * then use {@link FileAppendTransaction}, which automatically breaks the contents
     * into chunks for you, to append contents of arbitrary size.
     *
     * @param text the string to replace the contents of the file with.
     * @return {@code this}
     * @see #setContents(byte[]) for replacing the contents with arbitrary data.
     * @see FileAppendTransaction if you merely want to add data to a file's existing contents.
     */
    public FileUpdateTransaction setContents(String text) {
        Objects.requireNonNull(text);
        requireNotFrozen();
        contents = text.getBytes(StandardCharsets.UTF_8);
        return this;
    }

    @Nullable
    public String getFileMemo() {
        return fileMemo;
    }

    public FileUpdateTransaction setFileMemo(String memo) {
        Objects.requireNonNull(memo);
        requireNotFrozen();
        fileMemo = memo;
        return this;
    }

    public FileUpdateTransaction clearMemo() {
        requireNotFrozen();
        fileMemo = "";
        return this;
    }

    void initFromTransactionBody() {
        var body = sourceTransactionBody.getFileUpdate();
        if (body.hasFileID()) {
            fileId = FileId.fromProtobuf(body.getFileID());
        }
        if (body.hasKeys()) {
            keys = KeyList.fromProtobuf(body.getKeys(), null);
        }
        if (body.hasExpirationTime()) {
            expirationTime = InstantConverter.fromProtobuf(body.getExpirationTime());
        }
        if (body.hasMemo()) {
            fileMemo = body.getMemo().getValue();
        }
        contents = body.getContents().toByteArray();
    }

    FileUpdateTransactionBody.Builder build() {
        var builder = FileUpdateTransactionBody.newBuilder();
        if (fileId != null) {
            builder.setFileID(fileId.toProtobuf());
        }
        if (keys != null) {
            builder.setKeys(keys.toProtobuf());
        }
        if (expirationTime != null) {
            builder.setExpirationTime(InstantConverter.toProtobuf(expirationTime));
        }
        builder.setContents(ByteString.copyFrom(contents));
        if (fileMemo != null) {
            builder.setMemo(StringValue.of(fileMemo));
        }

        return builder;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (fileId != null) {
            fileId.validateChecksum(client);
        }
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return FileServiceGrpc.getUpdateFileMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setFileUpdate(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setFileUpdate(build());
    }
}
