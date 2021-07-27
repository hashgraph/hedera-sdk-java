package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.ScheduleID;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.util.Objects;

public final class ScheduleId {
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

    public ScheduleId(@Nonnegative long num) {
        this(0, 0, num);
    }

    @SuppressWarnings("InconsistentOverloads")
    public ScheduleId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num) {
        this.shard = shard;
        this.realm = realm;
        this.num = num;
        this.checksum = null;
    }

    @SuppressWarnings("InconsistentOverloads")
    ScheduleId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num, @Nullable NetworkName network, @Nullable String checksum) {
        this.shard = shard;
        this.realm = realm;
        this.num = num;

        if (network != null) {
            if (checksum == null) {
                this.checksum = EntityIdHelper.checksum(Integer.toString(network.id), shard + "." + realm + "." + num);
            } else {
                this.checksum = checksum;
            }
        } else {
            this.checksum = null;
        }
    }

    public static ScheduleId fromString(String id) {
        return EntityIdHelper.fromString(id, ScheduleId::new);
    }

    static ScheduleId fromProtobuf(ScheduleID scheduleId, @Nullable NetworkName networkName) {
        Objects.requireNonNull(scheduleId);
        return new ScheduleId(scheduleId.getShardNum(), scheduleId.getRealmNum(), scheduleId.getScheduleNum(), networkName, null);
    }

    static ScheduleId fromProtobuf(ScheduleID scheduleId) {
        return ScheduleId.fromProtobuf(scheduleId, null);
    }

    public static ScheduleId fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(ScheduleID.parseFrom(bytes).toBuilder().build());
    }

    ScheduleID toProtobuf() {
        return ScheduleID.newBuilder()
            .setShardNum(shard)
            .setRealmNum(realm)
            .setScheduleNum(num)
            .build();
    }

    @Deprecated
    public void validate(Client client) {
        validateChecksum(client);
    }

    public void validateChecksum(Client client) {
        EntityIdHelper.validate(shard, realm, num, client, checksum);
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
        return EntityIdHelper.toString(shard, realm, num);
    }

    public String toStringWithChecksum(Client client) {
        return EntityIdHelper.toStringWithChecksum(shard, realm, num, client, checksum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shard, realm, num);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScheduleId)) return false;

        ScheduleId otherId = (ScheduleId) o;
        return shard == otherId.shard && realm == otherId.realm && num == otherId.num;
    }
}
