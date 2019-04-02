package com.hedera.sdk;

import com.hedera.sdk.proto.AccountID;
import com.hedera.sdk.proto.AccountIDOrBuilder;

import java.util.Objects;

// TODO: AccountId.fromString

public final class AccountId implements Entity {
    final AccountID.Builder inner;

    /** Constructs an `AccountId` with `0` for `shard` and `realm` (e.g., `0.0.<accountNum>`). */
    public AccountId(long accountNum) {
        this(0, 0, accountNum);
    }

    public AccountId(long shardNum, long realmNum, long accountNum) {
        inner = AccountID.newBuilder()
            .setRealmNum(realmNum)
            .setShardNum(shardNum)
            .setAccountNum(accountNum);
    }

    public static AccountId fromProto(AccountIDOrBuilder accountID) {
        return new AccountId(accountID.getShardNum(), accountID.getRealmNum(), accountID.getAccountNum());
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

    public AccountID toProto() {
        return inner.build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        var other = (AccountId) o;
        return other.getAccountNum() == getAccountNum() && other.getRealmNum() == getRealmNum()
                && other.getShardNum() == this.getShardNum();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAccountNum(), getRealmNum(), getShardNum());
    }
}
