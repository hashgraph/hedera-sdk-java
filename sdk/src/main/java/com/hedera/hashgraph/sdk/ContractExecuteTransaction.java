package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.ContractCallTransactionBody;
import com.hedera.hashgraph.sdk.proto.SmartContractServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;

/**
 * Call a function of the given smart contract instance, giving it parameters as its inputs.
 * <p>
 * It can use the given amount of gas, and any unspent gas will be refunded to the paying account.
 * <p>
 * If this function stores information, it is charged gas to store it.
 * There is a fee in hbars to maintain that storage until the expiration time, and that fee is
 * added as part of the transaction fee.
 */
public final class ContractExecuteTransaction extends Transaction<ContractExecuteTransaction> {
    private final ContractCallTransactionBody.Builder builder;

    public ContractExecuteTransaction() {
        builder = ContractCallTransactionBody.newBuilder();
    }

    ContractExecuteTransaction(TransactionBody body) {
        super(body);

        builder = body.getContractCall().toBuilder();
    }

    @Nullable
    public ContractId getContractId() {
        return builder.hasContractID() ? ContractId.fromProtobuf(builder.getContractID()) : null;
    }

    /**
     * Sets the contract instance to call.
     *
     * @param contractId The ContractId to be set
     * @return {@code this}
     */
    public ContractExecuteTransaction setContractId(ContractId contractId) {
        requireNotFrozen();
        builder.setContractID(contractId.toProtobuf());
        return this;
    }

    public long getGas() {
        return builder.getGas();
    }

    /**
     * Sets the maximum amount of gas to use for the call.
     *
     * @param gas The long to be set as gas
     * @return {@code this}
     */
    public ContractExecuteTransaction setGas(long gas) {
        requireNotFrozen();
        builder.setGas(gas);
        return this;
    }

    public Hbar getPayableAmount() {
        return Hbar.fromTinybars(builder.getAmount());
    }

    /**
     * Sets the number of hbars sent with this function call.
     *
     * @param amount The Hbar to be set
     * @return {@code this}
     */
    public ContractExecuteTransaction setPayableAmount(Hbar amount) {
        requireNotFrozen();
        builder.setAmount(amount.toTinybars());
        return this;
    }

    public ByteString getFunctionParameters() {
        return builder.getFunctionParameters();
    }

    /**
     * Sets the function parameters as their raw bytes.
     * <p>
     * Use this instead of {@link #setFunction(String, ContractFunctionParameters)} if you have already
     * pre-encoded a solidity function call.
     *
     * @param functionParameters The function parameters to be set
     * @return {@code this}
     */
    public ContractExecuteTransaction setFunctionParameters(ByteString functionParameters) {
        requireNotFrozen();
        builder.setFunctionParameters(functionParameters);
        return this;
    }

    /**
     * Sets the function name to call.
     * <p>
     * The function will be called with no parameters. Use {@link #setFunction(String, ContractFunctionParameters)}
     * to call a function with parameters.
     *
     * @param name The String to be set as the function name
     * @return {@code this}
     */
    public ContractExecuteTransaction setFunction(String name) {
        return setFunction(name, new ContractFunctionParameters());
    }

    /**
     * Sets the function to call, and the parameters to pass to the function.
     *
     * @param name   The String to be set as the function name
     * @param params The function parameters to be set
     * @return {@code this}
     */
    public ContractExecuteTransaction setFunction(String name, ContractFunctionParameters params) {
        return setFunctionParameters(params.toBytes(name));
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return SmartContractServiceGrpc.getContractCallMethodMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setContractCall(builder);
        return true;
    }
}
