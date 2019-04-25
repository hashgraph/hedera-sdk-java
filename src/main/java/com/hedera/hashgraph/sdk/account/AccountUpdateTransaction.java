package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.DurationHelper;
import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hedera.hashgraph.sdk.crypto.Key;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.CryptoUpdateTransactionBody;
import com.hedera.hashgraph.sdk.proto.Transaction;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import java.time.Duration;
import java.time.Instant;

// `CryptoUpdateTransaction`
public final class AccountUpdateTransaction extends TransactionBuilder<AccountUpdateTransaction> {
    private final CryptoUpdateTransactionBody.Builder builder = bodyBuilder.getCryptoUpdateAccountBuilder();

    public AccountUpdateTransaction(Client client) {
        super(client);
    }

    AccountUpdateTransaction() {
        super(null);
    }

    public AccountUpdateTransaction setAccountForUpdate(AccountId accountId) {
        builder.setAccountIDToUpdate(accountId.toProto());
        return this;
    }

    public AccountUpdateTransaction setKey(Key key) {
        builder.setKey(key.toKeyProto());
        return this;
    }

    public AccountUpdateTransaction setProxyAccount(AccountId accountId) {
        builder.setProxyAccountID(accountId.toProto());
        return this;
    }

    public AccountUpdateTransaction setSendRecordThreshold(long sendRecordThreshold) {
        builder.setSendRecordThresholdWrapper(
            com.google.protobuf.UInt64Value.newBuilder()
                .setValue(sendRecordThreshold)
        );
        return this;
    }

    public AccountUpdateTransaction setReceiveRecordThreshold(long receiveRecordThreshold) {
        builder.setReceiveRecordThresholdWrapper(
            com.google.protobuf.UInt64Value.newBuilder()
                .setValue(receiveRecordThreshold)
        );
        return this;
    }

    public AccountUpdateTransaction setAutoRenewPeriod(Duration autoRenewPeriod) {
        builder.setAutoRenewPeriod(DurationHelper.durationFrom(autoRenewPeriod));
        return this;
    }

    public AccountUpdateTransaction setExpirationTime(Instant expirationTime) {
        builder.setExpirationTime(TimestampHelper.timestampFrom(expirationTime));
        return this;
    }

    @Override
    protected void doValidate() {
        require(builder.hasAccountIDToUpdate(), ".setAccountForUpdate() required");
        require(builder.hasKey(), ".setKey() required");
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return CryptoServiceGrpc.getUpdateAccountMethod();
    }
}
