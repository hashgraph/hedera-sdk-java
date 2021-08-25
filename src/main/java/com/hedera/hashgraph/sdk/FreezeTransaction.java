package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.proto.FreezeServiceGrpc;
import com.hedera.hashgraph.proto.FreezeTransactionBody;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;

import java.time.Instant;
import java.time.OffsetTime;
import java.time.ZoneOffset;

import io.grpc.MethodDescriptor;

/**
 * Set the freezing period in which the platform will stop creating events and accepting
 * transactions.
 *
 * This is used before safely shutting down the platform for maintenance.
 * For use by platform administrators only.
 */
public class FreezeTransaction extends SingleTransactionBuilder<FreezeTransaction> {
    private FreezeTransactionBody.Builder builder = FreezeTransactionBody.newBuilder();

    public FreezeTransaction() {
        super();
        bodyBuilder.setFreeze(builder);
    }

    @Override
    protected void doValidate() {
        // sanity checks, the API should not allow this
        require(checkHour(builder.getStartHour()), "startHour not in range");
        require(checkMinute(builder.getStartMin()), "startMin not in range");
        require(checkHour(builder.getEndHour()), "endHour not in range");
        require(checkMinute(builder.getEndMin()), "endMin not in range");
    }

    private static boolean checkHour(int hour) {
        return hour >= 0 && hour < 24;
    }

    private static boolean checkMinute(int minute) {
        return minute >= 0 && minute < 60;
    }

    /**
     * Set the start time (hour and minute) of the freeze.
     *
     * The time will be converted to UTC if it isn't already. Take note that both start and end
     * times are assumed to fall on the same day in Coordinated Universal Time.
     *
     * @param startTime the start time of the freeze (down to the minute), before conversion to UTC.
     * @return {@code this} for fluent usage.
     */
    public FreezeTransaction setStartTime(OffsetTime startTime) {
        OffsetTime actual = startTime.withOffsetSameInstant(ZoneOffset.UTC);

        builder.setStartTime(TimestampHelper.timestampFrom(Instant.from(actual)));
        builder.setStartHour(actual.getHour());
        builder.setStartMin(actual.getMinute());
        return this;
    }

    /**
     * Set the end time (hour and minute) of the freeze.
     *
     * The time will be converted to UTC if it isn't already. Take note that both start and end
     * times are assumed to fall on the same day in Coordinated Universal Time.
     *
     * @param endTime the end time of the freeze (down to the minute), before conversion to UTC.
     * @return {@code this} for fluent usage.
     */
    public FreezeTransaction setEndTime(OffsetTime endTime) {
        OffsetTime actual = endTime.withOffsetSameInstant(ZoneOffset.UTC);

        builder.setEndHour(actual.getHour());
        builder.setEndMin(actual.getMinute());
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return FreezeServiceGrpc.getFreezeMethod();
    }
}
