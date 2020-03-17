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
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.crypto.PublicKey;

import java.time.Duration;
import java.time.Instant;

import io.grpc.MethodDescriptor;

/**
 * A transaction to create a file on the Hedera network.
 *
 * After executing, you can retrieve the file ID via
 * {@link com.hedera.hashgraph.sdk.TransactionId#getReceipt(Client)}
 * and then {@link TransactionReceipt#getFileId()}.
 */
public final class FileCreateTransaction extends TransactionBuilder<FileCreateTransaction> {
    private final FileCreateTransactionBody.Builder builder = bodyBuilder.getFileCreateBuilder();
    private final KeyList.Builder keyList = builder.getKeysBuilder();

    public FileCreateTransaction() {
        super();
        // Default expiration time to an acceptable value, 1/4 of a Julian year
        setExpirationTime(Instant.now().plus(Duration.ofMillis(7890000000L)));
    }

    /**
     * Set the instant at which this file will expire, after which its contents will no longer be
     * available.
     *
     * May not be in the past or else {@link #execute(Client)} will throw
     * {@link com.hedera.hashgraph.sdk.HederaStatusException}.
     *
     * Defaults to 1/4 of a Julian year from the instant {@link #FileCreateTransaction()}
     * was invoked.
     *
     * May be extended using {@link FileUpdateTransaction#setExpirationTime(Instant)}.
     *
     * @param expiration the {@link Instant} at which this file should expire.
     * @return {@code this} for fluent API usage.
     */
    public FileCreateTransaction setExpirationTime(Instant expiration) {
        builder.setExpirationTime(TimestampHelper.timestampFrom(expiration));
        return this;
    }

    /**
     * Add a key which must sign any transactions modifying this file. Required.
     *
     * All keys must sign to modify the file's contents or keys. No key is required
     * to sign for extending the expiration time (except the one for the operator account
     * paying for the transaction). Only one key must sign to delete the file, however.
     *
     * To require more than one key to sign to delete a file, add them to a
     * {@link com.hedera.hashgraph.sdk.crypto.ThresholdKey} and pass that here; to require all of
     * them to sign, add them to a {@link com.hedera.hashgraph.sdk.crypto.KeyList} and pass that.
     *
     * The network currently requires a file to have at least one key (or key list or threshold key)
     * but this requirement may be lifted in the future.
     */
    public FileCreateTransaction addKey(PublicKey key) {
        keyList.addKeys(key.toKeyProto());
        return this;
    }

    /**
     * Set the given byte array as the file's contents.
     *
     * This may be omitted to create an empty file.
     *
     * Note that total size for a given transaction is limited to 6KiB (as of March 2020) by the
     * network; if you exceed this you may receive a {@link com.hedera.hashgraph.sdk.HederaStatusException}
     * with {@link com.hedera.hashgraph.sdk.Status#TransactionOversize}.
     *
     * In this case, you will need to break the data into chunks of less than ~6KiB and execute this
     * transaction with the first chunk and then use {@link FileAppendTransaction} with
     * {@link FileAppendTransaction#setContents(byte[])} for the remaining chunks.
     *
     * @param bytes the contents of the file.
     * @return {@code this} for fluent API usage.
     */
    public FileCreateTransaction setContents(byte[] bytes) {
        builder.setContents(ByteString.copyFrom(bytes));
        return this;
    }

    /**
     * Encode the given {@link String} as UTF-8 and set it as the file's contents.
     *
     * This may be omitted to create an empty file.
     *
     * The string can later be recovered from {@link FileContentsQuery#execute(Client)}
     * via {@link String#String(byte[], java.nio.charset.Charset)} using
     * {@link java.nio.charset.StandardCharsets#UTF_8}.
     *
     * Note that total size for a given transaction is limited to 6KiB (as of March 2020) by the
     * network; if you exceed this you may receive a {@link com.hedera.hashgraph.sdk.HederaStatusException}
     * with {@link com.hedera.hashgraph.sdk.Status#TransactionOversize}.
     *
     * In this case, you will need to break the data into chunks of less than ~6KiB and execute this
     * transaction with the first chunk and then use {@link FileAppendTransaction} with
     * {@link FileAppendTransaction#setContents(String)} for the remaining chunks.
     */
    public FileCreateTransaction setContents(String text) {
        builder.setContents(ByteString.copyFromUtf8(text));
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return FileServiceGrpc.getCreateFileMethod();
    }

    @Override
    protected void doValidate() {
        // file without contents is allowed
        require(!keyList.getKeysOrBuilderList().isEmpty(),
                "network currently requires files to have at least one key");
    }
}
