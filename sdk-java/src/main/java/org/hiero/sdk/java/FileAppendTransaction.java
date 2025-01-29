// SPDX-License-Identifier: Apache-2.0
package org.hiero.sdk.java;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.MethodDescriptor;
import java.util.LinkedHashMap;
import java.util.Objects;
import javax.annotation.Nullable;
import org.hiero.sdk.java.proto.FileAppendTransactionBody;
import org.hiero.sdk.java.proto.FileServiceGrpc;
import org.hiero.sdk.java.proto.SchedulableTransactionBody;
import org.hiero.sdk.java.proto.TransactionBody;
import org.hiero.sdk.java.proto.TransactionID;
import org.hiero.sdk.java.proto.TransactionResponse;

/**
 * <p>A transaction specifically to append data to a file on the network.
 *
 * <p>If a file has multiple keys, all keys must sign to modify its contents.
 * (See {@link FileCreateTransaction#setKeys(Key...)} for more information.)
 */
public final class FileAppendTransaction extends ChunkedTransaction<FileAppendTransaction> {
    static int DEFAULT_CHUNK_SIZE = 4096;

    @Nullable
    private FileId fileId = null;

    /**
     * Constructor.
     */
    public FileAppendTransaction() {
        super();

        defaultMaxTransactionFee = new Hbar(5);
        setChunkSize(2048);
    }

    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction)
     *            records
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    FileAppendTransaction(
            LinkedHashMap<TransactionId, LinkedHashMap<AccountId, org.hiero.sdk.java.proto.Transaction>> txs)
            throws InvalidProtocolBufferException {
        super(txs);

        initFromTransactionBody();
    }

    /**
     * Constructor.
     *
     * @param txBody protobuf TransactionBody
     */
    FileAppendTransaction(org.hiero.sdk.java.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    /**
     * Extract the file id.
     *
     * @return                          the file id
     */
    @Nullable
    public FileId getFileId() {
        return fileId;
    }

    /**
     * <p>Set the ID of the file to append to. Required.
     *
     * @param fileId the ID of the file to append to.
     * @return {@code this}
     */
    public FileAppendTransaction setFileId(FileId fileId) {
        Objects.requireNonNull(fileId);
        requireNotFrozen();
        this.fileId = fileId;
        return this;
    }

    /**
     * Extract the byte string representing the file.
     *
     * @return                          the byte string representing the file
     */
    @Nullable
    public ByteString getContents() {
        return getData();
    }

    /**
     * <p>Set the contents to append to the file as identified by {@link #setFileId(FileId)}.
     *
     * @param contents the contents to append to the file.
     * @return {@code this}
     * @see #setContents(String) for an overload which takes String.
     */
    public FileAppendTransaction setContents(byte[] contents) {
        return setData(contents);
    }

    /**
     * <p>Set the contents to append to the file as identified by {@link #setFileId(FileId)}.
     *
     * @param contents the contents to append to the file.
     * @return {@code this}
     * @see #setContents(String) for an overload which takes String.
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
     * @param text The String to be set as the contents of the file
     * @return {@code this}
     * @see #setContents(byte[]) for appending arbitrary data.
     */
    public FileAppendTransaction setContents(String text) {
        return setData(text);
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (fileId != null) {
            fileId.validateChecksum(client);
        }
    }

    @Override
    MethodDescriptor<org.hiero.sdk.java.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return FileServiceGrpc.getAppendContentMethod();
    }

    /**
     * Initialize from the transaction body.
     */
    void initFromTransactionBody() {
        var body = sourceTransactionBody.getFileAppend();
        if (body.hasFileID()) {
            fileId = FileId.fromProtobuf(body.getFileID());
        }

        if (!innerSignedTransactions.isEmpty()) {
            try {
                for (var i = 0;
                        i < innerSignedTransactions.size();
                        i += nodeAccountIds.isEmpty() ? 1 : nodeAccountIds.size()) {
                    data = data.concat(TransactionBody.parseFrom(
                                    innerSignedTransactions.get(i).getBodyBytes())
                            .getFileAppend()
                            .getContents());
                }
            } catch (InvalidProtocolBufferException exc) {
                throw new IllegalArgumentException(exc.getMessage());
            }
        } else {
            data = body.getContents();
        }
    }

    /**
     * Build the transaction body.
     *
     * @return {@link org.hiero.sdk.java.proto.FileAppendTransactionBody builder}
     */
    FileAppendTransactionBody.Builder build() {
        var builder = FileAppendTransactionBody.newBuilder();
        if (fileId != null) {
            builder.setFileID(fileId.toProtobuf());
        }
        builder.setContents(data);

        return builder;
    }

    @Override
    void onFreezeChunk(
            TransactionBody.Builder body,
            @Nullable TransactionID initialTransactionId,
            int startIndex,
            int endIndex,
            int chunk,
            int total) {
        body.setFileAppend(build().setContents(data.substring(startIndex, endIndex)));
    }

    @Override
    boolean shouldGetReceipt() {
        return true;
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setFileAppend(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setFileAppend(build().setContents(data));
    }
}
