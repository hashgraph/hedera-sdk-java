package com.hedera.hashgraph.sdk.contract;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.DurationHelper;
import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.PublicKey;
import com.hedera.hashgraph.sdk.file.FileId;
import com.hederahashgraph.api.proto.java.ContractUpdateTransactionBody;
import com.hederahashgraph.api.proto.java.Transaction;
import com.hederahashgraph.api.proto.java.TransactionResponse;
import com.hederahashgraph.service.proto.java.SmartContractServiceGrpc;

import java.time.Duration;
import java.time.Instant;

import javax.annotation.Nullable;

import io.grpc.MethodDescriptor;

public class ContractUpdateTransaction extends TransactionBuilder<ContractUpdateTransaction> {
    private final ContractUpdateTransactionBody.Builder builder = bodyBuilder.getContractUpdateInstanceBuilder();

    public ContractUpdateTransaction(@Nullable Client client) {
        super(client);
    }

    public ContractUpdateTransaction setContractId(ContractId contract) {
        builder.setContractID(contract.toProto());
        return this;
    }

    public ContractUpdateTransaction setExpirationTime(Instant expiration) {
        builder.setExpirationTime(TimestampHelper.timestampFrom(expiration));
        return this;
    }

    // fixme: update to the new Key interface
    public ContractUpdateTransaction setAdminKey(PublicKey key) {
        builder.setAdminKey(key.toKeyProto());
        return this;
    }

    public ContractUpdateTransaction setProxyAccount(AccountId account) {
        builder.setProxyAccountID(account.toProto());
        return this;
    }

    public ContractUpdateTransaction setAutoRenewPeriod(Duration duration) {
        builder.setAutoRenewPeriod(DurationHelper.durationFrom(duration));
        return this;
    }

    public ContractUpdateTransaction setFileId(FileId file) {
        builder.setFileID(file.toProto());
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return SmartContractServiceGrpc.getUpdateContractMethod();
    }

    @Override
    protected void doValidate() {
        require(builder.hasContractID(), ".setContractId() required");
    }
}
