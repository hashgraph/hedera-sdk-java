package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.FileAppendTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;

/**
 * <p>A transaction specifically to append data to a file on the network.
 *
 * <p>If a file has multiple keys, all keys must sign to modify its contents.
 * (See {@link FileCreateTransaction#setKeys(Key...)} for more information.)
 */
public final class FileAppendTransaction extends TransactionBuilder<FileAppendTransaction> {
    private final FileAppendTransactionBody.Builder builder;

    public FileAppendTransaction() {
        builder = FileAppendTransactionBody.newBuilder();
    }

    @Override
    void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setFileAppend(builder);
    }

    /**
     * <p>Set the ID of the file to append to. Required.
     *
     * @param fileId the ID of the file to append to.
     * @return {@code this} for fluent API usage.
     */
    public FileAppendTransaction setFileId(FileId fileId) {
        builder.setFileID(fileId.toProtobuf());
        return this;
    }

    /**
     * <p>Set the contents to append to the file as identified by {@link #setFileId(FileId)}.
     *
     * <p>Note that total size for a given transaction is limited to 6KiB (as of March 2020) by the
     * network; if you exceed this you may receive a {@link com.hedera.hashgraph.sdk.HederaPreCheckStatusException}
     * with {@link com.hedera.hashgraph.sdk.Status#TransactionOversize}.
     *
     * <p>If you want to append more than ~6KiB of data, you will need to break it into multiple chunks
     * and use a separate {@link FileAppendTransaction} for each.
     *
     * @param contents the contents to append to the file.
     * @see #setContents(String) for an overload which takes {@link String}.
     * @return {@code this}
     */
    public FileAppendTransaction setContents(byte[] contents) {
        builder.setContents(ByteString.copyFrom(contents));
        return this;
    }

    /**
     * <p>Encode the given {@link String} as UTF-8 and append it to file as identified by
     * {@link #setFileId(FileId)}.
     *
     * <p>If the whole file is UTF-8 encoded, the string can later be recovered from
     * {@link FileContentsQuery#execute(Client)} via
     * {@link String#String(byte[], java.nio.charset.Charset)} using
     * {@link java.nio.charset.StandardCharsets#UTF_8}.
     *
     * <p>Note that total size for a given transaction is limited to 6KiB (as of March 2020) by the
     * network; if you exceed this you may receive a {@link com.hedera.hashgraph.sdk.HederaPreCheckStatusException}
     * with {@link com.hedera.hashgraph.sdk.Status#TransactionOversize}.
     *
     * <p>If you want to append more than ~6KiB of data, you will need to break it into multiple chunks
     * and use a separate {@link FileAppendTransaction} for each.
     *
     * @see #setContents(byte[]) for appending arbitrary data.
     * @return {@code this}
     */
    public FileAppendTransaction setContents(String text) {
        builder.setContents(ByteString.copyFromUtf8(text));
        return this;
    }
}
