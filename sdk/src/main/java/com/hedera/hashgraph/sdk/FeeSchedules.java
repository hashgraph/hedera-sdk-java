package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.CurrentAndNextFeeSchedule;
import javax.annotation.Nullable;
import com.google.common.base.MoreObjects;

public class FeeSchedules {
    @Nullable
    private FeeSchedule current;
    @Nullable
    private FeeSchedule next;

    public FeeSchedules() {
        current = next = null;
    }

    static FeeSchedules fromProtobuf(CurrentAndNextFeeSchedule feeSchedules) {
        return new FeeSchedules()
            .setCurrent(feeSchedules.hasCurrentFeeSchedule() ? FeeSchedule.fromProtobuf(feeSchedules.getCurrentFeeSchedule()) : null)
            .setNext(feeSchedules.hasNextFeeSchedule() ? FeeSchedule.fromProtobuf(feeSchedules.getNextFeeSchedule()) : null);
    }

    public static FeeSchedules fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(CurrentAndNextFeeSchedule.parseFrom(bytes).toBuilder().build());
    }

    @Nullable
    public FeeSchedule getCurrent() {
        return current;
    }

    public FeeSchedules setCurrent(@Nullable FeeSchedule current) {
        this.current = current;
        return this;
    }

    @Nullable
    public FeeSchedule getNext() {
        return next;
    }

    public FeeSchedules setNext(@Nullable FeeSchedule next) {
        this.next = next;
        return this;
    }

    CurrentAndNextFeeSchedule toProtobuf() {
        var returnBuilder = CurrentAndNextFeeSchedule.newBuilder();
        if(current != null) {
            returnBuilder.setCurrentFeeSchedule(current.toProtobuf());
        }
        if(next != null) {
            returnBuilder.setNextFeeSchedule(next.toProtobuf());
        }
        return returnBuilder.build();
    }

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
