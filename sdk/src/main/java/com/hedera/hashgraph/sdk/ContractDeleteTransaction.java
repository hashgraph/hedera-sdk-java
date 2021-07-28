package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.ContractDeleteTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.SmartContractServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Marks a contract as deleted, moving all its current hbars to another account.
 */
public final class ContractDeleteTransaction extends Transaction<ContractDeleteTransaction> {
    private final ContractDeleteTransactionBody.Builder builder;

    @Nullable
    ContractId contractId = null;
    @Nullable
    ContractId transferContractId = null;
    @Nullable
    AccountId transferAccountId = null;

    public ContractDeleteTransaction() {
        builder = ContractDeleteTransactionBody.newBuilder();
    }

    ContractDeleteTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getContractDeleteInstance().toBuilder();

        if(builder.hasContractID()) {
            contractId = ContractId.fromProtobuf(builder.getContractID());
        }

        if(builder.hasTransferContractID()) {
            transferContractId = ContractId.fromProtobuf(builder.getTransferContractID());
        }

        if(builder.hasTransferAccountID()) {
            transferAccountId = AccountId.fromProtobuf(builder.getTransferAccountID());
        }
    }

    ContractDeleteTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);

        builder = bodyBuilder.getContractDeleteInstance().toBuilder();

        if(builder.hasContractID()) {
            contractId = ContractId.fromProtobuf(builder.getContractID());
        }

        if(builder.hasTransferContractID()) {
            transferContractId = ContractId.fromProtobuf(builder.getTransferContractID());
        }

        if(builder.hasTransferAccountID()) {
            transferAccountId = AccountId.fromProtobuf(builder.getTransferAccountID());
        }
    }

    @Nullable
    public ContractId getContractId() {
        return contractId;
    }

    /**
     * Sets the contract ID which should be deleted.
     *
     * @param contractId The ContractId to be set
     * @return {@code this}
     */
    public ContractDeleteTransaction setContractId(ContractId contractId) {
        Objects.requireNonNull(contractId);
        requireNotFrozen();
        this.contractId = contractId;
        return this;
    }

    @Nullable
    public AccountId getTransferAccountId() {
        return transferAccountId;
    }

    /**
     * Sets the account ID which will receive all remaining hbars.
     * <p>
     * This is mutually exclusive with {@link #setTransferContractId(ContractId)}.
     *
     * @param transferAccountId The AccountId to be set
     * @return {@code this}
     */
    public ContractDeleteTransaction setTransferAccountId(AccountId transferAccountId) {
        Objects.requireNonNull(transferAccountId);
        requireNotFrozen();
        this.transferAccountId = transferAccountId;
        return this;
    }

    @Nullable
    public ContractId getTransferContractId() {
        return transferContractId;
    }

    /**
     * Sets the contract ID which will receive all remaining hbars.
     * <p>
     * This is mutually exclusive with {@link #setTransferAccountId(AccountId)}.
     *
     * @param transferContractId The ContractId to be set
     * @return {@code this}
     */
    public ContractDeleteTransaction setTransferContractId(ContractId transferContractId) {
        Objects.requireNonNull(transferContractId);
        requireNotFrozen();
        this.transferContractId = transferContractId;
        return this;
    }

    @Override
    void validateChecksums(Client client) throws InvalidChecksumException {
        if (contractId != null) {
            contractId.validateChecksum(client);
        }

        if (transferContractId != null) {
            transferContractId.validateChecksum(client);
        }

        if (transferAccountId != null) {
            transferAccountId.validateChecksum(client);
        }
    }


    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return SmartContractServiceGrpc.getDeleteContractMethod();
    }

    ContractDeleteTransactionBody.Builder build() {
        if (contractId != null) {
            builder.setContractID(contractId.toProtobuf());
        }

        if (transferAccountId != null) {
            builder.setTransferAccountID(transferAccountId.toProtobuf());
        }

        if (transferContractId != null) {
            builder.setTransferContractID(transferContractId.toProtobuf());
        }

        return builder;
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setContractDeleteInstance(build());
        return true;
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setContractDeleteInstance(build());
    }
}
