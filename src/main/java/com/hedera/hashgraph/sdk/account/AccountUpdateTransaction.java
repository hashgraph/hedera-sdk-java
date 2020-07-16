package com.hedera.hashgraph.sdk.account;

import com.google.protobuf.BoolValue;
import com.google.protobuf.UInt64Value;
import com.hedera.hashgraph.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.proto.CryptoUpdateTransactionBody;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.DurationHelper;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.SingleTransactionBuilder;
import com.hedera.hashgraph.sdk.crypto.PublicKey;

import java.time.Duration;
import java.time.Instant;

import io.grpc.MethodDescriptor;

// `CryptoUpdateTransaction`
public final class AccountUpdateTransaction extends SingleTransactionBuilder<AccountUpdateTransaction> {
    private final CryptoUpdateTransactionBody.Builder builder = bodyBuilder.getCryptoUpdateAccountBuilder();

    public AccountUpdateTransaction() { super(); }

    public AccountUpdateTransaction setAccountId(AccountId accountId) {
        builder.setAccountIDToUpdate(accountId.toProto());
        return this;
    }

    public AccountUpdateTransaction setKey(PublicKey key) {
        builder.setKey(key.toKeyProto());
        return this;
    }

    public AccountUpdateTransaction setProxyAccountId(AccountId accountId) {
        builder.setProxyAccountID(accountId.toProto());
        return this;
    }

    /**
     * Set the threshold for generating records when sending currency, in tinybar.
     */
    public AccountUpdateTransaction setSendRecordThreshold(long sendRecordThreshold) {
        builder.setSendRecordThresholdWrapper(UInt64Value.of(sendRecordThreshold));
        return this;
    }

    public AccountUpdateTransaction setSendRecordThreshold(Hbar sendRecordThreshold) {
        builder.setSendRecordThresholdWrapper(
            UInt64Value.of(sendRecordThreshold.asTinybar()));
        return this;
    }

    /**
     * Set the threshold for generating records when receiving currency, in tinybar.
     */
    public AccountUpdateTransaction setReceiveRecordThreshold(long receiveRecordThreshold) {
        builder.setReceiveRecordThresholdWrapper(UInt64Value.of(receiveRecordThreshold));
        return this;
    }

    public AccountUpdateTransaction setReceiveRecordThreshold(Hbar receiveRecordThreshold) {
        builder.setReceiveRecordThresholdWrapper(
            UInt64Value.of(receiveRecordThreshold.asTinybar()));
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

    public AccountUpdateTransaction setReceiverSignatureRequired(boolean receiverSignatureRequired) {
        builder.setReceiverSigRequiredWrapper(BoolValue.of(receiverSignatureRequired));
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
