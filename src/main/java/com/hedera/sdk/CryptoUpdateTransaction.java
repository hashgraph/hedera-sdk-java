package com.hedera.sdk;

import com.hedera.sdk.crypto.IPublicKey;
import com.hedera.sdk.proto.CryptoUpdateTransactionBody;
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

    public CryptoUpdateTransaction setKey(IPublicKey key) {
        builder.setKey(key.toProtoKey());
        return this;
    }

    public CryptoUpdateTransaction setKey(ContractId contractId) {
        builder.setKey(contractId.toProtoKey());
        return this;
    }

    public CryptoUpdateTransaction setAccountForProxy(AccountId accountId) {
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
}
