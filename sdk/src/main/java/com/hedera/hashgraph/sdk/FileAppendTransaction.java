package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.FileAppendTransactionBody;
import com.hedera.hashgraph.sdk.proto.FileServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;

/**
 * <p>A transaction specifically to append data to a file on the network.
 *
 * <p>If a file has multiple keys, all keys must sign to modify its contents.
 * (See {@link FileCreateTransaction#setKeys(Key...)} for more information.)
 */
public final class FileAppendTransaction extends Transaction<FileAppendTransaction> {
    private final FileAppendTransactionBody.Builder builder;

    public FileAppendTransaction() {
        builder = FileAppendTransactionBody.newBuilder();
    }

    FileAppendTransaction(TransactionBody body) {
        super(body);

        builder = body.getFileAppend().toBuilder();
    }

    @Nullable
    public FileId getFileId() {
        return builder.hasFileID() ? FileId.fromProtobuf(builder.getFileID()) : null;
    }

    /**
     * <p>Set the ID of the file to append to. Required.
     *
     * @param fileId the ID of the file to append to.
     * @return {@code this}
     */
    public FileAppendTransaction setFileId(FileId fileId) {
        requireNotFrozen();
        builder.setFileID(fileId.toProtobuf());
        return this;
    }

    @Nullable
    public ByteString getContents() {
        return builder.getContents();
    }

    /**
     * <p>Set the contents to append to the file as identified by {@link #setFileId(FileId)}.
     *
     * <p>Note that total size for a given transaction is limited to 6KiB (as of March 2020) by the
     * network; if you exceed this you may receive a {@link com.hedera.hashgraph.sdk.HederaPreCheckStatusException}
     * with {@link com.hedera.hashgraph.sdk.Status#TRANSACTION_OVERSIZE}.
     *
     * <p>If you want to append more than ~6KiB of data, you will need to break it into multiple chunks
     * and use a separate {@link FileAppendTransaction} for each.
     *
     * @param contents the contents to append to the file.
     * @return {@code this}
     * @see #setContents(String) for an overload which takes {@link String}.
     */
    public FileAppendTransaction setContents(byte[] contents) {
        requireNotFrozen();
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
     * with {@link com.hedera.hashgraph.sdk.Status#TRANSACTION_OVERSIZE}.
     *
     * <p>If you want to append more than ~6KiB of data, you will need to break it into multiple chunks
     * and use a separate {@link FileAppendTransaction} for each.
     *
     * @param text The String to be set as the contents of the file
     * @return {@code this}
     * @see #setContents(byte[]) for appending arbitrary data.
     */
    public FileAppendTransaction setContents(String text) {
        requireNotFrozen();
        builder.setContents(ByteString.copyFromUtf8(text));
        return this;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return FileServiceGrpc.getAppendContentMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setFileAppend(builder);
        return true;
    }
}
