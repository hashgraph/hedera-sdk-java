package com.hedera.sdk.contract;

import com.google.protobuf.ByteString;
import com.hedera.sdk.Client;
import com.hedera.sdk.TransactionBuilder;
import com.hedera.sdk.proto.ContractCallTransactionBody;
import com.hedera.sdk.proto.SmartContractServiceGrpc;
import com.hedera.sdk.proto.Transaction;
import com.hedera.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

/** Call a function in the contract, updating its internal state in the hashgraph. */
// `ContractCallTransaction`
public final class ContractExecuteTransaction extends TransactionBuilder<ContractExecuteTransaction> {
    private final ContractCallTransactionBody.Builder builder = inner.getBodyBuilder()
        .getContractCallBuilder();

    public ContractExecuteTransaction(Client client) {
        super(client);
    }

    ContractExecuteTransaction() {
        super(null);
    }

    public ContractExecuteTransaction setContract(ContractId contractId) {
        builder.setContractID(contractId.toProto());
        return this;
    }

    public ContractExecuteTransaction setGas(long gas) {
        builder.setGas(gas);
        return this;
    }

    public ContractExecuteTransaction setAmount(long amount) {
        builder.setAmount(amount);
        return this;
    }

    public ContractExecuteTransaction setFunctionParameters(byte[] functionParameters) {
        builder.setFunctionParameters(ByteString.copyFrom(functionParameters));
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return SmartContractServiceGrpc.getContractCallMethodMethod();
    }

    @Override
    protected void doValidate() {
        require(builder.hasContractID(), ".setContract() required");
    }
}
