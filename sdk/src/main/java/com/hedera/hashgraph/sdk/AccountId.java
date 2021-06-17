package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.AccountID;

import javax.annotation.Nonnegative;
import java.util.Objects;

/**
 * The ID for a crypto-currency account on Hedera.
 */
public final class AccountId {
    /**
     * The shard number
     */
    @Nonnegative
    public final long shard;

    /**
     * The realm number
     */
    @Nonnegative
    public final long realm;

    /**
     * The id number
     */
    @Nonnegative
    public final long num;

    public AccountId(@Nonnegative long num) {
        this(0, 0, num);
    }

    @SuppressWarnings("InconsistentOverloads")
    public AccountId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num) {
        this.shard = shard;
        this.realm = realm;
        this.num = num;
    }

    public static AccountId fromString(String id) {
        return EntityIdHelper.fromString(id, AccountId::new);
    }

    public static AccountId fromSolidityAddress(String address) {
        return EntityIdHelper.fromSolidityAddress(address, AccountId::new);
    }

    static AccountId fromProtobuf(AccountID accountId) {
        Objects.requireNonNull(accountId);
        return new AccountId(
            accountId.getShardNum(), accountId.getRealmNum(), accountId.getAccountNum());
    }

    public static AccountId fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(AccountID.parseFrom(bytes).toBuilder().build());
    }

    public String toSolidityAddress() {
        return EntityIdHelper.toSolidityAddress(shard, realm, num);
    }

    AccountID toProtobuf() {
        return AccountID.newBuilder()
            .setShardNum(shard)
            .setRealmNum(realm)
            .setAccountNum(num)
            .build();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }

    @Override
    public String toString() {
        return "" + shard + "." + realm + "." + num;
    }

    @Override
    public int hashCode() {
        return Objects.hash(shard, realm, num);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountId)) return false;

        AccountId otherId = (AccountId) o;
        return shard == otherId.shard && realm == otherId.realm && num == otherId.num;
    }
}
