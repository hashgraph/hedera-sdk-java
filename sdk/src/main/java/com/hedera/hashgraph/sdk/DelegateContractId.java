package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.ContractID;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * The ID for a smart contract instance on Hedera.
 */
public final class DelegateContractId extends ContractId {
    public DelegateContractId(long num) {
        super(num);
    }

    public DelegateContractId(long shard, long realm, long num) {
        super(shard, realm, num);
    }

    DelegateContractId(long shard, long realm, long num, @Nullable String checksum) {
        super(shard, realm, num, checksum);
    }

    public static DelegateContractId fromString(String id) {
        return EntityIdHelper.fromString(id, DelegateContractId::new);
    }

    public static DelegateContractId fromSolidityAddress(String address) {
        return EntityIdHelper.fromSolidityAddress(address, DelegateContractId::new);
    }

    static DelegateContractId fromProtobuf(ContractID contractId) {
        Objects.requireNonNull(contractId);
        return new DelegateContractId(contractId.getShardNum(), contractId.getRealmNum(), contractId.getContractNum());
    }

    public static DelegateContractId fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(ContractID.parseFrom(bytes).toBuilder().build());
    }

    @Override
    com.hedera.hashgraph.sdk.proto.Key toProtobufKey() {
        return com.hedera.hashgraph.sdk.proto.Key.newBuilder()
            .setDelegatableContractId(toProtobuf())
            .build();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }

        if (o instanceof DelegateContractId) {
            DelegateContractId otherId = (DelegateContractId) o;
            return shard == otherId.shard && realm == otherId.realm && num == otherId.num;
        } else if (o instanceof ContractId) {
            ContractId otherId = (ContractId) o;
            return shard == otherId.shard && realm == otherId.realm && num == otherId.num;
        } else {
            return false;
        }

    }
}

