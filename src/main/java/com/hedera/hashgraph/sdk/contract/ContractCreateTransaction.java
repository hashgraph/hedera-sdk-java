package com.hedera.hashgraph.sdk.contract;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.proto.ContractCreateTransactionBody;
import com.hedera.hashgraph.proto.SmartContractServiceGrpc;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.DurationHelper;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.HederaConstants;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hedera.hashgraph.sdk.TransactionRecord;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.PublicKey;
import com.hedera.hashgraph.sdk.file.FileId;

import java.time.Duration;

import io.grpc.MethodDescriptor;

/**
 * Transaction to create a contract on the Hedera network.
 *
 * After execution, call {@link com.hedera.hashgraph.sdk.Transaction#getRecord(Client)}
 * and {@link TransactionRecord#getContractCreateResult()} to get the
 * result of the constructor call.
 */
public class ContractCreateTransaction extends TransactionBuilder<ContractCreateTransaction> {
    private final ContractCreateTransactionBody.Builder builder = bodyBuilder.getContractCreateInstanceBuilder();

    {
        // Required fixed autorenew duration.
        setAutoRenewPeriod(HederaConstants.DEFAULT_AUTORENEW_DURATION);
    }

    public ContractCreateTransaction() { super(); }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return SmartContractServiceGrpc.getCreateContractMethod();
    }

    // more descriptive name than `setFileId`
    public ContractCreateTransaction setBytecodeFileId(FileId fileId) {
        builder.setFileID(fileId.toProto());
        return this;
    }

    public ContractCreateTransaction setAdminKey(PublicKey adminKey) {
        builder.setAdminKey(adminKey.toKeyProto());
        return this;
    }

    public ContractCreateTransaction setGas(long gas) {
        builder.setGas(gas);
        return this;
    }

    /**
     * Set the initial balance that will be transferred from the operator's account to the new
     * contract's internal crypto account before the constructor is called.
     *
     * @param intialBalance the initial balance of the new contract's internal crypto account.
     * @return {@code this} for fluent API usage.
     */
    public ContractCreateTransaction setInitialBalance(Hbar intialBalance) {
        builder.setInitialBalance(intialBalance.asTinybar());
        return this;
    }

    /**
     * Set the initial balance that will be transferred from the operator's account to the new
     * contract's internal crypto account before the constructor is called.
     *
     * @param intialBalance the initial balance of the new contract's internal crypto account,
     *                      in tinybar.
     * @return {@code this} for fluent API usage.
     */
    public ContractCreateTransaction setInitialBalance(long intialBalance) {
        builder.setInitialBalance(intialBalance);
        return this;
    }

    public ContractCreateTransaction setProxyAccountId(AccountId proxyAccountId) {
        builder.setProxyAccountID(proxyAccountId.toProto());
        return this;
    }

    public ContractCreateTransaction setAutoRenewPeriod(java.time.Duration duration) {
        builder.setAutoRenewPeriod(DurationHelper.durationFrom(duration));
        return this;
    }

    public ContractCreateTransaction setConstructorParams(byte[] constructorParams) {
        builder.setConstructorParameters(ByteString.copyFrom(constructorParams));
        return this;
    }

    public ContractCreateTransaction setConstructorParams(ContractFunctionParams constructorParams) {
        builder.setConstructorParameters(constructorParams.toBytes(null));
        return this;
    }

    /**
     * Set a memo for the contract itself, as opposed to for this transaction
     * (via {@link #setTransactionMemo(String)}).
     */
    public ContractCreateTransaction setContractMemo(String memo) {
        builder.setMemo(memo);
        return this;
    }

    @Override
    protected void doValidate() {
        require(builder.hasFileID(), ".setBytecodeFile() required");
    }
}
