package com.hedera.sdk;

import com.hedera.sdk.proto.RealmID;
import com.hedera.sdk.proto.ShardID;

import javax.annotation.Nonnull;
import java.time.Duration;

public class CryptoCreateTransaction extends TransactionBodyBuilder<CryptoCreateTransaction> {
  // todo: setKey


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
    inner.getCryptoCreateAccountBuilder().setAutoRenewPeriod(
      com.hedera.sdk.proto.Duration.newBuilder()
        .setSeconds(autoRenewPeriod.getSeconds())
        .setNanos(autoRenewPeriod.getNano()));

    return this;
  }

  public CryptoCreateTransaction setShardId(long shardId) {
    inner.getCryptoCreateAccountBuilder().setShardID(ShardID.newBuilder().setShardNum(shardId).build());

    return this;
  }

  public CryptoCreateTransaction setRealmId(long realmId) {
    inner.getCryptoCreateAccountBuilder().setRealmID(RealmID.newBuilder().setRealmNum(realmId).build());

    return this;
  }

  // todo: setNewRealmAdminKey'
}
