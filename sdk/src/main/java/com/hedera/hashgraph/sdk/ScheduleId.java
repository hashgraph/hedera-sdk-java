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
        this(shard, realm, num, null);
    }

    @SuppressWarnings("InconsistentOverloads")
    ScheduleId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num, @Nullable String checksum) {
        this.shard = shard;
        this.realm = realm;
        this.num = num;
        this.checksum = checksum;
    }

    public static ScheduleId fromString(String id) {
        return EntityIdHelper.fromString(id, ScheduleId::new);
    }

    static ScheduleId fromProtobuf(ScheduleID scheduleId) {
        Objects.requireNonNull(scheduleId);
        return new ScheduleId(scheduleId.getShardNum(), scheduleId.getRealmNum(), scheduleId.getScheduleNum());
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
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ScheduleId)) {
            return false;
        }

        ScheduleId otherId = (ScheduleId) o;
        return shard == otherId.shard && realm == otherId.realm && num == otherId.num;
    }
}
