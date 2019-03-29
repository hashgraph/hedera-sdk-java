package com.hedera.sdk.contract;

import com.google.protobuf.ByteString;
import com.hedera.sdk.ContractId;
import com.hedera.sdk.TransactionBuilder;
import com.hedera.sdk.proto.ContractCallTransactionBody;
import com.hedera.sdk.proto.SmartContractServiceGrpc;
import com.hedera.sdk.proto.Transaction;
import com.hedera.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

// `ContractCallMethodTransaction`
public final class ContractCallMethodTransaction
        extends TransactionBuilder<ContractCallMethodTransaction> {
    private final ContractCallTransactionBody.Builder builder;

    public ContractCallMethodTransaction() {
        builder = inner.getBodyBuilder().getContractCallBuilder();
    }

    public ContractCallMethodTransaction setContract(ContractId contractId) {
        builder.setContractID(contractId.toProto());
        return this;
    }

    public ContractCallMethodTransaction setGas(long gas) {
        builder.setGas(gas);
        return this;
    }

    public ContractCallMethodTransaction setAmount(long amount) {
        builder.setAmount(amount);
        return this;
    }

    public ContractCallMethodTransaction setFunctionParameters(byte[] functionParameters) {
        builder.setFunctionParameters(ByteString.copyFrom(functionParameters));
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return SmartContractServiceGrpc.getContractCallMethodMethod();
    }
}
