package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.CurrentAndNextFeeSchedule;

import javax.annotation.Nullable;

public class FeeSchedules {
    
    @Nullable
    private FeeSchedule current, next;

    public FeeSchedules() {
        current = next = null;
    }

    boolean hasCurrent() {
        return current != null;
    }
    boolean hasNext() {
        return next != null;
    }
    @Nullable
    FeeSchedule getCurrent() {
        assert hasCurrent();
        return current;
    }
    @Nullable
    FeeSchedule getNext() {
        assert hasNext();
        return next;
    }
    FeeSchedules setCurrent(@Nullable FeeSchedule current) {
        this.current = current;
        return this;
    }
    FeeSchedules setNext(@Nullable FeeSchedule next) {
        this.next = next;
        return this;
    }


    static FeeSchedules fromProtobuf(CurrentAndNextFeeSchedule feeSchedules) {
        return new FeeSchedules()
            .setCurrent(feeSchedules.hasCurrentFeeSchedule() ? FeeSchedule.fromProtobuf(feeSchedules.getCurrentFeeSchedule()) : null)
            .setNext(feeSchedules.hasNextFeeSchedule() ? FeeSchedule.fromProtobuf(feeSchedules.getNextFeeSchedule()) : null);
    }
    public static FeeSchedules fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(CurrentAndNextFeeSchedule.parseFrom(bytes).toBuilder().build());
    }



    CurrentAndNextFeeSchedule toProtobuf()
    {
        var builder = CurrentAndNextFeeSchedule.newBuilder();
        if(hasCurrent()) {
            builder.setCurrentFeeSchedule(getCurrent().toProtobuf());
        }
        if(hasNext()) {
            builder.setNextFeeSchedule(getNext().toProtobuf());
        }
        return builder.build();
    }
    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}