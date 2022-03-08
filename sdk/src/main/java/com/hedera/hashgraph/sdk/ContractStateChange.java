package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.List;

public class ContractStateChange {
    public final ContractId contractId;
    public final List<StorageChange> storageChanges;

    ContractStateChange(ContractId contractId, List<StorageChange> storageChanges) {
        this.contractId = contractId;
        this.storageChanges = storageChanges;
    }

    static ContractStateChange fromProtobuf(com.hedera.hashgraph.sdk.proto.ContractStateChange stateChangeProto) {
        List<StorageChange> storageChanges = new ArrayList<>(stateChangeProto.getStorageChangesCount());
        for (var storageChangeProto : stateChangeProto.getStorageChangesList()) {
            storageChanges.add(StorageChange.fromProtobuf(storageChangeProto));
        }
        return new ContractStateChange(ContractId.fromProtobuf(stateChangeProto.getContractID()), storageChanges);
    }

    public static ContractStateChange fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.ContractStateChange.parseFrom(bytes));
    }

    com.hedera.hashgraph.sdk.proto.ContractStateChange toProtobuf() {
        var builder = com.hedera.hashgraph.sdk.proto.ContractStateChange.newBuilder()
            .setContractID(contractId.toProtobuf());
        for (var storageChange : storageChanges) {
            builder.addStorageChanges(storageChange.toProtobuf());
        }
        return builder.build();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
