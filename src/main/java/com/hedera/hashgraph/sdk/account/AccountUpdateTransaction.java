package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.proto.CryptoUpdateTransactionBody;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.DurationHelper;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hedera.hashgraph.sdk.crypto.PublicKey;

import java.time.Duration;
import java.time.Instant;

import javax.annotation.Nullable;

import io.grpc.MethodDescriptor;

// `CryptoUpdateTransaction`
public final class AccountUpdateTransaction extends TransactionBuilder<AccountUpdateTransaction> {
    private final CryptoUpdateTransactionBody.Builder builder = bodyBuilder.getCryptoUpdateAccountBuilder();

    /**
     * @deprecated use the no-arg constructor and pass the client to {@link #build(Client)} instead.
     */
    @Deprecated
    public AccountUpdateTransaction(@Nullable Client client) {
        super(client);
    }

    public AccountUpdateTransaction() { super(); }

    public AccountUpdateTransaction setAccountForUpdate(AccountId accountId) {
        builder.setAccountIDToUpdate(accountId.toProto());
        return this;
    }

    public AccountUpdateTransaction setKey(PublicKey key) {
        builder.setKey(key.toKeyProto());
        return this;
    }

    public AccountUpdateTransaction setProxyAccount(AccountId accountId) {
        builder.setProxyAccountID(accountId.toProto());
        return this;
    }

    /**
     * Set the threshold for generating records when sending currency, in tinybar.
     */
    public AccountUpdateTransaction setSendRecordThreshold(long sendRecordThreshold) {
        builder.setSendRecordThresholdWrapper(
            com.google.protobuf.UInt64Value.newBuilder()
                .setValue(sendRecordThreshold));
        return this;
    }

    public AccountUpdateTransaction setSendRecordThreshold(Hbar sendRecordThreshold) {
        builder.setSendRecordThresholdWrapper(
            com.google.protobuf.UInt64Value.newBuilder()
                .setValue(sendRecordThreshold.asTinybar()));
        return this;
    }

    /**
     * Set the threshold for generating records when receiving currency, in tinybar.
     */
    public AccountUpdateTransaction setReceiveRecordThreshold(long receiveRecordThreshold) {
        builder.setReceiveRecordThresholdWrapper(
            com.google.protobuf.UInt64Value.newBuilder()
                .setValue(receiveRecordThreshold));
        return this;
    }

    public AccountUpdateTransaction setReceiveRecordThreshold(Hbar receiveRecordThreshold) {
        builder.setReceiveRecordThresholdWrapper(
            com.google.protobuf.UInt64Value.newBuilder()
                .setValue(receiveRecordThreshold.asTinybar()));
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
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return CryptoServiceGrpc.getUpdateAccountMethod();
    }
}
