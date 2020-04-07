package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.FileDeleteTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;

/**
 * <p>A transaction to delete a file on the Hedera network.
 *
 * <p>When deleted, a file's contents are truncated to zero length and it can no longer be updated
 * or appended to, or its expiration time extended. {@link FileContentsQuery} and {@link FileInfoQuery}
 * will throw {@link HederaPrecheckStatusException} with a status of {@link Status#FileDeleted}.
 *
 * <p>Only one of the file's keys needs to sign to delete the file, unless the key you have is part
 * of a {@link com.hedera.hashgraph.sdk.ThresholdKey}, in which case the threshold
 * must still be satisfied, or a {@link com.hedera.hashgraph.sdk.KeyList}, in which
 * case all keys in the list must sign.
 */
public final class FileDeleteTransaction extends TransactionBuilder<FileDeleteTransaction> {
    private final FileDeleteTransactionBody.Builder builder;

    public FileDeleteTransaction() {
        builder = FileDeleteTransactionBody.newBuilder();
    }

    @Override
    protected void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setFileDelete(builder);
    }

    /**
     * <p>Set the ID of the file to delete. Required.
     *
     * @param fileId the ID of the file to delete.
     * @return {@code this}
     */
    public FileDeleteTransaction setFileID(FileId fileId) {
        builder.setFileID(fileId.toProtobuf());
        return this;
    }
}
