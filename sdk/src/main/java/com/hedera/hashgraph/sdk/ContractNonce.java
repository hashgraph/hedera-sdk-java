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

import com.google.common.base.MoreObjects;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.ContractNonceInfo;

import java.util.Objects;

/**
 * Info about a contract account's nonce value.
 * A nonce of a contract is only incremented when that contract creates another contract.
 */
public final class ContractNonce {

    /**
     * The ID of the contract.
     */
    public final ContractId contractId;

    /**
     * The current value of the contract account's nonce property.
     */
    public final long nonce;

    /**
     * Constructor.
     *
     * @param contractId                the contract ID
     * @param nonce                     the value of the contract account's nonce
     */
    public ContractNonce(
        ContractId contractId,
        long nonce
    ) {
        this.contractId = contractId;
        this.nonce = nonce;
    }

    /**
     * Retrieve the contract nonce from a protobuf.
     *
     * @param contractNonce             the contract nonce protobuf
     * @return                          the contract nonce object
     */
    static ContractNonce fromProtobuf(ContractNonceInfo contractNonce) {
        return new ContractNonce(
            ContractId.fromProtobuf(contractNonce.getContractId()),
            contractNonce.getNonce()
        );
    }

    /**
     * Retrieve the contract nonce from a protobuf byte array.
     *
     * @param bytes                                 a byte array representing the protobuf
     * @return                                      the contract nonce object
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    public static ContractNonce fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(ContractNonceInfo.parseFrom(bytes).toBuilder().build());
    }

    /**
     * Convert a contract nonce object into a protobuf.
     *
     * @return                          the protobuf object
     */
    ContractNonceInfo toProtobuf() {
        var contractNonceBuilder = ContractNonceInfo.newBuilder()
            .setContractId(contractId.toProtobuf())
            .setNonce(nonce);

        return contractNonceBuilder.build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("contractId", contractId)
            .add("nonce", nonce)
            .toString();
    }

    /**
     * Extract a byte array representation.
     *
     * @return                          a byte array representation
     */
    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }


    @Override
    public int hashCode() {
        return Objects.hash(contractId, nonce);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ContractNonce)) {
            return false;
        }

        ContractNonce otherNonce = (ContractNonce)o;

        return contractId.equals(otherNonce.contractId) && nonce == otherNonce.nonce;
    }
}
