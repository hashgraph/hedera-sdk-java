package com.hedera.hashgraph.sdk.contract;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.proto.ContractCallTransactionBody;
import com.hedera.hashgraph.proto.SmartContractServiceGrpc;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hedera.hashgraph.sdk.TransactionRecord;

import io.grpc.MethodDescriptor;

/**
 * Call a function in the contract, updating its internal state in the hashgraph.
 *
 * After execution, call {@link com.hedera.hashgraph.sdk.Transaction#getRecord(Client)}
 * and {@link TransactionRecord#getContractExecuteResult()} to get the
 * result of the function call.
 */
// `ContractCallTransaction`
public final class ContractExecuteTransaction extends TransactionBuilder<ContractExecuteTransaction> {
    private final ContractCallTransactionBody.Builder builder = bodyBuilder.getContractCallBuilder();

    public ContractExecuteTransaction() { super(); }

    public ContractExecuteTransaction setContractId(ContractId contractId) {
        builder.setContractID(contractId.toProto());
        return this;
    }

    public ContractExecuteTransaction setGas(long gas) {
        builder.setGas(gas);
        return this;
    }

    /**
     * Set the amount of hbar that will be paid from the operator account to the function
     * being invoked.
     *
     * The function being invoked must itself be marked {@code payable}.
     *
     * @param amount the amount being paid to the function.
     * @return {@code this} for fluent API usage.
     */
    public ContractExecuteTransaction setPayableAmount(Hbar amount) {
        builder.setAmount(amount.asTinybar());
        return this;
    }

    /**
     * Set the amount of hbar that will be paid, in tinybar, from the operator account to the
     * function being invoked.
     *
     * The function being invoked must itself be marked {@code payable}.
     *
     * @param amount the amount being paid to the function, in tinybar.
     * @return {@code this} for fluent API usage.
     */
    public ContractExecuteTransaction setPayableAmount(long amount) {
        builder.setAmount(amount);
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
