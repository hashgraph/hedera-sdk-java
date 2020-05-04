package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.AccountID;

import javax.annotation.Nonnegative;

/**
 * The ID for an a crypto-currency account on Hedera.
 */
public final class AccountId extends EntityId {
    public AccountId(@Nonnegative long num) {
        super(0, 0, num);
    }

    @SuppressWarnings("InconsistentOverloads")
    public AccountId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num) {
        super(shard, realm, num);
    }

    public static AccountId fromString(String id) {
        return EntityId.fromString(id, AccountId::new);
    }

    public static AccountId fromSolidityAddress(String address) {
        return EntityId.fromSolidityAddress(address, AccountId::new);
    }

    static AccountId fromProtobuf(AccountID accountId) {
        return new AccountId(
            accountId.getShardNum(), accountId.getRealmNum(), accountId.getAccountNum());
    }

    public static AccountId fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(AccountID.parseFrom(bytes).toBuilder().build());
    }

    @Override
    public String toSolidityAddress() {
        return super.toSolidityAddress();
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
}
