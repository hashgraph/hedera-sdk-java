package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ContractCallTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;

public final class ContractExecuteTransaction extends TransactionBuilder<ContractExecuteTransaction> {
    private final ContractCallTransactionBody.Builder builder;

    public ContractExecuteTransaction() {
        builder = ContractCallTransactionBody.newBuilder();
    }

    @Override
    protected void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setContractCall(builder);
    }
}
