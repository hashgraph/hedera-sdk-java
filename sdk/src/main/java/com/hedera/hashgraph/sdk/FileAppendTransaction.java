package com.hedera.hashgraph.sdk;

import com.google.errorprone.annotations.Var;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import java8.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;
import java.util.*;

/**
 * <p>A transaction specifically to append data to a file on the network.
 *
 * <p>If a file has multiple keys, all keys must sign to modify its contents.
 * (See {@link FileCreateTransaction#setKeys(Key...)} for more information.)
 */
public final class FileAppendTransaction extends ChunkedTransaction<FileAppendTransaction> {
    private final FileAppendTransactionBody.Builder builder;

    public FileAppendTransaction() {
        super();

        builder = FileAppendTransactionBody.newBuilder();

        setMaxTransactionFee(new Hbar(5));
    }

    FileAppendTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getFileAppend().toBuilder();

        for (var i = 0; i < signedTransactions.size(); i += nodeAccountIds.size()) {
            data = data.concat(
                TransactionBody.parseFrom(signedTransactions.get(i).getBodyBytes())
                    .getFileAppend().getContents()
            );
        }
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
        return getData();
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
        return setData(contents);
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
    public FileAppendTransaction setContents(ByteString contents) {
        return setData(contents);
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
        return setData(text);
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return FileServiceGrpc.getAppendContentMethod();
    }

    @Override
    void onFreezeChunk(TransactionBody.Builder body, TransactionID initialTransactionId, int startIndex, int endIndex, int chunk, int total) {
        body.setFileAppend(builder.setContents(data.substring(startIndex, endIndex)));
    }

    boolean shouldGetReceipt() {
        return true;
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setFileAppend(builder);
        return true;
    }
}
