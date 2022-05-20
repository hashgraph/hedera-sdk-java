/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.List;

/**
 * The storage changes to a smart contract's storage as a side effect of the function call.
 * {@link https://docs.hedera.com/guides/docs/hedera-api/smart-contracts/contractcalllocal#contractstatechange}
 */
public class ContractStateChange {
    public final ContractId contractId;
    public final List<StorageChange> storageChanges;

    /**
     * Constructor.
     *
     * @param contractId                the contract id
     * @param storageChanges            the list of storage change objects
     */
    ContractStateChange(ContractId contractId, List<StorageChange> storageChanges) {
        this.contractId = contractId;
        this.storageChanges = storageChanges;
    }

    /**
     * Create contract stage change object from protobuf.
     *
     * @param stateChangeProto          the protobuf
     * @return                          the contract stage change object
     */
    static ContractStateChange fromProtobuf(com.hedera.hashgraph.sdk.proto.ContractStateChange stateChangeProto) {
        List<StorageChange> storageChanges = new ArrayList<>(stateChangeProto.getStorageChangesCount());
        for (var storageChangeProto : stateChangeProto.getStorageChangesList()) {
            storageChanges.add(StorageChange.fromProtobuf(storageChangeProto));
        }
        return new ContractStateChange(ContractId.fromProtobuf(stateChangeProto.getContractID()), storageChanges);
    }

    /**
     * Create contract stage change object from byte array.
     *
     * @param bytes                     the byte array
     * @return                          the contract stage change object
     * @throws InvalidProtocolBufferException
     */
    public static ContractStateChange fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.ContractStateChange.parseFrom(bytes));
    }

    /**
     * @return                          the protobuf representation
     */
    com.hedera.hashgraph.sdk.proto.ContractStateChange toProtobuf() {
        var builder = com.hedera.hashgraph.sdk.proto.ContractStateChange.newBuilder()
            .setContractID(contractId.toProtobuf());
        for (var storageChange : storageChanges) {
            builder.addStorageChanges(storageChange.toProtobuf());
        }
        return builder.build();
    }

    /**
     * @return                          the byte array representation
     */
    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
