package com.hedera.hashgraph.sdk.account;

import com.google.common.base.Splitter;
import com.hedera.hashgraph.sdk.Entity;
import com.hedera.hashgraph.sdk.IdUtil;
import com.hedera.hashgraph.sdk.SolidityUtil;
import com.hedera.hashgraph.sdk.proto.AccountID;
import com.hedera.hashgraph.sdk.proto.AccountIDOrBuilder;

import java.util.Objects;

public final class AccountId implements Entity {
    private final AccountID.Builder inner;

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

    /** Constructs an `AccountId` from a string formatted as <shardNum>.<realmNum>.<accountNum> */
    public static AccountId fromString(String account) throws IllegalArgumentException {
        return IdUtil.parseIdString(account, AccountId::new);
    }

    public AccountId(AccountIDOrBuilder accountId) {
        this(accountId.getShardNum(), accountId.getRealmNum(), accountId.getAccountNum());
    }

    public static AccountId fromSolidityAddress(String address) {
        return SolidityUtil.parseAddress(address, AccountId::new);
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
        return "" + getShardNum() + "." + getRealmNum() + "." + getAccountNum();
    }

    public String toSolidityAddress() {
        return SolidityUtil.addressFor(this);
    }

    public AccountID toProto() {
        return inner.build();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;

        if (other == null || getClass() != other.getClass()) return false;

        var otherId = (AccountId) other;
        return otherId.getAccountNum() == getAccountNum() && otherId.getRealmNum() == getRealmNum()
                && otherId.getShardNum() == getShardNum();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAccountNum(), getRealmNum(), getShardNum());
    }
}
