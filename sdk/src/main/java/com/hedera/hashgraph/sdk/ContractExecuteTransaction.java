package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.ContractCallTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;

/**
 * Call a function of the given smart contract instance, giving it parameters as its inputs.
 *
 * It can use the given amount of gas, and any unspent gas will be refunded to the paying account.
 *
 * If this function stores information, it is charged gas to store it.
 * There is a fee in hbars to maintain that storage until the expiration time, and that fee is
 * added as part of the transaction fee.
 */
public final class ContractExecuteTransaction extends TransactionBuilder<ContractExecuteTransaction> {
    private final ContractCallTransactionBody.Builder builder;

    public ContractExecuteTransaction() {
        builder = ContractCallTransactionBody.newBuilder();
    }

    /**
     * Sets the contract instance to call.
     *
     * @return {@code this}
     */
    public ContractExecuteTransaction setContractId(ContractId contractId) {
        builder.setContractID(contractId.toProtobuf());
        return this;
    }

    /**
     * Sets the maximum amount of gas to use for the call.
     *
     * @return {@code this}
     */
    public ContractExecuteTransaction setGas(long gas) {
        builder.setGas(gas);
        return this;
    }

    /**
     * Sets the number of hbars sent with this function call.
     *
     * @return {@code this}
     */
    public ContractExecuteTransaction setPayableAmount(Hbar amount) {
        builder.setAmount(amount.toTinybars());
        return this;
    }

    /**
     * Sets the function parameters as their raw bytes.
     * <p>
     * Use this instead of {@link #setFunction(String, ContractFunctionParameters)} if you have already
     * pre-encoded a solidity function call.
     *
     * @return {@code this}
     */
    public ContractExecuteTransaction setFunctionParameters(ByteString functionParameters) {
        builder.setFunctionParameters(functionParameters);
        return this;
    }

    /**
     * Sets the function name to call.
     * <p>
     * The function will be called with no parameters. Use {@link #setFunction(String, ContractFunctionParameters)}
     * to call a function with parameters.
     *
     * @return {@code this}
     */
    public ContractExecuteTransaction setFunction(String name) {
        return setFunction(name, new ContractFunctionParameters());
    }

    /**
     * Sets the function to call, and the parameters to pass to the function.
     *
     * @return {@code this}
     */
    public ContractExecuteTransaction setFunction(String name, ContractFunctionParameters params) {
        return setFunctionParameters(params.toBytes(name));
    }

    @Override
    void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setContractCall(builder);
    }
}
