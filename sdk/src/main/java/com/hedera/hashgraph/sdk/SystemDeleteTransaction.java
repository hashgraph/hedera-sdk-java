package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.time.Instant;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;

/**
 * Delete a file or smart contract - can only be done with a Hedera admin.
 * <p>
 * When it is deleted, it immediately disappears from the system as seen by the user,
 * but is still stored internally until the expiration time, at which time it
 * is truly and permanently deleted.
 * <p>
 * Until that time, it can be undeleted by the Hedera admin.
 * When a smart contract is deleted, the cryptocurrency account within it continues
 * to exist, and is not affected by the expiration time here.
 */
public final class SystemDeleteTransaction extends Transaction<SystemDeleteTransaction> {
    private final SystemDeleteTransactionBody.Builder builder;

    FileId fileId;
    ContractId contractId;

    public SystemDeleteTransaction() {
        builder = SystemDeleteTransactionBody.newBuilder();
    }

    SystemDeleteTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getSystemDelete().toBuilder();
    }

    SystemDeleteTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);

        builder = bodyBuilder.getSystemDelete().toBuilder();
    }

    @Nullable
    public final FileId getFileId() {
        return fileId;
    }

    /**
     * Sets the file ID to delete.
     * <p>
     * Mutually exclusive with {@link #setContractId(ContractId)}.
     *
     * @param fileId The FileId to be set
     * @return {@code this}
     */
    public SystemDeleteTransaction setFileId(FileId fileId) {
        requireNotFrozen();
        this.fileId = fileId;
        return this;
    }

    @Nullable
    public final ContractId getContractId() {
        return contractId;
    }

    /**
     * Sets the contract ID to delete.
     * <p>
     * Mutually exclusive with {@link #setFileId(FileId)}.
     *
     * @param contractId The ContractId to be set
     * @return {@code this}
     */
    public SystemDeleteTransaction setContractId(ContractId contractId) {
        requireNotFrozen();
        this.contractId = contractId;
        return this;
    }

    @Nullable
    public final Instant getExpirationTime() {
        return builder.hasExpirationTime() ? InstantConverter.fromProtobuf(builder.getExpirationTime()) : null;
    }

    /**
     * Sets the timestamp at which the "deleted" file should
     * truly be permanently deleted.
     *
     * @param expirationTime The Instant to be set as expiration time
     * @return {@code this}
     */
    public SystemDeleteTransaction setExpirationTime(Instant expirationTime) {
        requireNotFrozen();

        builder.setExpirationTime(TimestampSeconds.newBuilder()
            .setSeconds(expirationTime.getEpochSecond())
            .build());

        return this;
    }

    SystemDeleteTransactionBody.Builder build() {
        if (fileId != null) {
            builder.setFileID(fileId.toProtobuf());
        }

        if (contractId != null) {
            builder.setContractID(contractId.toProtobuf());
        }

        return builder;
    }

    @Override
    void validateNetworkOnIds(Client client) {
        if (fileId != null) {
            fileId.validate(client);
        }

        if (contractId != null) {
            contractId.validate(client);
        }
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        switch (builder.getIdCase()) {
            case FILEID:
                return FileServiceGrpc.getSystemDeleteMethod();

            case CONTRACTID:
                return SmartContractServiceGrpc.getSystemDeleteMethod();

            default:
                throw new IllegalStateException("requires an ID to be set, try calling setFileId or setContractId");
        }
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setSystemDelete(build());
        return true;
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setSystemDelete(build());
    }
}
