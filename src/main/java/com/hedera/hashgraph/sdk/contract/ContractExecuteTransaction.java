package com.hedera.hashgraph.sdk.contract;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.proto.ContractCallTransactionBody;
import com.hedera.hashgraph.proto.SmartContractServiceGrpc;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.CallParams;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.TransactionBuilder;

import javax.annotation.Nullable;

import io.grpc.MethodDescriptor;

/** Call a function in the contract, updating its internal state in the hashgraph. */
// `ContractCallTransaction`
public final class ContractExecuteTransaction extends TransactionBuilder<ContractExecuteTransaction> {
    private final ContractCallTransactionBody.Builder builder = bodyBuilder.getContractCallBuilder();

    /**
     * @deprecated use the no-arg constructor and pass the client to {@link #build(Client)} instead.
     */
    @Deprecated
    public ContractExecuteTransaction(@Nullable Client client) {
        super(client);
    }

    public ContractExecuteTransaction() { super(); }

    public ContractExecuteTransaction setContractId(ContractId contractId) {
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

    /**
     * @deprecated renamed to {@link #setFunctionParams(byte[])}.
     */
    @Deprecated
    public ContractExecuteTransaction setFunctionParameters(byte[] functionParameters) {
        builder.setFunctionParameters(ByteString.copyFrom(functionParameters));
        return this;
    }

    /**
     * Set the function parameters (selector plus arguments) as a raw byte array.
     *
     * @param parameters
     * @return {@code this} for fluent API usage.
     */
    public ContractExecuteTransaction setFunctionParams(byte[] parameters) {
        builder.setFunctionParameters(ByteString.copyFrom(parameters));
        return this;
    }

    /**
     * @deprecated Associated class is being removed; use {@link #setFunction(String, ContractFunctionParams)}
     * and see {@link ContractFunctionParams} for new API.
     */
    public ContractExecuteTransaction setFunctionParameters(CallParams<CallParams.Function> parameters) {
        builder.setFunctionParameters(parameters.toProto());
        return this;
    }

    /**
     * Set the function to execute with an <b>empty parameter list</b>.
     *
     * @param funcName the name of the function to call.
     * @return {@code this} for fluent API usage.
     */
    public ContractExecuteTransaction setFunction(String funcName) {
        return setFunction(funcName, new ContractFunctionParams());
    }

    /**
     * Set the function to execute and the parameters to pass.
     *
     * @param funcName the name of the function to call; the function selector is calculated from
     *                 this and the types of the params passed in {@code params}.
     * @param params the params to pass to the function being executed.
     * @return {@code this} for fluent API usage.
     */
    public ContractExecuteTransaction setFunction(String funcName, ContractFunctionParams params) {
        builder.setFunctionParameters(params.toBytes(funcName));
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return SmartContractServiceGrpc.getContractCallMethodMethod();
    }

    @Override
    protected void doValidate() {
        require(builder.hasContractID(), ".setContractId() required");
    }
}
