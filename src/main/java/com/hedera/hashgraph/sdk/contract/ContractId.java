package com.hedera.hashgraph.sdk.contract;

import com.hedera.hashgraph.proto.ContractID;
import com.hedera.hashgraph.proto.ContractIDOrBuilder;
import com.hedera.hashgraph.sdk.IdUtil;
import com.hedera.hashgraph.sdk.Internal;
import com.hedera.hashgraph.sdk.SolidityUtil;
import com.hedera.hashgraph.sdk.crypto.PublicKey;

import java.util.Objects;

public final class ContractId extends PublicKey {
    public final long shard;
    public final long realm;
    public final long contract;

    public ContractId(long shard, long realm, long contract) {
        this.shard = shard;
        this.realm = realm;
        this.contract = contract;
    }

    public ContractId(ContractIDOrBuilder contractID) {
        this(contractID.getShardNum(), contractID.getRealmNum(), contractID.getContractNum());
    }

    /** Constructs a `ContractId` from a string formatted as <shardNum>.<realmNum>.<contractNum> */
    public static ContractId fromString(String account) throws IllegalArgumentException {
        return IdUtil.parseIdString(account, ContractId::new);
    }

    public static ContractId fromSolidityAddress(String address) {
        return SolidityUtil.parseAddress(address, ContractId::new);
    }

    @Override
    public com.hedera.hashgraph.proto.Key toKeyProto() {
        return com.hedera.hashgraph.proto.Key.newBuilder()
            .setContractID(toProto())
            .build();
    }

    @Override
    public int hashCode() {
        return Objects.hash(shard, realm, contract);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;

        if (!(other instanceof ContractId)) return false;

        ContractId otherId = (ContractId) other;
        return shard == otherId.shard
            && realm == otherId.realm
            && contract == otherId.contract;
    }

    @Internal
    public ContractID toProto() {
        return ContractID.newBuilder()
            .setShardNum(shard)
            .setRealmNum(realm)
            .setContractNum(contract)
            .build();
    }

    @Override
    public String toString() {
        return "" + shard + "." + realm + "." + contract;
    }

    public String toSolidityAddress() {
        return SolidityUtil.addressFor(this);
    }
}
