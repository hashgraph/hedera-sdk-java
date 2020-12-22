package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.FileServiceGrpc;
import com.hedera.hashgraph.sdk.proto.FileUpdateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * Updates a file by submitting the transaction.
 */
public final class FileUpdateTransaction extends Transaction<FileUpdateTransaction> {
    private final FileUpdateTransactionBody.Builder builder;

    public FileUpdateTransaction() {
        builder = FileUpdateTransactionBody.newBuilder();
    }

    FileUpdateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getFileUpdate().toBuilder();
    }

    @Nullable
    public FileId getFileId() {
        return builder.hasFileID() ? FileId.fromProtobuf(builder.getFileID()) : null;
    }

    /**
     * Set the ID of the file to update; required.
     *
     * @param fileId the ID of the file to update.
     * @return {@code this}
     */
    public FileUpdateTransaction setFileId(FileId fileId) {
        requireNotFrozen();
        builder.setFileID(fileId.toProtobuf());
        return this;
    }

    public Collection<Key> getKeys() {
        return KeyList.fromProtobuf(builder.getKeys(), null);
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
        requireNotFrozen();
        builder.setContents(ByteString.copyFromUtf8(text));
        return this;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return FileServiceGrpc.getUpdateFileMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setFileUpdate(builder);
        return true;
    }
}
