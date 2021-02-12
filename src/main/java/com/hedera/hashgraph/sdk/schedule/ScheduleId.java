package com.hedera.hashgraph.sdk.schedule;

import com.hedera.hashgraph.proto.ScheduleID;
import com.hedera.hashgraph.proto.ScheduleIDOrBuilder;
import com.hedera.hashgraph.sdk.IdUtil;
import com.hedera.hashgraph.sdk.Internal;

import java.util.Objects;

public final class ScheduleId {
    public final long shard;
    public final long realm;
    public final long schedule;

    /** Constructs an `ScheduleId` with `0` for `shard` and `realm` (e.g., `0.0.<scheduleNum>`). */
    public ScheduleId(long scheduleNum) {
        this(0, 0, scheduleNum);
    }

    public ScheduleId(long shard, long realm, long schedule) {
        this.shard = shard;
        this.realm = realm;
        this.schedule = schedule;
    }

    /** Constructs an `ScheduleId` from a string formatted as <shardNum>.<realmNum>.<scheduleNum> */
    public static ScheduleId fromString(String schedule) throws IllegalArgumentException {
        return IdUtil.parseIdString(schedule, ScheduleId::new);
    }

    public ScheduleId(ScheduleIDOrBuilder scheduleId) {
        this(scheduleId.getShardNum(), scheduleId.getRealmNum(), scheduleId.getScheduleNum());
    }

    @Override
    public String toString() {
        return "" + shard + "." + realm + "." + schedule;
    }

    @Internal
    public ScheduleID toProto() {
        return ScheduleID.newBuilder()
            .setShardNum(shard)
            .setRealmNum(realm)
            .setScheduleNum(schedule)
            .build();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;

        if (other == null || getClass() != other.getClass()) return false;

        ScheduleId otherId = (ScheduleId) other;
        return otherId.schedule == schedule
            && otherId.realm == realm
            && otherId.shard == shard;
    }

    @Override
    public int hashCode() {
        return Objects.hash(schedule, realm, shard);
    }
}
