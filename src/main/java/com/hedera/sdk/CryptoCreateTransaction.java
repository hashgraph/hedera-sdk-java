package com.hedera.sdk;

import com.google.protobuf.ByteString;
import com.hedera.sdk.crypto.ed25519.Ed25519PublicKey;
import com.hedera.sdk.proto.Key;
import com.hedera.sdk.proto.RealmID;
import com.hedera.sdk.proto.ShardID;
import java.time.Duration;
import javax.annotation.Nonnull;

public final class CryptoCreateTransaction extends TransactionBuilder<CryptoCreateTransaction> {
    // todo: setKey

    public CryptoCreateTransaction() {
        // Recommendation from Hedera
        setAutoRenewPeriod(Duration.ofDays(30));

        // Default to maximum values for record thresholds. Without this records would be
        // auto-created
        // whenever a send or receive transaction takes place for this new account. This should
        // be an explicit ask.
        inner.getBodyBuilder()
                .getCryptoCreateAccountBuilder()
                .setSendRecordThreshold(Long.MAX_VALUE)
                .setReceiveRecordThreshold(Long.MAX_VALUE);
    }

    @Override
    public CryptoCreateTransaction setTransactionId(@Nonnull TransactionId transactionId) {
        // Setting the transaction ID defaults the shard and realm IDs
        // If you truly want to create a _new_ realm, then you need to null the realm after setting
        // this

        if (!inner.getBodyBuilder().getCryptoCreateAccountBuilder().hasShardID()) {
            setShardId(transactionId.inner.getAccountID().getShardNum());
        }

        if (!inner.getBodyBuilder().getCryptoCreateAccountBuilder().hasRealmID()) {
            setRealmId(transactionId.inner.getAccountID().getRealmNum());
        }

        return super.setTransactionId(transactionId);
    }

    // TODO: Accept a PublicKey interface type that can serialize itself to protobuf so we can
    // accept N key types
    public CryptoCreateTransaction setKey(Ed25519PublicKey publicKey) {
        // FIXME: Is `ByteString.copyFrom` ideal here?
        inner.getBodyBuilder()
                .getCryptoCreateAccountBuilder()
                .setKey(Key.newBuilder().setEd25519(ByteString.copyFrom(publicKey.toBytes())));

        return this;
    }

    public CryptoCreateTransaction setInitialBalance(long initialBalance) {
        inner.getBodyBuilder().getCryptoCreateAccountBuilder().setInitialBalance(initialBalance);
        return this;
    }

    // todo: setProxyAccountId
    // todo: setProxyFraction
    // todo: setMaxReceiveProxyFraction
    // todo: setSendRecordThreshold
    // todo: setReceiveRecordThreshold
    // todo: setReceiverSignatureRequired

    public CryptoCreateTransaction setAutoRenewPeriod(@Nonnull Duration autoRenewPeriod) {
        inner.getBodyBuilder()
                .getCryptoCreateAccountBuilder()
                .setAutoRenewPeriod(
                        com.hedera.sdk.proto.Duration.newBuilder()
                                .setSeconds(autoRenewPeriod.getSeconds())
                                .setNanos(autoRenewPeriod.getNano()));

        return this;
    }

    public CryptoCreateTransaction setShardId(long shardId) {
        inner.getBodyBuilder()
                .getCryptoCreateAccountBuilder()
                .setShardID(ShardID.newBuilder().setShardNum(shardId));

        return this;
    }

    public CryptoCreateTransaction setRealmId(long realmId) {
        inner.getBodyBuilder()
                .getCryptoCreateAccountBuilder()
                .setRealmID(RealmID.newBuilder().setRealmNum(realmId));

        return this;
    }

    // todo: setNewRealmAdminKey'
}
