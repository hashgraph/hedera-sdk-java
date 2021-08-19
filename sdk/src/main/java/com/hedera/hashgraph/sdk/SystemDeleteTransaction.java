package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.FileServiceGrpc;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.SmartContractServiceGrpc;
import com.hedera.hashgraph.sdk.proto.SystemDeleteTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Objects;

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
    @Nullable
    private FileId fileId = null;
    @Nullable
    private ContractId contractId = null;
    @Nullable
    private Instant expirationTime = null;

    public SystemDeleteTransaction() {
    }

    SystemDeleteTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    SystemDeleteTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
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
        Objects.requireNonNull(fileId);
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
        Objects.requireNonNull(contractId);
        requireNotFrozen();
        this.contractId = contractId;
        return this;
    }

    @Nullable
    public final Instant getExpirationTime() {
        return expirationTime;
    }

    /**
     * Sets the timestamp at which the "deleted" file should
     * truly be permanently deleted.
     *
     * @param expirationTime The Instant to be set as expiration time
     * @return {@code this}
     */
    public SystemDeleteTransaction setExpirationTime(Instant expirationTime) {
        Objects.requireNonNull(expirationTime);
        requireNotFrozen();

        this.expirationTime = expirationTime;

        return this;
    }

    SystemDeleteTransactionBody.Builder build() {
        var builder = SystemDeleteTransactionBody.newBuilder();
        if (fileId != null) {
            builder.setFileID(fileId.toProtobuf());
        }
        if (contractId != null) {
            builder.setContractID(contractId.toProtobuf());
        }
        if (expirationTime != null) {
            builder.setExpirationTime(InstantConverter.toSecondsProtobuf(expirationTime));
        }

        return builder;
    }

    void initFromTransactionBody() {
        var body = sourceTransactionBody.getSystemDelete();
        if (body.hasFileID()) {
            fileId = FileId.fromProtobuf(body.getFileID());
        }
        if (body.hasContractID()) {
            contractId = ContractId.fromProtobuf(body.getContractID());
        }
        if (body.hasExpirationTime()) {
            expirationTime = InstantConverter.fromProtobuf(body.getExpirationTime());
        }
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (fileId != null) {
            fileId.validateChecksum(client);
        }

        if (contractId != null) {
            contractId.validateChecksum(client);
        }
    }

    @Override
    CompletableFuture<Void> onExecuteAsync(Client client) {
        int modesEnabled = (fileId != null ? 1 : 0) + (contractId != null ? 1 : 0);
        if (modesEnabled != 1) {
            throw new IllegalStateException("SystemDeleteTransaction must have exactly 1 of the following fields set: contractId, fileId");
        }
        return super.onExecuteAsync(client);
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        if (fileId != null) {
            return FileServiceGrpc.getSystemDeleteMethod();
        } else {
            return SmartContractServiceGrpc.getSystemDeleteMethod();
        }
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setSystemDelete(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setSystemDelete(build());
    }
}
