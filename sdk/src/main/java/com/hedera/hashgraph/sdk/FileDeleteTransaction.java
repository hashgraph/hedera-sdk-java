package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;

/**
 * <p>A transaction to delete a file on the Hedera network.
 *
 * <p>When deleted, a file's contents are truncated to zero length and it can no longer be updated
 * or appended to, or its expiration time extended. {@link FileContentsQuery} and {@link FileInfoQuery}
 * will throw {@link PrecheckStatusException} with a status of {@link Status#FILE_DELETED}.
 *
 * <p>Only one of the file's keys needs to sign to delete the file, unless the key you have is part
 * of a {@link com.hedera.hashgraph.sdk.KeyList}.
 */
public final class FileDeleteTransaction extends Transaction<FileDeleteTransaction> {
    private final FileDeleteTransactionBody.Builder builder;

    public FileDeleteTransaction() {
        builder = FileDeleteTransactionBody.newBuilder();
    }

    FileDeleteTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getFileDelete().toBuilder();
    }

    FileDeleteTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);

        builder = bodyBuilder.getFileDelete().toBuilder();
    }

    @Nullable
    public FileId getFileId() {
        return builder.hasFileID() ? FileId.fromProtobuf(builder.getFileID()) : null;
    }

    /**
     * <p>Set the ID of the file to delete. Required.
     *
     * @param fileId the ID of the file to delete.
     * @return {@code this}
     */
    public FileDeleteTransaction setFileId(FileId fileId) {
        requireNotFrozen();
        builder.setFileID(fileId.toProtobuf());
        return this;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return FileServiceGrpc.getDeleteFileMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setFileDelete(builder);
        return true;
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setFileDelete(builder);
    }
}
