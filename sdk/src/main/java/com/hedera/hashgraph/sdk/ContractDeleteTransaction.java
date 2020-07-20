package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ContractDeleteTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;

/**
 * Marks a contract as deleted, moving all its current hbars to another account.
 */
public final class ContractDeleteTransaction extends SingleTransactionBuilder<ContractDeleteTransaction> {
    private final ContractDeleteTransactionBody.Builder builder;

    public ContractDeleteTransaction() {
        builder = ContractDeleteTransactionBody.newBuilder();
    }

    /**
     * Sets the contract ID which should be deleted.
     *
     * @return {@code this}
     * @param contractId The ContractId to be set
     */
    public ContractDeleteTransaction setContractId(ContractId contractId) {
        builder.setContractID(contractId.toProtobuf());
        return this;
    }

    /**
     * Sets the account ID which will receive all remaining hbars.
     *
     * This is mutually exclusive with {@link #setTransferContractId(ContractId)}.
     *
     * @return {@code this}
     * @param transferAccountId The AccountId to be set
     */
    public ContractDeleteTransaction setTransferAccountId(AccountId transferAccountId) {
        builder.setTransferAccountID(transferAccountId.toProtobuf());
        return this;
    }

    /**
     * Sets the contract ID which will receive all remaining hbars.
     *
     * This is mutually exclusive with {@link #setTransferAccountId(AccountId)}.
     *
     * @return {@code this}
     * @param transferContractId The ContractId to be set
     */
    public ContractDeleteTransaction setTransferContractId(ContractId transferContractId) {
        builder.setTransferContractID(transferContractId.toProtobuf());
        return this;
    }

    @Override
    void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setContractDeleteInstance(builder);
    }
}
