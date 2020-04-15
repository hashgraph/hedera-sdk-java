package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.ContractCallTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;

public final class ContractExecuteTransaction extends TransactionBuilder<ContractExecuteTransaction> {
    private final ContractCallTransactionBody.Builder builder;

    public ContractExecuteTransaction() {
        builder = ContractCallTransactionBody.newBuilder();
    }

    public ContractExecuteTransaction setContractId(ContractId contractId) {
        builder.setContractID(contractId.toProtobuf());
        return this;
    }

    public ContractExecuteTransaction setGas(long gas) {
        builder.setGas(gas);
        return this;
    }

    public ContractExecuteTransaction setPayableAmount(Hbar amount) {
        builder.setAmount(amount.asTinybar());
        return this;
    }

    public ContractExecuteTransaction setFunctionParameters(ByteString functionParameters) {
        builder.setFunctionParameters(functionParameters);
        return this;
    }

    public ContractExecuteTransaction setFunction(String name) {
        return setFunction(name, new ContractFunctionParameters());
    }

    public ContractExecuteTransaction setFunction(String name, ContractFunctionParameters params) {
        return setFunctionParameters(params.toBytes(name));
    }

    @Override
    void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setContractCall(builder);
    }
}
