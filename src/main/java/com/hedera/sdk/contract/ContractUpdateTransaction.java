package com.hedera.sdk.contract;

import com.hedera.sdk.*;
import com.hedera.sdk.crypto.Key;
import com.hedera.sdk.proto.ContractUpdateTransactionBody;
import com.hedera.sdk.proto.SmartContractServiceGrpc;
import com.hedera.sdk.proto.Transaction;
import com.hedera.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import java.time.Duration;
import java.time.Instant;

public class ContractUpdateTransaction extends TransactionBuilder<ContractUpdateTransaction> {
    private final ContractUpdateTransactionBody.Builder builder;

    public ContractUpdateTransaction() {
        builder = inner.getBodyBuilder().getContractUpdateInstanceBuilder();
    }

    public ContractUpdateTransaction setContract(ContractId contract) {
        builder.setContractID(contract.toProto());
        return this;
    }

    public ContractUpdateTransaction setExpirationTime(Instant expiration) {
        builder.setExpirationTime(TimestampHelper.timestampFrom(expiration));
        return this;
    }

    // fixme: update to the new Key interface
    public ContractUpdateTransaction setAdminKey(Key key) {
        builder.setAdminKey(key.toKeyProto());
        return this;
    }

    public ContractUpdateTransaction SetProxyAccount(AccountId account) {
        builder.setProxyAccountID(account.toProto());
        return this;
    }

    public ContractUpdateTransaction setAutoRenewPeriod(Duration duration) {
        builder.setAutoRenewPeriod(
                com.hedera.sdk.proto.Duration.newBuilder()
                        .setSeconds(duration.getSeconds())
                        .setNanos(duration.getNano()));
        return this;
    }

    public ContractUpdateTransaction setFile(FileId file) {
        builder.setFileID(file.toProto());
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return SmartContractServiceGrpc.getUpdateContractMethod();
    }
}
