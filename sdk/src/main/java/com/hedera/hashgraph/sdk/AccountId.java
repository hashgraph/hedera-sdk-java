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
    public final byte[] aliasEvmAddress;

    @Nullable
    private final String checksum;

    public AccountId(@Nonnegative long num) {
        this(0, 0, num);
    }

    @SuppressWarnings("InconsistentOverloads")
    public AccountId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num) {
        this(shard, realm, num, null);
    }

    @SuppressWarnings("InconsistentOverloads")
    AccountId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num, @Nullable String checksum) {
        this.shard = shard;
        this.realm = realm;
        this.num = num;
        this.checksum = checksum;
        this.aliasKey = null;
        this.aliasEvmAddress = null;
    }

    @SuppressWarnings("InconsistentOverloads")
    AccountId(
        @Nonnegative long shard,
        @Nonnegative long realm,
        @Nonnegative long num,
        @Nullable String checksum,
        @Nullable PublicKey aliasKey,
        @Nullable byte[] aliasEvmAddress
    ) {
        this.shard = shard;
        this.realm = realm;
        this.num = num;
        this.checksum = checksum;
        this.aliasKey = aliasKey;
        this.aliasEvmAddress = aliasEvmAddress;
    }

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
                    isEvmAddress ? aliasBytes : null
                );
            }
        }
    }

    public static AccountId fromSolidityAddress(String address) {
        return EntityIdHelper.fromSolidityAddress(address, AccountId::new);
    }

    static AccountId fromProtobuf(AccountID accountId) {
        Objects.requireNonNull(accountId);
        boolean aliasIsEvmAddress = accountId.getAlias().size() == 20;
        return new AccountId(
            accountId.getShardNum(),
            accountId.getRealmNum(),
            accountId.getAccountNum(),
            null,
            aliasIsEvmAddress ? null : PublicKey.fromAliasBytes(accountId.getAlias()),
            aliasIsEvmAddress ? accountId.getAlias().toByteArray() : null
        );
    }

    public static AccountId fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(AccountID.parseFrom(bytes).toBuilder().build());
    }

    public String toSolidityAddress() {
        return EntityIdHelper.toSolidityAddress(shard, realm, num);
    }

    AccountID toProtobuf() {
        var accountIdBuilder = AccountID.newBuilder()
            .setShardNum(shard)
            .setRealmNum(realm);
        if (aliasKey != null) {
            accountIdBuilder.setAlias(aliasKey.toProtobufKey().toByteString());
        } else if (aliasEvmAddress != null) {
            accountIdBuilder.setAlias(ByteString.copyFrom(aliasEvmAddress));
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

    public void validateChecksum(Client client) throws BadEntityIdException {
        if (aliasKey == null) {
            EntityIdHelper.validate(shard, realm, num, client, checksum);
        }
    }

    @Nullable
    public String getChecksum() {
        return checksum;
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }

    @Override
    public String toString() {
        if (aliasKey != null) {
            return "" + shard + "." + realm + "." + aliasKey.toStringDER();
        } else if (aliasEvmAddress != null) {
            return "" + shard + "." + realm + "." + Hex.toHexString(aliasEvmAddress);
        } else {
            return EntityIdHelper.toString(shard, realm, num);
        }
    }

    public String toStringWithChecksum(Client client) {
        if (aliasKey != null) {
            throw new IllegalStateException("toStringWithChecksum cannot be applied to AccountId with aliasKey");
        } else {
            return EntityIdHelper.toStringWithChecksum(shard, realm, num, client, checksum);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(shard, realm, num, (aliasKey != null) ? aliasKey.toBytes() : null);
    }

    @Override
    public boolean equals(@Nullable Object o) {
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
        return shard == otherId.shard && realm == otherId.realm && num == otherId.num &&
            (aliasKey == null || aliasKey.equals(otherId.aliasKey));
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
        if (aliasKey == null) {
            return 0;
        }
        return aliasKey.toStringDER().compareTo(o.aliasKey.toStringDER());
    }
}
