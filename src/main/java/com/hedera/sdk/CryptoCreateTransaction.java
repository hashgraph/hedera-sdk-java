package com.hedera.sdk;

import com.hedera.sdk.crypto.Key;
import com.hedera.sdk.proto.*;
import java.time.Duration;
import javax.annotation.Nonnull;

public final class CryptoCreateTransaction extends TransactionBuilder<CryptoCreateTransaction> {

    private final CryptoCreateTransactionBody.Builder builder;

    public CryptoCreateTransaction() {
        // Recommendation from Hedera
        setAutoRenewPeriod(Duration.ofDays(30));

        builder = inner.getBodyBuilder().getCryptoCreateAccountBuilder();

        // Default to maximum values for record thresholds. Without this records would be
        // auto-created
        // whenever a send or receive transaction takes place for this new account. This should
        // be an explicit ask.
        builder.setSendRecordThreshold(Long.MAX_VALUE).setReceiveRecordThreshold(Long.MAX_VALUE);
    }

    @Override
    public CryptoCreateTransaction setTransactionId(@Nonnull TransactionId transactionId) {
        // Setting the transaction ID defaults the shard and realm IDs
        // If you truly want to create a _new_ realm, then you need to null the realm after setting
        // this

        if (!builder.hasShardID()) {
            setShardId(transactionId.inner.getAccountID().getShardNum());
        }

        if (!builder.hasRealmID()) {
            setRealmId(transactionId.inner.getAccountID().getRealmNum());
        }

        return super.setTransactionId(transactionId);
    }

    public CryptoCreateTransaction setKey(Key publicKey) {
        builder.setKey(publicKey.toProtoKey());
        return this;
    }

    public CryptoCreateTransaction setInitialBalance(long initialBalance) {
        builder.setInitialBalance(initialBalance);
        return this;
    }

    public CryptoCreateTransaction setproxyAccountId(AccountId accountId) {
        builder.setProxyAccountID(accountId.inner);
        return this;
    }

    public CryptoCreateTransaction setProxyFraction(int proxyFraction) {
        builder.setProxyFraction(proxyFraction);
        return this;
    }

    public CryptoCreateTransaction setMaxReceiveProxyFraction(int maxReceiveProxyFraction) {
        builder.setMaxReceiveProxyFraction(maxReceiveProxyFraction);
        return this;
    }

    public CryptoCreateTransaction setSendRecordThreshold(long sendRecordThreshold) {
        builder.setSendRecordThreshold(sendRecordThreshold);
        return this;
    }

    public CryptoCreateTransaction setReceiveRecordThreshold(long receiveRecordThreshold) {
        builder.setReceiveRecordThreshold(receiveRecordThreshold);
        return this;
    }

    public CryptoCreateTransaction setReceiverSignatureRequired(boolean receiverSignatureRequired) {
        builder.setReceiverSigRequired(receiverSignatureRequired);
        return this;
    }

    public CryptoCreateTransaction setAutoRenewPeriod(@Nonnull Duration autoRenewPeriod) {
        builder.setAutoRenewPeriod(
                com.hedera.sdk.proto.Duration.newBuilder()
                        .setSeconds(autoRenewPeriod.getSeconds())
                        .setNanos(autoRenewPeriod.getNano()));

        return this;
    }

    public CryptoCreateTransaction setShardId(long shardId) {
        builder.setShardID(ShardID.newBuilder().setShardNum(shardId));

        return this;
    }

    public CryptoCreateTransaction setRealmId(long realmId) {
        builder.setRealmID(RealmID.newBuilder().setRealmNum(realmId));

        return this;
    }

    public CryptoCreateTransaction setNewRealmAdminKey(Key publicKey) {
        builder.setNewRealmAdminKey(publicKey.toProtoKey());
        return this;
    }
}
