package com.hedera.hashgraph.sdk.file;

import com.hedera.hashgraph.proto.FileDeleteTransactionBody;
import com.hedera.hashgraph.proto.FileServiceGrpc;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.HederaPrecheckStatusException;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TransactionBuilder;

import io.grpc.MethodDescriptor;

/**
 * A transaction to delete a file on the Hedera network.
 *
 * When deleted, a file's contents are truncated to zero length and it can no longer be updated
 * or appended to, or its expiration time extended. {@link FileContentsQuery} and {@link FileInfoQuery}
 * will throw {@link HederaPrecheckStatusException} with a status of {@link Status#FileDeleted}.
 *
 * Only one of the file's keys needs to sign to delete the file, unless the key you have is part
 * of a {@link com.hedera.hashgraph.sdk.crypto.ThresholdKey}, in which case the threshold
 * must still be satisfied, or a {@link com.hedera.hashgraph.sdk.crypto.KeyList}, in which
 * case all keys in the list must sign.
 */
public final class FileDeleteTransaction extends TransactionBuilder<FileDeleteTransaction> {
    private final FileDeleteTransactionBody.Builder builder = bodyBuilder.getFileDeleteBuilder();

    public FileDeleteTransaction() { super(); }

    /**
     * Set the ID of the file to delete. Required.
     *
     * @param fileId the ID of the file to delete.
     * @return {@code this} for fluent API usage.
     */
    public FileDeleteTransaction setFileId(FileId fileId) {
        builder.setFileID(fileId.toProto());
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return FileServiceGrpc.getDeleteFileMethod();
    }

    @Override
    protected void doValidate() {
        require(builder.hasFileID(), ".setFileId() required");
    }
}
