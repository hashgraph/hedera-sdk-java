package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ContractCreateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;

public final class ContractCreateTransaction extends TransactionBuilder<ContractCreateTransaction> {
    private final ContractCreateTransactionBody.Builder builder;

    public ContractCreateTransaction() {
        builder = ContractCreateTransactionBody.newBuilder();
    }

    @Override
    protected void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setContractCreateInstance(builder);
    }
}
