package com.hedera.hashgraph.sdk;

import com.google.protobuf.BoolValue;
import com.google.protobuf.UInt64Value;
import com.hedera.hashgraph.sdk.proto.CryptoUpdateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

public final class AccountUpdateTransaction extends TransactionBuilder<AccountUpdateTransaction> {
    private final CryptoUpdateTransactionBody.Builder builder;

    public AccountUpdateTransaction() {
        builder = CryptoUpdateTransactionBody.newBuilder();
    }

    public AccountUpdateTransaction setAccountId(AccountId accountId) {
        builder.setAccountIDToUpdate(accountId.toProtobuf());
        return this;
    }

    public AccountUpdateTransaction setKey(Key key) {
        builder.setKey(key.toKeyProtobuf());
        return this;
    }

    public AccountUpdateTransaction setProxyAccountId(AccountId proxyAccountId) {
        builder.setProxyAccountID(proxyAccountId.toProtobuf());
        return this;
    }

    public AccountUpdateTransaction setSendRecordThreshold(Hbar sendRecordThreshold) {
        builder.setSendRecordThresholdWrapper(UInt64Value.of(sendRecordThreshold.asTinybar()));
        return this;
    }

    public AccountUpdateTransaction setReceiveRecordThreshold(Hbar receiveRecordThreshold) {
        builder.setReceiveRecordThresholdWrapper(UInt64Value.of(receiveRecordThreshold.asTinybar()));
        return this;
    }

    public AccountUpdateTransaction setExpirationTime(Instant expirationTime) {
        builder.setExpirationTime(InstantConverter.toProtobuf(expirationTime));
        return this;
    }

    public AccountUpdateTransaction setAutoRenewPeriod(Duration autoRenewPeriod) {
        builder.setAutoRenewPeriod(DurationConverter.toProtobuf(autoRenewPeriod));
        return this;
    }

    public AccountUpdateTransaction setReceiverSignatureRequired(boolean receiverSignatureRequired) {
        builder.setReceiverSigRequiredWrapper(BoolValue.of(receiverSignatureRequired));
        return this;
    }

    @Override
    protected void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setCryptoUpdateAccount(builder);
    }
}
