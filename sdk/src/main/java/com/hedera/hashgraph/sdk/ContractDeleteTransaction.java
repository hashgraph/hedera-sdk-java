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

    @Nullable
    private ContractId contractId = null;
    @Nullable
    private ContractId transferContractId = null;
    @Nullable
    private AccountId transferAccountId = null;

    public ContractDeleteTransaction() {
    }

    ContractDeleteTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    ContractDeleteTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
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
    void validateChecksums(Client client) throws BadEntityIdException {
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

    void initFromTransactionBody() {
        var body = txBody.getContractDeleteInstance();
        if(body.hasContractID()) {
            contractId = ContractId.fromProtobuf(body.getContractID());
        }
        if(body.hasTransferContractID()) {
            transferContractId = ContractId.fromProtobuf(body.getTransferContractID());
        }
        if(body.hasTransferAccountID()) {
            transferAccountId = AccountId.fromProtobuf(body.getTransferAccountID());
        }
    }

    ContractDeleteTransactionBody.Builder build() {
        var builder = ContractDeleteTransactionBody.newBuilder();
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
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setContractDeleteInstance(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setContractDeleteInstance(build());
    }
}
