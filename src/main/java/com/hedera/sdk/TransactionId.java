package com.hedera.sdk;

import com.hedera.sdk.proto.Timestamp;
import com.hedera.sdk.proto.TransactionID;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoField;

public class TransactionId {
  TransactionID.Builder inner;

  public TransactionId(long accountNum) {
    this(0, 0, accountNum);
  }

  public TransactionId(long shardNum, long realmNum, long accountNum) {
    this(new AccountId(shardNum, realmNum, accountNum));
  }

  public TransactionId(AccountId accountId) {
    this(accountId, Clock.systemUTC().instant());
  }

  public TransactionId(AccountId accountId, Instant transactionValidStart) {
    inner =
        TransactionID.newBuilder()
            .setAccountID(accountId.inner)
            .setTransactionValidStart(
                Timestamp.newBuilder()
                    .setSeconds(transactionValidStart.getLong(ChronoField.INSTANT_SECONDS))
                    .setNanos(transactionValidStart.get(ChronoField.NANO_OF_SECOND)));
  }
}
