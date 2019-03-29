package com.hedera.sdk;

import com.google.protobuf.ByteString;
import com.hedera.sdk.crypto.Key;
import com.hedera.sdk.proto.*;
import com.hedera.sdk.proto.Transaction;
import io.grpc.MethodDescriptor;

public class ContractCreateTransaction extends TransactionBuilder<ContractCreateTransaction> {
    private final ContractCreateTransactionBody.Builder builder;

    public ContractCreateTransaction() {
        builder = inner.getBodyBuilder().getContractCreateInstanceBuilder();
    }

    @Override
    MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return SmartContractServiceGrpc.getCreateContractMethod();
    }

    // more descriptive name than `setFileId`
    ContractCreateTransaction setBytecodeFile(FileId fileId) {
        builder.setFileID(fileId.toProto());
        return this;
    }

    ContractCreateTransaction setAdminKey(Key adminKey) {
        builder.setAdminKey(adminKey.toKeyProto());
        return this;
    }

    ContractCreateTransaction setGas(long gas) {
        builder.setGas(gas);
        return this;
    }

    ContractCreateTransaction setIntialBalance(long intialBalance) {
        builder.setInitialBalance(intialBalance);
        return this;
    }

    ContractCreateTransaction setProxyAccount(AccountId proxyAccountId) {
        builder.setProxyAccountID(proxyAccountId.toProto());
        return this;
    }

    ContractCreateTransaction setProxyFraction(int proxyFraction) {
        builder.setProxyFraction(proxyFraction);
        return this;
    }

    ContractCreateTransaction setAutoRenewPeriod(java.time.Duration duration) {
        builder.setAutoRenewPeriod(DurationHelper.durationFrom(duration));
        return this;
    }

    ContractCreateTransaction setConstructorParams(byte[] constructorParams) {
        builder.setConstructorParameters(ByteString.copyFrom(constructorParams));
        return this;
    }

    ContractCreateTransaction setShard(long shardId) {
        builder.setShardID(ShardID.newBuilder().setShardNum(shardId));
        return this;
    }

    ContractCreateTransaction setRealm(long realmId) {
        builder.setRealmID(RealmID.newBuilder().setRealmNum(realmId));
        return this;
    }

    ContractCreateTransaction setNewRealmAdminKey(Key newRealmAdminKey) {
        builder.setNewRealmAdminKey(newRealmAdminKey.toKeyProto());
        return this;
    }
}
