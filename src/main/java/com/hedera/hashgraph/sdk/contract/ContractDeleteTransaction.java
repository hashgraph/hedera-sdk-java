package com.hedera.hashgraph.sdk.contract;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hedera.hashgraph.proto.ContractDeleteTransactionBody;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.proto.SmartContractServiceGrpc;

import io.grpc.MethodDescriptor;

/** Delete a smart contract instance. */
// `ContractDeleteTransaction`
public final class ContractDeleteTransaction extends TransactionBuilder<ContractDeleteTransaction> {
    private final ContractDeleteTransactionBody.Builder builder = bodyBuilder.getContractDeleteInstanceBuilder();

    /**
     * @deprecated use the no-arg constructor and pass the client to {@link #build(Client)} instead.
     */
    @Deprecated
    public ContractDeleteTransaction(Client client) {
        super(client);
    }

    public ContractDeleteTransaction() {
        super(null);
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
