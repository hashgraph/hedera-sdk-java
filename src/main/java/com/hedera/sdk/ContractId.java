package com.hedera.sdk;

import com.hedera.sdk.crypto.Key;
import com.hedera.sdk.proto.ContractID;

public class ContractId implements Key, Entity {
    transient ContractID.Builder inner;

    public ContractId(long shardNum, long realmNum, long contractNum) {
        inner =
                ContractID.newBuilder()
                        .setShardNum(shardNum)
                        .setRealmNum(realmNum)
                        .setContractNum(contractNum);
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
    public com.hedera.sdk.proto.Key toKeyProto() {
        return com.hedera.sdk.proto.Key.newBuilder().setContractID(inner).build();
    }

    public ContractID toProto() {
        return inner.build();
    }
}
