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
import com.hedera.hashgraph.sdk.proto.AccountID;
import org.bouncycastle.util.encoders.Hex;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * The ID for a crypto-currency account on Hedera.
 */
public final class AccountId implements Comparable<AccountId> {
    private static final Pattern ALIAS_ID_REGEX = Pattern.compile("(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.((?:[0-9a-fA-F][0-9a-fA-F])+)$");

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
    public final PublicKey aliasKey;

    @Nullable
    public final EvmAddress evmAddress;

    @Nullable
    private final String checksum;

    /**
     * Assign the num part of the account id.
     *
     * @param num                       the num part of the account id
     */
    public AccountId(@Nonnegative long num) {
        this(0, 0, num);
    }

    /**
     * Assign all parts of the account id.
     *
     * @param shard                     the shard part of the account id
     * @param realm                     the realm part of the account id
     * @param num                       the num part of the account id
     */
    @SuppressWarnings("InconsistentOverloads")
    public AccountId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num) {
        this(shard, realm, num, null);
    }

    /**
     * Assign all parts of the account id.
     *
     * @param shard                     the shard part of the account id
     * @param realm                     the realm part of the account id
     * @param num                       the num part of the account id
     */
    @SuppressWarnings("InconsistentOverloads")
    AccountId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num, @Nullable String checksum) {
        this.shard = shard;
        this.realm = realm;
        this.num = num;
        this.checksum = checksum;
        this.aliasKey = null;
        this.evmAddress = null;
    }

    /**
     * Assign all parts of the account id.
     *
     * @param shard                     the shard part of the account id
     * @param realm                     the realm part of the account id
     * @param num                       the num part of the account id
     */
    @SuppressWarnings("InconsistentOverloads")
    AccountId(
        @Nonnegative long shard,
        @Nonnegative long realm,
        @Nonnegative long num,
        @Nullable String checksum,
        @Nullable PublicKey aliasKey,
        @Nullable EvmAddress evmAddress
    ) {
        this.shard = shard;
        this.realm = realm;
        this.num = num;
        this.checksum = checksum;
        this.aliasKey = aliasKey;
        this.evmAddress = evmAddress;
    }

    /**
     * Retrieve the account id from a string.
     *
     * @param id                        a string representing a valid account id
     * @return                          the account id object
     * @throws IllegalArgumentException when the account id and checksum are invalid
     */
    public static AccountId fromString(String id) {
        try {
            return EntityIdHelper.fromString(id, AccountId::new);
        } catch (IllegalArgumentException error) {
            var match = ALIAS_ID_REGEX.matcher(id);
            if (!match.find()) {
                throw new IllegalArgumentException(
                    "Invalid Account ID \"" + id + "\": format should look like 0.0.123 or 0.0.123-vfmkw or 0.0.1337BEEF (where 1337BEEF is a hex-encoded, DER-format public key)"
                );
            } else {
                byte[] aliasBytes = Hex.decode(match.group(3));
                boolean isEvmAddress = aliasBytes.length == 20;
                return new AccountId(
                    Long.parseLong(match.group(1)),
                    Long.parseLong(match.group(2)),
                    0,
                    null,
                    isEvmAddress ? null : PublicKey.fromBytesDER(aliasBytes),
                    isEvmAddress ? EvmAddress.fromBytes(aliasBytes) : null
                );
            }
        }
    }

    /**
     * Retrieve the account id from an EVM address.
     *
     * @param evmAddress                a string representing the EVM address
     * @return                          the account id object
     */
    public static AccountId fromEvmAddress(String evmAddress) {
        return fromEvmAddress(evmAddress, 0, 0);
    }

    /**
     * Retrieve the account id from an EVM address.
     *
     * @param evmAddress                a string representing the EVM address
     * @param shard                     the shard part of the account id
     * @param realm                     the shard realm of the account id
     * @return                          the account id object
     */
    public static AccountId fromEvmAddress(String evmAddress, @Nonnegative long shard, @Nonnegative long realm) {
        return new AccountId(
            shard,
            realm,
            0,
            null,
            null,
            EvmAddress.fromString(evmAddress)
        );
    }

    /**
     * Retrieve the account id from a solidity address.
     *
     * @param address                   a string representing the address
     * @return                          the account id object
     */
    public static AccountId fromSolidityAddress(String address) {
        return EntityIdHelper.fromSolidityAddress(address, AccountId::new);
    }

    /**
     * Retrieve the account id from a protobuf.
     *
     * @param accountId                 the protobuf
     * @return                          the account id object
     */
    static AccountId fromProtobuf(AccountID accountId) {
        Objects.requireNonNull(accountId);
        return new AccountId(
            accountId.getShardNum(),
            accountId.getRealmNum(),
            accountId.getAccountNum(),
            null,
            accountId.hasAlias() ? PublicKey.fromAliasBytes(accountId.getAlias()) : null,
            accountId.hasAlias() ? EvmAddress.fromAliasBytes(accountId.getAlias()) : null
        );
    }

    /**
     * Retrieve the account id from a protobuf byte array.
     *
     * @param bytes                     a byte array representation of the protobuf
     * @return                          the account id object
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    public static AccountId fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(AccountID.parseFrom(bytes).toBuilder().build());
    }

    /**
     * Extract the solidity address.
     *
     * @return                          the solidity address as a string
     */
    public String toSolidityAddress() {
        return EntityIdHelper.toSolidityAddress(shard, realm, num);
    }

    /**
     * Extract the account id protobuf.
     *
     * @return                          the account id builder
     */
    AccountID toProtobuf() {
        var accountIdBuilder = AccountID.newBuilder()
            .setShardNum(shard)
            .setRealmNum(realm);
        if (aliasKey != null) {
            accountIdBuilder.setAlias(aliasKey.toProtobufKey().toByteString());
        } else if (evmAddress != null) {
            accountIdBuilder.setAlias(ByteString.copyFrom(evmAddress.toBytes()));
        }else {
            accountIdBuilder.setAccountNum(num);
        }
        return accountIdBuilder.build();
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

    /**
     * Verify that the client has a valid checksum.
     *
     * @param client                    the client to verify
     * @throws BadEntityIdException     when the account id and checksum are invalid
     */
    public void validateChecksum(Client client) throws BadEntityIdException {
        if (aliasKey == null && evmAddress == null) {
            EntityIdHelper.validate(shard, realm, num, client, checksum);
        }
    }

    /**
     * Extract the checksum.
     *
     * @return                          the checksum
     */
    @Nullable
    public String getChecksum() {
        return checksum;
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
    public String toString() {
        if (aliasKey != null) {
            return "" + shard + "." + realm + "." + aliasKey.toStringDER();
        } else if (evmAddress != null) {
            return "" + shard + "." + realm + "." + evmAddress.toString();
        } else {
            return EntityIdHelper.toString(shard, realm, num);
        }
    }

    /**
     * Extract a string representation with the checksum.
     *
     * @param client                    the client
     * @return                          the account id with checksum
     */
    public String toStringWithChecksum(Client client) {
        if (aliasKey != null || evmAddress != null) {
            throw new IllegalStateException("toStringWithChecksum cannot be applied to AccountId with aliasKey or evmAddress");
        } else {
            return EntityIdHelper.toStringWithChecksum(shard, realm, num, client, checksum);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            shard, realm, num,
            (aliasKey != null) ? aliasKey.toBytes() : ((evmAddress != null) ? evmAddress.toBytes() : null)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof AccountId)) {
            return false;
        }

        AccountId otherId = (AccountId) o;
        if ((aliasKey == null) != (otherId.aliasKey == null)) {
            return false;
        }
        if ((evmAddress == null) != (otherId.evmAddress == null)) {
            return false;
        }
        return shard == otherId.shard && realm == otherId.realm && num == otherId.num &&
            (aliasKey == null || aliasKey.equals(otherId.aliasKey)) &&
            (evmAddress == null || evmAddress.equals(otherId.evmAddress));
    }

    @Override
    public int compareTo(AccountId o) {
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
        if ((aliasKey == null) != (o.aliasKey == null)) {
            return aliasKey != null ? 1 : -1;
        }
        if (aliasKey != null) {
            return aliasKey.toStringDER().compareTo(o.aliasKey.toStringDER());
        }
        if ((evmAddress == null) != (o.evmAddress == null)) {
            return evmAddress != null ? 1 : -1;
        }
        if (evmAddress == null) {
            return 0;
        }
        return evmAddress.toString().compareTo(o.evmAddress.toString());
    }
}
