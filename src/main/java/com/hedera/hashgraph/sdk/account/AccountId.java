package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.proto.AccountID;
import com.hedera.hashgraph.proto.AccountIDOrBuilder;
import com.hedera.hashgraph.sdk.Entity;
import com.hedera.hashgraph.sdk.IdUtil;
import com.hedera.hashgraph.sdk.Internal;
import com.hedera.hashgraph.sdk.SolidityUtil;

import java.util.Objects;

public final class AccountId implements Entity {
    public final long shard;
    public final long realm;
    public final long account;

    /** Constructs an `AccountId` with `0` for `shard` and `realm` (e.g., `0.0.<accountNum>`). */
    public AccountId(long accountNum) {
        this(0, 0, accountNum);
    }

    public AccountId(long shard, long realm, long account) {
        this.shard = shard;
        this.realm = realm;
        this.account = account;
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

    @Deprecated
    public long getShardNum() {
        return shard;
    }

    @Deprecated
    public long getRealmNum() {
        return realm;
    }

    @Deprecated
    public long getAccountNum() {
        return account;
    }

    @Override
    public String toString() {
        return "" + shard + "." + realm + "." + account;
    }

    public String toSolidityAddress() {
        return SolidityUtil.addressFor(this);
    }

    @Internal
    public AccountID toProto() {
        return AccountID.newBuilder()
            .setShardNum(shard)
            .setRealmNum(realm)
            .setAccountNum(account)
            .build();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;

        if (other == null || getClass() != other.getClass()) return false;

        AccountId otherId = (AccountId) other;
        return otherId.account == account
            && otherId.realm == realm
            && otherId.shard == shard;
    }

    @Override
    public int hashCode() {
        return Objects.hash(account, realm, shard);
    }
}
