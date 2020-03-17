package com.hedera.hashgraph.sdk.file;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.proto.FileServiceGrpc;
import com.hedera.hashgraph.proto.FileUpdateTransactionBody;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hedera.hashgraph.sdk.crypto.PublicKey;

import java.time.Instant;

import io.grpc.MethodDescriptor;

/**
 * Update a file on the Hedera network to replace its contents or keys, or extend its expiration
 * time.
 * <p>
 * If a file has multiple keys, all keys must sign to update the file.
 * (See {@link FileCreateTransaction#addKey(PublicKey)} for more information.)
 */
public class FileUpdateTransaction extends TransactionBuilder<FileUpdateTransaction> {
    private final FileUpdateTransactionBody.Builder builder = bodyBuilder.getFileUpdateBuilder();
    private com.hedera.hashgraph.proto.KeyList.Builder keyList = builder.getKeysBuilder();

    public FileUpdateTransaction() { super(); }

    /**
     * Set the ID of the file to update; required.
     *
     * @param file the ID of the file to update.
     * @return {@code this} for fluent API usage.
     */
    public FileUpdateTransaction setFileId(FileId file) {
        builder.setFileID(file.toProto());

        return this;
    }

    /**
     * If set, update the expiration time of the file.
     * <p>
     * Must be in the future (may only be used to extend the expiration).
     * To make a file inaccessible use {@link FileDeleteTransaction} instead.
     *
     * @param expiration the new {@link Instant} at which the transaction will expire.
     * @return {@code}
     */
    public FileUpdateTransaction setExpirationTime(Instant expiration) {
        builder.setExpirationTime(TimestampHelper.timestampFrom(expiration));

        return this;
    }

    /**
     * <b>NOTICE: misleading method name!</b>
     * <p>
     * Add a key to the list in the transaction which will <i>replace</i> the current list on the
     * file. If not called, the file's keys are not changed.
     * <p>
     * If you want to merely add a key to a file's existing list of keys, you must first
     * query the file's existing list with {@link FileInfoQuery}, add all the keys in
     * {@link FileInfo#keys} to this transaction, and then add your new key.
     * <p>
     * Expect this method to be deprecated in favor of a replacement with clearer semantics in a
     * future release.
     * @see FileCreateTransaction#addKey(PublicKey) for information on keys associated with files.
     */
    public FileUpdateTransaction addKey(PublicKey key) {
        keyList.addKeys(key.toKeyProto());
        return this;
    }

    /**
     * If set, replace contents of the file identified by {@link #setFileId(FileId)}
     * with the given bytes.
     * <p>
     * If the contents of the file are longer than the given byte array, then the file will
     * be truncated.
     * <p>
     * Note that total size for a given transaction is limited to 6KiB (as of March 2020) by the
     * network; if you exceed this you may receive a {@link HederaStatusException}
     * with {@link Status#TransactionOversize}.
     * <p>
     * In this case, you will need to break the data into chunks of less than ~6KiB and execute this
     * transaction with the first chunk and then use {@link FileAppendTransaction} with
     * {@link FileAppendTransaction#setContents(byte[])} for the remaining chunks.
     *
     * @param bytes the bytes to replace the contents of the file with.
     * @return {@code this} for fluent API usage.
     * @see #setContents(String) for an overload which takes a {@link String}.
     * @see FileAppendTransaction if you merely want to add data to a file's existing contents.
     */
    public FileUpdateTransaction setContents(byte[] bytes) {
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
     * network; if you exceed this you may receive a {@link HederaStatusException}
     * with {@link Status#TransactionOversize}.
     * <p>
     * In this case, you will need to break the data into chunks of less than ~6KiB and execute this
     * transaction with the first chunk and then use {@link FileAppendTransaction} with
     * {@link FileAppendTransaction#setContents(String)} for the remaining chunks.
     *
     * @param text the string to replace the contents of the file with.
     * @return {@code this} for fluent API usage.
     * @see #setContents(byte[]) for replacing the contents with arbitrary data.
     * @see FileAppendTransaction if you merely want to add data to a file's existing contents.
     */
    public FileUpdateTransaction setContents(String text) {
        builder.setContents(ByteString.copyFromUtf8(text));
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return FileServiceGrpc.getUpdateFileMethod();
    }

    @Override
    protected void doValidate() {
        require(builder.hasFileID(), ".setFileId() required");
    }
}
