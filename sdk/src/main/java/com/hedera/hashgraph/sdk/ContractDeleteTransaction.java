package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ContractDeleteTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;

public final class ContractDeleteTransaction extends TransactionBuilder<ContractDeleteTransaction> {
    private final ContractDeleteTransactionBody.Builder builder;

    public ContractDeleteTransaction() {
        builder = ContractDeleteTransactionBody.newBuilder();
    }

    public ContractDeleteTransaction setContractId(ContractId contractId) {
        builder.setContractID(contractId.toProtobuf());
        return this;
    }

    public ContractDeleteTransaction setTransferAccountId(AccountId transferAccountId) {
        builder.setTransferAccountID(transferAccountId.toProtobuf());
        return this;
    }

    public ContractDeleteTransaction setTransferContractId(ContractId transferContractId) {
        builder.setTransferContractID(transferContractId.toProtobuf());
        return this;
    }

    @Override
    void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setContractDeleteInstance(builder);
    }
}
