package com.hedera.sdk.contract;

import com.google.protobuf.ByteString;
import com.hedera.sdk.*;
import com.hedera.sdk.account.AccountId;
import com.hedera.sdk.crypto.Key;
import com.hedera.sdk.file.FileId;
import com.hedera.sdk.proto.*;
import com.hedera.sdk.proto.Transaction;
import io.grpc.MethodDescriptor;

public class ContractCreateTransaction extends TransactionBuilder<ContractCreateTransaction> {
    private final ContractCreateTransactionBody.Builder builder = inner.getBodyBuilder()
        .getContractCreateInstanceBuilder();

    public ContractCreateTransaction(Client client) {
        super(client);
    }

    ContractCreateTransaction() {
        super(null);
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return SmartContractServiceGrpc.getCreateContractMethod();
    }

    // more descriptive name than `setFileId`
    public ContractCreateTransaction setBytecodeFile(FileId fileId) {
        builder.setFileID(fileId.toProto());
        return this;
    }

    public ContractCreateTransaction setAdminKey(Key adminKey) {
        builder.setAdminKey(adminKey.toKeyProto());
        return this;
    }

    public ContractCreateTransaction setGas(long gas) {
        builder.setGas(gas);
        return this;
    }

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

    public ContractCreateTransaction setConstructorParams(CallParams<CallParams.Constructor> constructorParams) {
        builder.setConstructorParameters(constructorParams.toProto());
        return this;
    }

    public ContractCreateTransaction setShard(long shardId) {
        builder.setShardID(
            ShardID.newBuilder()
                .setShardNum(shardId)
        );
        return this;
    }

    public ContractCreateTransaction setRealm(long realmId) {
        builder.setRealmID(
            RealmID.newBuilder()
                .setRealmNum(realmId)
        );
        return this;
    }

    public ContractCreateTransaction setNewRealmAdminKey(Key newRealmAdminKey) {
        builder.setNewRealmAdminKey(newRealmAdminKey.toKeyProto());
        return this;
    }

    @Override
    protected void doValidate() {
        require(builder.hasFileID(), ".setBytecodeFile() required");
    }
}
