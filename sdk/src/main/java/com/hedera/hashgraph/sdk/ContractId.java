package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.ContractID;

import javax.annotation.Nonnegative;
import java.util.Objects;

/**
 * The ID for a smart contract instance on Hedera.
 */
public final class ContractId extends Key {
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

    public ContractId(@Nonnegative long num) {
        this(0, 0, num);
    }

    @SuppressWarnings("InconsistentOverloads")
    public ContractId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num) {
        this.shard = shard;
        this.realm = realm;
        this.num = num;
    }

    public static ContractId fromString(String id) {
        return EntityIdHelper.fromString(id, ContractId::new);
    }

    public static ContractId fromSolidityAddress(String address) {
        return EntityIdHelper.fromSolidityAddress(address, ContractId::new);
    }

    static ContractId fromProtobuf(ContractID contractId) {
        return new ContractId(
            contractId.getShardNum(), contractId.getRealmNum(), contractId.getContractNum());
    }

    public static ContractId fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(ContractID.parseFrom(bytes).toBuilder().build());
    }

    public String toSolidityAddress() {
        return EntityIdHelper.toSolidityAddress(shard, realm, num);
    }

    ContractID toProtobuf() {
        return ContractID.newBuilder()
            .setShardNum(shard)
            .setRealmNum(realm)
            .setContractNum(num)
            .build();
    }

    @Override
    com.hedera.hashgraph.sdk.proto.Key toProtobufKey() {
        return com.hedera.hashgraph.sdk.proto.Key.newBuilder()
            .setContractID(toProtobuf())
            .build();
    }

    @Override
    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }

    @Override
    public String toString() {
        var checksum = EntityIdHelper.parseAddress("","" + shard + "." + realm + "." + num);
        return "" + shard + "." + realm + "." + num + "-" + checksum.correctChecksum;
    }

    @Override
    public int hashCode() {
        return Objects.hash(shard, realm, num);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContractId)) return false;

        ContractId otherId = (ContractId) o;
        return shard == otherId.shard && realm == otherId.realm && num == otherId.num;
    }
}
