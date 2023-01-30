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
public final class VirtualAddress {

    /**
     * The EVM address.
     */
    public final String address;

    /**
     * If true, then this is the default virtual address for a given account.
     */
    public final boolean isDefault;

    /**
     * Constructor.
     *
     * @param address                   the virtual address
     * @param isDefault                 is it the default address
     */
    public VirtualAddress(
        String address,
        boolean isDefault
    ) {
        this.address = address;
        this.isDefault = isDefault;
    }

    /**
     * Retrieve the virtual address from a protobuf.
     *
     * @param virtualAddress            the virtual address protobuf
     * @return                          the virtual address object
     */
    static VirtualAddress fromProtobuf(com.hedera.hashgraph.sdk.proto.VirtualAddress virtualAddress) {
        return new VirtualAddress(
            virtualAddress.getAddress().toString(),
            virtualAddress.getIsDefault()
        );
    }

    /**
     * Retrieve the virtual address from a protobuf byte array.
     *
     * @param bytes                                 a byte array representing the protobuf
     * @return                                      the virtual address object
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    public static VirtualAddress fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.VirtualAddress.parseFrom(bytes).toBuilder().build());
    }

    /**
     * Convert a virtual address object into a protobuf.
     *
     * @return                          the protobuf object
     */
    com.hedera.hashgraph.sdk.proto.VirtualAddress toProtobuf() {
        var virtualAddressBuilder = com.hedera.hashgraph.sdk.proto.VirtualAddress.newBuilder()
            .setAddress(ByteString.fromHex(address))
            .setIsDefault(isDefault);

        return virtualAddressBuilder.build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("address", address)
            .add("default", isDefault)
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
        return Objects.hash(address, isDefault);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof VirtualAddress)) {
            return false;
        }

        VirtualAddress otherAddress = (VirtualAddress)o;
        // TODO - should addresses "0x123abc" and "123abc" be considered equal???
        return address.equals(otherAddress.address) && isDefault == otherAddress.isDefault;
    }
}
