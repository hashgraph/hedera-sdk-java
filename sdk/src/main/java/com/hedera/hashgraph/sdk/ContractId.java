package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.ContractID;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
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

    @Nullable
    NetworkName network;

    @Nullable
    private final String checksum;

    public ContractId(@Nonnegative long num) {
        this(0, 0, num);
    }

    @SuppressWarnings("InconsistentOverloads")
    public ContractId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num) {
        this.shard = shard;
        this.realm = realm;
        this.num = num;
        this.network = null;
        this.checksum = null;
    }

    ContractId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num, @Nullable NetworkName network, @Nullable String checksum) {
        this.shard = shard;
        this.realm = realm;
        this.num = num;
        this.network = network;

        if (network != null) {
            if (checksum == null) {
                this.checksum = EntityIdHelper.checksum(network.toString(), shard + "." + realm + "." + num);
            } else {
                this.checksum = checksum;
            }
        } else {
            this.checksum = null;
        }
    }

    public static ContractId withNetwork(@Nonnegative long num, NetworkName network) {
        return new ContractId(0, 0, num, network, null);
    }

    public static ContractId withNetwork(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num, NetworkName network) {
        return new ContractId(shard, realm, num, network, null);
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
        return EntityIdHelper.toString(shard, realm, num, network, checksum);
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
