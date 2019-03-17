package com.hedera.sdk;

import com.hedera.sdk.proto.AccountID;

public class AccountId {
  AccountID.Builder inner;

  public AccountId(long accountNum) {
    this(0, 0, accountNum);
  }

  public AccountId(long shardNum, long realmNum, long accountNum) {
    inner =
        AccountID.newBuilder()
            .setRealmNum(realmNum)
            .setShardNum(shardNum)
            .setAccountNum(accountNum);
  }

  public long getShardNum() {
    return inner.getShardNum();
  }

  public long getRealmNum() {
    return inner.getRealmNum();
  }

  public long getAccountNum() {
    return inner.getAccountNum();
  }
}
