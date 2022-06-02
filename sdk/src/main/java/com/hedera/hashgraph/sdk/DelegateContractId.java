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
import com.hedera.hashgraph.sdk.proto.ContractID;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * The ID for a smart contract instance on Hedera.
 */
public final class DelegateContractId extends ContractId {
    /**
     * Constructor.
     *
     * @param num                       the num portion of the contract id
     */
    public DelegateContractId(long num) {
        super(num);
    }

    /**
     * Constructor.
     *
     * @param shard                     the shard portion of the contract id
     * @param realm                     the realm portion of the contract id
     * @param num                       the num portion of the contract id
     */
    public DelegateContractId(long shard, long realm, long num) {
        super(shard, realm, num);
    }

    /**
     * Constructor.
     *
     * @param shard                     the shard portion of the contract id
     * @param realm                     the realm portion of the contract id
     * @param num                       the num portion of the contract id
     * @param checksum                  the optional checksum
     */
    DelegateContractId(long shard, long realm, long num, @Nullable String checksum) {
        super(shard, realm, num, checksum);
    }

    /**
     * Create a delegate contract id from a string.
     *
     * @param id                        the contract id
     * @return                          the delegate contract id object
     */
    public static DelegateContractId fromString(String id) {
        return EntityIdHelper.fromString(id, DelegateContractId::new);
    }

    /**
     * Create a delegate contract id from a string.
     *
     * @param address                   the contract id solidity address
     * @return                          the delegate contract id object
     */
    public static DelegateContractId fromSolidityAddress(String address) {
        return EntityIdHelper.fromSolidityAddress(address, DelegateContractId::new);
    }

    /**
     * Create a delegate contract id from a string.
     *
     * @param contractId                the contract id protobuf
     * @return                          the delegate contract id object
     */
    static DelegateContractId fromProtobuf(ContractID contractId) {
        Objects.requireNonNull(contractId);
        return new DelegateContractId(contractId.getShardNum(), contractId.getRealmNum(), contractId.getContractNum());
    }

    /**
     * Create a delegate contract id from a byte array.
     *
     * @param bytes                     the byte array
     * @return                          the delegate contract id object
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
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

