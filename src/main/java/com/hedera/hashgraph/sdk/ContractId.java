package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ContractID;
import javax.annotation.Nonnegative;

public final class ContractId extends EntityId {
    public ContractId(@Nonnegative long num) {
        super(0, 0, num);
    }

    @SuppressWarnings("InconsistentOverloads")
    public ContractId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num) {
        super(shard, realm, num);
    }

    static ContractId fromProtobuf(ContractID contractId) {
        return new ContractId(
                contractId.getShardNum(), contractId.getRealmNum(), contractId.getContractNum());
    }

    ContractID toProtobuf() {
        return ContractID.newBuilder()
                .setShardNum(shard)
                .setRealmNum(realm)
                .setContractNum(num)
                .build();
    }
}
