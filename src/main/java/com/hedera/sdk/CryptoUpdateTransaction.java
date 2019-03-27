package com.hedera.sdk;

import com.hedera.sdk.crypto.Key;
import com.hedera.sdk.proto.CryptoServiceGrpc;
import com.hedera.sdk.proto.CryptoUpdateTransactionBody;
import com.hedera.sdk.proto.Transaction;
import com.hedera.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import java.time.Duration;
import java.time.Instant;

public final class CryptoUpdateTransaction extends TransactionBuilder<CryptoUpdateTransaction> {
    private final CryptoUpdateTransactionBody.Builder builder;

    public CryptoUpdateTransaction() {
        builder = inner.getBodyBuilder().getCryptoUpdateAccountBuilder();
    }

    public CryptoUpdateTransaction setAccountforUpdate(AccountId accountId) {
        builder.setAccountIDToUpdate(accountId.inner);
        return this;
    }

    public CryptoUpdateTransaction setKey(Key key) {
        builder.setKey(key.toKeyProto());
        return this;
    }

    public CryptoUpdateTransaction setProxyAccount(AccountId accountId) {
        builder.setProxyAccountID(accountId.inner);
        return this;
    }

    public CryptoUpdateTransaction setProxyFraction(int proxyFraction) {
        builder.setProxyFraction(proxyFraction);
        return this;
    }

    public CryptoUpdateTransaction setSendRecordThreshold(long sendRecordThreshold) {
        builder.setSendRecordThreshold(sendRecordThreshold);
        return this;
    }

    public CryptoUpdateTransaction setReceiveRecordThreshold(long receiveRecordThreshold) {
        builder.setReceiveRecordThreshold(receiveRecordThreshold);
        return this;
    }

    public CryptoUpdateTransaction setAutoRenewPeriod(Duration autoRenewPeriod) {
        builder.setAutoRenewPeriod(DurationHelper.durationFrom(autoRenewPeriod));
        return this;
    }

    public CryptoUpdateTransaction setExpirationTime(Instant expirationTime) {
        builder.setExpirationTime(TimestampHelper.timestampFrom(expirationTime));
        return this;
    }

    @Override
    MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return CryptoServiceGrpc.getUpdateAccountMethod();
    }
}
