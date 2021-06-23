package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.AccountID;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
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

    @Nullable
    NetworkName network;

    @Nullable
    final String checksum;

    public AccountId(@Nonnegative long num) {
        this(0, 0, num);
    }

    @SuppressWarnings("InconsistentOverloads")
    public AccountId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num) {
        this.shard = shard;
        this.realm = realm;
        this.num = num;
        this.network = null;
        this.checksum = null;
    }

    AccountId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num, @Nullable NetworkName network, @Nullable String checksum) {
        this.shard = shard;
        this.realm = realm;
        this.num = num;
        this.network = network;

        if (network != null) {
            if (checksum == null) {
                this.checksum = EntityIdHelper.checksum(Integer.toString(network.id), shard + "." + realm + "." + num);
            } else {
                this.checksum = checksum;
            }
        } else {
            this.checksum = null;
        }
    }

    public static AccountId withNetwork(@Nonnegative long num, NetworkName network) {
        return new AccountId(0, 0, num, network, null);
    }

    public static AccountId withNetwork(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num, NetworkName network) {
        return new AccountId(shard, realm, num, network, null);
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
        return EntityIdHelper.toString(shard, realm, num, network, checksum);
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
