package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.ContractDeleteTransactionBody;
import com.hedera.hashgraph.sdk.proto.SmartContractServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.HashMap;

/**
 * Marks a contract as deleted, moving all its current hbars to another account.
 */
public final class ContractDeleteTransaction extends Transaction<ContractDeleteTransaction> {
    private final ContractDeleteTransactionBody.Builder builder;

    public ContractDeleteTransaction() {
        builder = ContractDeleteTransactionBody.newBuilder();
    }

    ContractDeleteTransaction(HashMap<TransactionId, HashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs.values().iterator().next());

        builder = bodyBuilder.getContractDeleteInstance().toBuilder();
    }

    @Nullable
    public ContractId getContractId() {
        return builder.hasContractID() ? ContractId.fromProtobuf(builder.getContractID()) : null;
    }

    /**
     * Sets the contract ID which should be deleted.
     *
     * @param contractId The ContractId to be set
     * @return {@code this}
     */
    public ContractDeleteTransaction setContractId(ContractId contractId) {
        requireNotFrozen();
        builder.setContractID(contractId.toProtobuf());
        return this;
    }

    @Nullable
    public AccountId getTransferAccountId() {
        return builder.hasTransferAccountID() ? AccountId.fromProtobuf(builder.getTransferAccountID()) : null;
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
        requireNotFrozen();
        builder.setTransferAccountID(transferAccountId.toProtobuf());
        return this;
    }

    @Nullable
    public ContractId getTransferContractId() {
        return builder.hasTransferContractID() ? ContractId.fromProtobuf(builder.getTransferContractID()) : null;
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
        requireNotFrozen();
        builder.setTransferContractID(transferContractId.toProtobuf());
        return this;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return SmartContractServiceGrpc.getDeleteContractMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setContractDeleteInstance(builder);
        return true;
    }
}
