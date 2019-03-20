package com.hedera.sdk;

import com.hedera.sdk.proto.ContractID;

public class ContractId {
    transient ContractID.Builder inner;

    public ContractId(long shardNum, long realmNum, long contractNum) {
        inner = ContractID.newBuilder()
            .setShardNum(shardNum)
            .setRealmNum(realmNum)
            .setContractNum(contractNum);
    }

    public long getShardNum() { return inner.getShardNum(); }
    public long getRealmNum() { return inner.getRealmNum(); }
    public long getContractNum() { return inner.getContractNum(); }
}
