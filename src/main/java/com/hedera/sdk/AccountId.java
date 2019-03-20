package com.hedera.sdk;

import com.hedera.sdk.proto.AccountID;

// TODO: AccountId.fromString

public final class AccountId {
    final AccountID.Builder inner;

    /** Constructs an `AccountId` with `0` for `shard` and `realm` (e.g., `0.0.<accountNum>`). */
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

    @Override
    public String toString() {
        return String.format("%d.%d.%d", getShardNum(), getRealmNum(), getAccountNum());
    }
}
