package com.hedera.hashgraph.sdk;

import java.util.Objects;
import javax.annotation.Nonnegative;

// TODO: fromString
// TODO: toSolidityAddress
// TODO: fromSolidityAddress

abstract class EntityId {
    /** The shard number */
    @Nonnegative public final long shard;

    /** The realm number */
    @Nonnegative public final long realm;

    /** The account number */
    @Nonnegative public final long num;

    EntityId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num) {
        this.shard = shard;
        this.realm = realm;
        this.num = num;
    }

    @Override
    public String toString() {
        return "" + shard + "." + realm + "." + num;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EntityId)) return false;

        EntityId otherId = (EntityId) o;
        return shard == otherId.shard && realm == otherId.realm && num == otherId.num;
    }

    @Override
    public int hashCode() {
        return Objects.hash(shard, realm, num);
    }
}
