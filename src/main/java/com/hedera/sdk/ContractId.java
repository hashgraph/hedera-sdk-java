package com.hedera.sdk;

import com.hedera.sdk.proto.ContractID;
import com.hedera.sdk.proto.Key;

public class ContractId {
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

    // duplicating this method from the `IPublicKey` interface because it doesn't make sense for
    // this to implement it
    Key toProtoKey() {
        return Key.newBuilder().setContractID(inner).build();
    }
}
