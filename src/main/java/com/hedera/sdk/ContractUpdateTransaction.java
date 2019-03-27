package com.hedera.sdk;

import com.hedera.sdk.crypto.Key;
import com.hedera.sdk.proto.ContractUpdateTransactionBody;
import java.time.Duration;
import java.time.Instant;

public class ContractUpdateTransaction extends TransactionBuilder<ContractUpdateTransaction> {
    private final ContractUpdateTransactionBody.Builder builder;

    public ContractUpdateTransaction() {
        builder = inner.getBodyBuilder().getContractUpdateInstanceBuilder();
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
        builder.setProxyAccountID(account.inner);
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
        builder.setFileID(file.inner);
        return this;
    }
}
