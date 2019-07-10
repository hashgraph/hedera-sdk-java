package com.hedera.hashgraph.sdk.contract;

import com.hedera.hashgraph.sdk.Entity;
import com.hedera.hashgraph.sdk.SolidityUtil;
import com.hedera.hashgraph.sdk.IdUtil;
import com.hedera.hashgraph.sdk.crypto.Key;
import com.hedera.hashgraph.sdk.proto.ContractID;
import com.hedera.hashgraph.sdk.proto.ContractIDOrBuilder;

import java.util.Objects;

public final class ContractId implements Key, Entity {
    private final ContractID.Builder inner;

    public ContractId(long shardNum, long realmNum, long contractNum) {
        inner = ContractID.newBuilder()
            .setShardNum(shardNum)
            .setRealmNum(realmNum)
            .setContractNum(contractNum);
    }

    public ContractId(ContractIDOrBuilder contractID) {
        this(contractID.getShardNum(), contractID.getRealmNum(), contractID.getContractNum());
    }

    /** Constructs a `ContractId` from a string formatted as <shardNum>.<realmNum>.<contractNum> */
    public static ContractId fromString(String account) throws IllegalArgumentException {

        long[] rawNums = IdUtil.parseIdString(account);
        var newContract = ContractID.newBuilder();

        try {
            newContract.setRealmNum(rawNums[0]);
            newContract.setShardNum(rawNums[1]);
            newContract.setContractNum(rawNums[2]);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Id format, should be in format {shardNum}.{realmNum}.{contractNum}");
        }

        return new ContractId(newContract);
    }

    public static ContractId fromSolidityAddress(String address) {
        return SolidityUtil.parseAddress(address, ContractId::new);
    }

    public long getShardNum() {
        return inner.getShardNum();
    }

    public long getRealmNum() {
        return inner.getRealmNum();
    }

    public long getContractNum() {
        return inner.getContractNum();
    }

    @Override
    public com.hedera.hashgraph.sdk.proto.Key toKeyProto() {
        return com.hedera.hashgraph.sdk.proto.Key.newBuilder()
            .setContractID(inner)
            .build();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getShardNum(), getRealmNum(), getContractNum());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;

        if (!(other instanceof ContractId)) return false;

        var otherId = (ContractId) other;
        return getShardNum() == otherId.getShardNum() && getRealmNum() == otherId.getRealmNum()
                && getContractNum() == otherId.getContractNum();
    }

    public ContractID toProto() {
        return inner.build();
    }

    @Override
    public String toString() {
        return "" + getShardNum() + "." + getRealmNum() + "." + getContractNum();
    }

    public String toSolidityAddress() {
        return SolidityUtil.addressFor(this);
    }
}
