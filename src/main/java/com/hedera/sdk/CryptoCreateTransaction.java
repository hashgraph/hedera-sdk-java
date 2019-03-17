package com.hedera.sdk;

import com.hedera.sdk.proto.RealmID;
import com.hedera.sdk.proto.ShardID;
import java.time.Duration;
import javax.annotation.Nonnull;

public class CryptoCreateTransaction extends TransactionBodyBuilder<CryptoCreateTransaction> {
  // todo: setKey

  public CryptoCreateTransaction() {
    // Recommendation from Hedera to set this to ~1 month
    setAutoRenewPeriod(Duration.ofSeconds(2_592_000));

    // Default to maximum values for record thresholds. Without this records would be auto-created
    // whenever a send or receive transaction takes place for this new account. This should
    // be an explicit ask.
    inner
        .getCryptoCreateAccountBuilder()
        .setSendRecordThreshold(Long.MAX_VALUE)
        .setReceiveRecordThreshold(Long.MAX_VALUE);
  }

  @Override
  public CryptoCreateTransaction setTransactionId(@Nonnull TransactionId transactionId) {
    // Setting the transaction ID defaults the shard and realm IDs
    // If you truly want to create a _new_ realm, then you need to null the realm after setting this

    if (!inner.getCryptoCreateAccountBuilder().hasShardID()) {
      setShardId(transactionId.inner.getAccountID().getShardNum());
    }

    if (!inner.getCryptoCreateAccountBuilder().hasRealmID()) {
      setRealmId(transactionId.inner.getAccountID().getRealmNum());
    }

    return super.setTransactionId(transactionId);
  }

  public CryptoCreateTransaction setInitialBalance(long initialBalance) {
    inner.getCryptoCreateAccountBuilder().setInitialBalance(initialBalance);
    return this;
  }

  // todo: setProxyAccountId
  // todo: setProxyFraction
  // todo: setMaxReceiveProxyFraction
  // todo: setSendRecordThreshold
  // todo: setReceiveRecordThreshold
  // todo: setReceiverSignatureRequired

  public CryptoCreateTransaction setAutoRenewPeriod(@Nonnull Duration autoRenewPeriod) {
    inner
        .getCryptoCreateAccountBuilder()
        .setAutoRenewPeriod(
            com.hedera.sdk.proto.Duration.newBuilder()
                .setSeconds(autoRenewPeriod.getSeconds())
                .setNanos(autoRenewPeriod.getNano()));

    return this;
  }

  public CryptoCreateTransaction setShardId(long shardId) {
    inner
        .getCryptoCreateAccountBuilder()
        .setShardID(ShardID.newBuilder().setShardNum(shardId).build());

    return this;
  }

  public CryptoCreateTransaction setRealmId(long realmId) {
    inner
        .getCryptoCreateAccountBuilder()
        .setRealmID(RealmID.newBuilder().setRealmNum(realmId).build());

    return this;
  }

  // todo: setNewRealmAdminKey'
}
