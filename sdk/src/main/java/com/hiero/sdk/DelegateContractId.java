// SPDX-License-Identifier: Apache-2.0
package com.hiero.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hiero.sdk.proto.ContractID;
import java.util.Objects;
import javax.annotation.Nullable;

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
    com.hiero.sdk.proto.Key toProtobufKey() {
        return com.hiero.sdk.proto.Key.newBuilder()
                .setDelegatableContractId(toProtobuf())
                .build();
    }

    @Override
    public boolean equals(Object o) {
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
