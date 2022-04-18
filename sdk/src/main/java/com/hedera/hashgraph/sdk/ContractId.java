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

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.ContractID;
import org.bouncycastle.util.encoders.Hex;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * The ID for a smart contract instance on Hedera.
 */
public class ContractId extends Key implements Comparable<ContractId> {
    static final Pattern EVM_ADDRESS_REGEX = Pattern.compile("(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.([a-fA-F0-9]{40}$)");
    /**
     * The shard number
     */
    @Nonnegative
    public final long shard;

    /**
     * The realm number
     */
    @Nonnegative
    public final long realm;

    /**
     * The id number
     */
    @Nonnegative
    public final long num;

    @Nullable
    private final String checksum;

    @Nullable
    public final byte[] evmAddress;

    public ContractId(@Nonnegative long num) {
        this(0, 0, num);
    }

    @SuppressWarnings("InconsistentOverloads")
    public ContractId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num) {
        this(shard, realm, num, null);
    }

    @SuppressWarnings("InconsistentOverloads")
    ContractId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num, @Nullable String checksum) {
        this.shard = shard;
        this.realm = realm;
        this.num = num;
        this.checksum = checksum;
        this.evmAddress = null;
    }

    ContractId(@Nonnegative long shard, @Nonnegative long realm, byte[] evmAddress) {
        this.shard = shard;
        this.realm = realm;
        this.evmAddress = evmAddress;
        this.num = 0;
        this.checksum = null;
    }

    public static ContractId fromString(String id) {
        var match = EVM_ADDRESS_REGEX.matcher(id);
        if (match.find()) {
            return new ContractId(
                Long.parseLong(match.group(1)),
                Long.parseLong(match.group(2)),
                Hex.decode(match.group(3))
            );
        } else {
            return EntityIdHelper.fromString(id, ContractId::new);
        }
    }

    @Deprecated
    public static ContractId fromSolidityAddress(String address) {
        return EntityIdHelper.fromSolidityAddress(address, ContractId::new);
    }

    public static ContractId fromEvmAddress(@Nonnegative long shard, @Nonnegative long realm, String evmAddress) {
        return new ContractId(
            shard,
            realm,
            Hex.decode(evmAddress.startsWith("0x") ? evmAddress.substring(2) : evmAddress)
        );
    }

    static ContractId fromProtobuf(ContractID contractId) {
        Objects.requireNonNull(contractId);
        if (contractId.hasEvmAddress()) {
            return new ContractId(
                contractId.getShardNum(),
                contractId.getRealmNum(),
                contractId.getEvmAddress().toByteArray()
            );
        } else {
            return new ContractId(contractId.getShardNum(), contractId.getRealmNum(), contractId.getContractNum());
        }
    }

    public static ContractId fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(ContractID.parseFrom(bytes).toBuilder().build());
    }

    public String toSolidityAddress() {
        if (evmAddress != null) {
            return Hex.toHexString(evmAddress);
        } else {
            return EntityIdHelper.toSolidityAddress(shard, realm, num);
        }
    }

    ContractID toProtobuf() {
        var builder = ContractID.newBuilder()
            .setShardNum(shard)
            .setRealmNum(realm);
        if (evmAddress != null) {
            builder.setEvmAddress(ByteString.copyFrom(evmAddress));
        } else {
            builder.setContractNum(num);
        }
        return builder.build();
    }

    /**
     * @param client to validate against
     * @throws BadEntityIdException if entity ID is formatted poorly
     * @deprecated Use {@link #validateChecksum(Client)} instead.
     */
    @Deprecated
    public void validate(Client client) throws BadEntityIdException {
        validateChecksum(client);
    }

    public void validateChecksum(Client client) throws BadEntityIdException {
        EntityIdHelper.validate(shard, realm, num, client, checksum);
    }

    @Nullable
    public String getChecksum() {
        return checksum;
    }

    @Override
    com.hedera.hashgraph.sdk.proto.Key toProtobufKey() {
        return com.hedera.hashgraph.sdk.proto.Key.newBuilder()
            .setContractID(toProtobuf())
            .build();
    }

    @Override
    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }

    @Override
    public String toString() {
        if (evmAddress != null) {
            return "" + shard + "." + realm + "." + Hex.toHexString(evmAddress);
        } else {
            return EntityIdHelper.toString(shard, realm, num);
        }
    }

    public String toStringWithChecksum(Client client) {
        if (evmAddress != null) {
            throw new IllegalStateException("toStringWithChecksum cannot be applied to ContractId with evmAddress");
        } else {
            return EntityIdHelper.toStringWithChecksum(shard, realm, num, client, checksum);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(shard, realm, num, Arrays.hashCode(evmAddress));
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ContractId)) {
            return false;
        }

        ContractId otherId = (ContractId) o;
        return shard == otherId.shard && realm == otherId.realm && num == otherId.num && evmAddressMatches(otherId);
    }

    private boolean evmAddressMatches(ContractId otherId) {
        if ((evmAddress == null) != (otherId.evmAddress == null)) {
            return false;
        }
        if (evmAddress != null) {
            return Arrays.equals(evmAddress, otherId.evmAddress);
        }
        // both are null
        return true;
    }

    @Override
    public int compareTo(ContractId o) {
        Objects.requireNonNull(o);
        int shardComparison = Long.compare(shard, o.shard);
        if (shardComparison != 0) {
            return shardComparison;
        }
        int realmComparison = Long.compare(realm, o.realm);
        if (realmComparison != 0) {
            return realmComparison;
        }
        int numComparison = Long.compare(num, o.num);
        if (numComparison != 0) {
            return numComparison;
        }
        return evmAddressCompare(o);
    }

    private int evmAddressCompare(ContractId o) {
        int nullCompare = (evmAddress == null ? 0 : 1) - (o.evmAddress == null ? 0 : 1);
        if (nullCompare != 0) {
            return nullCompare;
        }
        if (evmAddress != null) {
            return Hex.toHexString(evmAddress).compareTo(Hex.toHexString(o.evmAddress));
        }
        // both are null
        return 0;
    }
}
