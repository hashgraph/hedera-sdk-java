package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.AccountID;

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
    }

    @SuppressWarnings("InconsistentOverloads")
    AccountId(
        @Nonnegative long shard,
        @Nonnegative long realm,
        @Nonnegative long num,
        @Nullable String checksum,
        @Nullable PublicKey aliasKey
    ) {
        this.shard = shard;
        this.realm = realm;
        this.num = num;
        this.checksum = checksum;
        this.aliasKey = aliasKey;
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
                return new AccountId(
                    Long.parseLong(match.group(1)),
                    Long.parseLong(match.group(2)),
                    0,
                    null,
                    PublicKey.fromString(match.group(3))
                );
            }
        }
    }

    public static AccountId fromSolidityAddress(String address) {
        return EntityIdHelper.fromSolidityAddress(address, AccountId::new);
    }

    static AccountId fromProtobuf(AccountID accountId) {
        Objects.requireNonNull(accountId);
        return new AccountId(
            accountId.getShardNum(),
            accountId.getRealmNum(),
            accountId.getAccountNum(),
            null,
            PublicKey.fromAliasBytes(accountId.getAlias())
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
        } else {
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
