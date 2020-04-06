package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ContractUpdateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;

public final class ContractUpdateTransaction extends TransactionBuilder<ContractUpdateTransaction> {
    private final ContractUpdateTransactionBody.Builder builder;

    public ContractUpdateTransaction() {
        builder = ContractUpdateTransactionBody.newBuilder();
    }

    @Override
    protected void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setContractUpdateInstance(builder);
    }
}
