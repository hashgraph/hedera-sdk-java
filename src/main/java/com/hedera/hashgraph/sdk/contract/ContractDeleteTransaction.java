package com.hedera.hashgraph.sdk.contract;

import com.hedera.hashgraph.proto.ContractDeleteTransactionBody;
import com.hedera.hashgraph.proto.SmartContractServiceGrpc;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.TransactionBuilder;

import io.grpc.MethodDescriptor;

/** Delete a smart contract instance. */
// `ContractDeleteTransaction`
public final class ContractDeleteTransaction extends TransactionBuilder<ContractDeleteTransaction> {
    private final ContractDeleteTransactionBody.Builder builder = bodyBuilder.getContractDeleteInstanceBuilder();

    public ContractDeleteTransaction() {
        super();
    }

    public ContractDeleteTransaction setContractId(ContractId contractId) {
        builder.setContractID(contractId.toProto());
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return SmartContractServiceGrpc.getDeleteContractMethod();
    }

    @Override
    protected void doValidate() {
        require(builder.hasContractID(), ".setContractId() required");
    }
}
