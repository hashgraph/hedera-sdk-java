package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.FileCreateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import org.threeten.bp.Instant;

import java.util.ArrayList;

public final class FileCreateTransaction extends TransactionBuilder<FileCreateTransaction> {
    private final FileCreateTransactionBody.Builder builder;

    public FileCreateTransaction() {
        builder = FileCreateTransactionBody.newBuilder();

        setExpirationTime(Instant.now().plus(DEFAULT_AUTO_RENEW_PERIOD));
    }

    @Override
    void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setFileCreate(builder);
    }

    /**
     * <p>Set the instant at which this file will expire, after which its contents will no longer be
     * available.
     *
     * <p>Defaults to 1/4 of a Julian year from the instant {@link #FileCreateTransaction()}
     * was invoked.
     *
     * <p>May be extended using {@link FileUpdateTransaction#setExpirationTime(Instant)}.
     *
     * @param expirationTime the {@link Instant} at which this file should expire.
     * @return {@code this}
     */
    public FileCreateTransaction setExpirationTime(Instant expirationTime) {
        builder.setExpirationTime(InstantConverter.toProtobuf(expirationTime));

        return this;
    }

    /**
     * <p>Set the keys which must sign any transactions modifying this file. Required.
     *
     * <p>All keys must sign to modify the file's contents or keys. No key is required
     * to sign for extending the expiration time (except the one for the operator account
     * paying for the transaction). Only one key must sign to delete the file, however.
     *
     * <p>To require more than one key to sign to delete a file, add them to a
     * {@link com.hedera.hashgraph.sdk.KeyList} and pass that here.
     *
     * <p>The network currently requires a file to have at least one key (or key list or threshold key)
     * but this requirement may be lifted in the future.
     *
     * @return {@code this}
     */
    public FileCreateTransaction setKeys(Key... keys) {
        var keyList = com.hedera.hashgraph.sdk.proto.KeyList.newBuilder();
        for (Key key: keys) {
            keyList.addKeys(key.toKeyProtobuf());
        }
        builder.setKeys(keyList);
        return this;
    }

    /**
     * <p>Set the given byte array as the file's contents.
     *
     * <p>This may be omitted to create an empty file.
     *
     * <p>Note that total size for a given transaction is limited to 6KiB (as of March 2020) by the
     * network; if you exceed this you may receive a {@link com.hedera.hashgraph.sdk.HederaPreCheckStatusException}
     * with {@link com.hedera.hashgraph.sdk.Status#TransactionOversize}.
     *
     * <p>In this case, you will need to break the data into chunks of less than ~6KiB and execute this
     * transaction with the first chunk and then use {@link FileAppendTransaction} with
     * {@link FileAppendTransaction#setContents(byte[])} for the remaining chunks.
     *
     * @param bytes the contents of the file.
     * @return {@code this}
     */
    public FileCreateTransaction setContents(byte[] bytes) {
        builder.setContents(ByteString.copyFrom(bytes));
        return this;
    }

    /**
     * <p>Encode the given {@link String} as UTF-8 and set it as the file's contents.
     *
     * <p>This may be omitted to create an empty file.
     *
     * <p>The string can later be recovered from {@link FileContentsQuery#execute(Client)}
     * via {@link String#String(byte[], java.nio.charset.Charset)} using
     * {@link java.nio.charset.StandardCharsets#UTF_8}.
     *
     * <p>Note that total size for a given transaction is limited to 6KiB (as of March 2020) by the
     * network; if you exceed this you may receive a {@link com.hedera.hashgraph.sdk.HederaPreCheckStatusException}
     * with {@link com.hedera.hashgraph.sdk.Status#TransactionOversize}.
     *
     * <p>In this case, you will need to break the data into chunks of less than ~6KiB and execute this
     * transaction with the first chunk and then use {@link FileAppendTransaction} with
     * {@link FileAppendTransaction#setContents(String)} for the remaining chunks.
     *
     * @param text the contents of the file.
     * @return {@code this}
     */
    public FileCreateTransaction setContents(String text) {
        builder.setContents(ByteString.copyFromUtf8(text));
        return this;
    }
}
