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
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.Objects;

/**
 * EVM virtual address.
 */
public final class VirtualAddressInfo {

    /**
     * The EVM address.
     */
    public final String address;

    /**
     * If true, then this is the default virtual address for a given account.
     */
    public final boolean isDefault;

    /**
     * The EthereumTransaction type nonce associated with this virtual address.
     */
    public final long nonce;

    /**
     * Constructor.
     *
     * @param address                   the virtual address
     * @param isDefault                 is it the default address
     * @param nonce                     nonce associated with this virtual address
     */
    public VirtualAddressInfo(
        String address,
        boolean isDefault,
        long nonce
    ) {
        this.address = address;
        this.isDefault = isDefault;
        this.nonce = nonce;
    }

    /**
     * Retrieve the virtual address from a protobuf.
     *
     * @param virtualAddress            the virtual address protobuf
     * @return                          the virtual address object
     */
    static VirtualAddressInfo fromProtobuf(com.hedera.hashgraph.sdk.proto.CryptoGetInfoResponse.AccountInfo.VirtualAddressInfo virtualAddress) {
        return new VirtualAddressInfo(
            virtualAddress.getAddress().toString(),
            virtualAddress.getIsDefault(),
            virtualAddress.getNonce()
        );
    }

    /**
     * Retrieve the virtual address from a protobuf byte array.
     *
     * @param bytes                                 a byte array representing the protobuf
     * @return                                      the virtual address object
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    public static VirtualAddressInfo fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.CryptoGetInfoResponse.AccountInfo.VirtualAddressInfo.parseFrom(bytes).toBuilder().build());
    }

    /**
     * Convert a virtual address object into a protobuf.
     *
     * @return                          the protobuf object
     */
    com.hedera.hashgraph.sdk.proto.CryptoGetInfoResponse.AccountInfo.VirtualAddressInfo toProtobuf() {
        var virtualAddressBuilder = com.hedera.hashgraph.sdk.proto.CryptoGetInfoResponse.AccountInfo.VirtualAddressInfo.newBuilder()
            .setAddress(ByteString.fromHex(address))
            .setIsDefault(isDefault)
            .setNonce(nonce);

        return virtualAddressBuilder.build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("address", address)
            .add("default", isDefault)
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
        // TODO - should addresses "0x123abc" and "123abc" be considered equal???
        return Objects.hash(address, isDefault, nonce);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof VirtualAddressInfo)) {
            return false;
        }

        VirtualAddressInfo otherAddress = (VirtualAddressInfo)o;
        // TODO - should addresses "0x123abc" and "123abc" be considered equal???
        return
            address.equals(otherAddress.address) &&
            isDefault == otherAddress.isDefault &&
            nonce == otherAddress.nonce;
    }
}
