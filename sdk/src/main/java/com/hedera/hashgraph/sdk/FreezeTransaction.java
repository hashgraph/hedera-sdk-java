package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.FreezeTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;

/**
 * Set the freezing period in which the platform will stop creating events and accepting transactions.
 * This is used before safely shut down the platform for maintenance.
 */
public final class FreezeTransaction extends TransactionBuilder<FreezeTransaction> {
    private final FreezeTransactionBody.Builder builder;

    public FreezeTransaction() {
        builder = FreezeTransactionBody.newBuilder();
    }

    /**
     * Sets the start time (in UTC).
     *
     * @return {@code this}
     */
    public FreezeTransaction setStartTime(int hour, int minute) {
        builder.setStartHour(hour);
        builder.setStartMin(minute);

        return this;
    }

    /**
     * Sets the end time (in UTC).
     *
     * @return {@code this}
     */
    public FreezeTransaction setEndTime(int hour, int minute) {
        builder.setEndHour(hour);
        builder.setEndMin(minute);

        return this;
    }

    @Override
    void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setFreeze(builder);
    }
}
