package com.hedera.hashgraph.sdk.contract;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.proto.ContractCreateTransactionBody;
import com.hedera.hashgraph.proto.RealmID;
import com.hedera.hashgraph.proto.ShardID;
import com.hedera.hashgraph.proto.SmartContractServiceGrpc;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.CallParams;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.DurationHelper;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hedera.hashgraph.sdk.TransactionRecord;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.PublicKey;
import com.hedera.hashgraph.sdk.file.FileId;

import java.time.Duration;

import javax.annotation.Nullable;

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
        // Required fixed autorenew duration (roughly 1/4 year)
        setAutoRenewPeriod(Duration.ofMinutes(131_500));
    }

    /**
     * @deprecated use the no-arg constructor and pass the client to {@link #build(Client)} instead.
     */
    @Deprecated
    public ContractCreateTransaction(@Nullable Client client) {
        super(client);
    }

    public ContractCreateTransaction() { super(); }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return SmartContractServiceGrpc.getCreateContractMethod();
    }

    /**
     * @deprecated renamed to {@link #setBytecodeFileId(FileId)}
     */
    @Deprecated
    public ContractCreateTransaction setBytecodeFile(FileId fileId) {
        builder.setFileID(fileId.toProto());
        return this;
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

    /**
     * @deprecated Associated class is being removed; use {@link #setConstructorParams(ContractFunctionParams)}
     * and see {@link ContractFunctionParams} for new API.
     */
    @Deprecated
    public ContractCreateTransaction setConstructorParams(CallParams<CallParams.Constructor> constructorParams) {
        builder.setConstructorParameters(constructorParams.toProto());
        return this;
    }

    public ContractCreateTransaction setConstructorParams(ContractFunctionParams constructorParams) {
        builder.setConstructorParameters(constructorParams.toBytes(null));
        return this;
    }

    /**
     * @deprecated shards and realms are not yet implemented on Hedera so this method won't
     * function as expected. It will be restored when the network functionality is available.
     */
    @Deprecated
    public ContractCreateTransaction setShard(long shardId) {
        builder.setShardID(
            ShardID.newBuilder()
                .setShardNum(shardId));
        return this;
    }

    /**
     * @deprecated shards and realms are not yet implemented on Hedera so this method won't
     * function as expected. It will be restored when the network functionality is available.
     */
    @Deprecated
    public ContractCreateTransaction setRealm(long realmId) {
        builder.setRealmID(
            RealmID.newBuilder()
                .setRealmNum(realmId));
        return this;
    }

    /**
     * @deprecated shards and realms are not yet implemented on Hedera so this method won't
     * function as expected. It will be restored when the network functionality is available.
     */
    @Deprecated
    public ContractCreateTransaction setNewRealmAdminKey(PublicKey newRealmAdminKey) {
        builder.setNewRealmAdminKey(newRealmAdminKey.toKeyProto());
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
