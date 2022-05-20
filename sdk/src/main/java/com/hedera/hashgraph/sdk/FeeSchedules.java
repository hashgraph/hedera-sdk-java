package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.CurrentAndNextFeeSchedule;

import javax.annotation.Nullable;

/**
 * This contains two Fee Schedules with expiry timestamp.
 * {@link https://docs.hedera.com/guides/docs/hedera-api/basic-types/currentandnextfeeschedule}
 */
public class FeeSchedules {
    @Nullable
    private FeeSchedule current;
    @Nullable
    private FeeSchedule next;

    /**
     * Constructor.
     */
    public FeeSchedules() {
        current = next = null;
    }

    /**
     * Create a fee schedules object from a protobuf.
     *
     * @param feeSchedules              the protobuf
     * @return                          the fee schedules object
     */
    static FeeSchedules fromProtobuf(CurrentAndNextFeeSchedule feeSchedules) {
        return new FeeSchedules()
            .setCurrent(feeSchedules.hasCurrentFeeSchedule() ? FeeSchedule.fromProtobuf(feeSchedules.getCurrentFeeSchedule()) : null)
            .setNext(feeSchedules.hasNextFeeSchedule() ? FeeSchedule.fromProtobuf(feeSchedules.getNextFeeSchedule()) : null);
    }

    /**
     * Create a fee schedules object from a byte array.
     *
     * @param bytes                     the byte array
     * @return                          the fee schedules object
     * @throws InvalidProtocolBufferException
     */
    public static FeeSchedules fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(CurrentAndNextFeeSchedule.parseFrom(bytes).toBuilder().build());
    }

    /**
     * @return                          the current fee schedule
     */
    @Nullable
    public FeeSchedule getCurrent() {
        return current;
    }

    /**
     * Assign the current fee schedule.
     *
     * @param current                   the fee schedule
     * @return {@code this}
     */
    public FeeSchedules setCurrent(@Nullable FeeSchedule current) {
        this.current = current;
        return this;
    }

    /**
     * @return                          the next fee schedule
     */
    @Nullable
    public FeeSchedule getNext() {
        return next;
    }

    /**
     * Assign the next fee schedule.
     *
     * @param next                      the fee schedule
     * @return {@code this}
     */
    public FeeSchedules setNext(@Nullable FeeSchedule next) {
        this.next = next;
        return this;
    }

    /**
     * @return                          protobuf representation
     */
    CurrentAndNextFeeSchedule toProtobuf() {
        var returnBuilder = CurrentAndNextFeeSchedule.newBuilder();
        if (current != null) {
            returnBuilder.setCurrentFeeSchedule(current.toProtobuf());
        }
        if (next != null) {
            returnBuilder.setNextFeeSchedule(next.toProtobuf());
        }
        return returnBuilder.build();
    }

    /**
     * @return                          byte array representation
     */
    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("current", getCurrent())
            .add("next", getNext())
            .toString();
    }
}
