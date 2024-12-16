// SPDX-License-Identifier: Apache-2.0
package com.hiero.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hiero.sdk.proto.SchedulableTransactionBody;
import com.hiero.sdk.proto.ScheduleDeleteTransactionBody;
import com.hiero.sdk.proto.ScheduleServiceGrpc;
import com.hiero.sdk.proto.TransactionBody;
import com.hiero.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import java.util.LinkedHashMap;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * Delete a scheduled transaction.
 * <p>
 * See <a href="https://docs.hedera.com/guides/docs/sdks/schedule-transaction/delete-a-schedule-transaction">Hedera Documentation</a>
 */
public final class ScheduleDeleteTransaction extends Transaction<ScheduleDeleteTransaction> {

    @Nullable
    private ScheduleId scheduleId = null;

    /**
     * Constructor.
     */
    public ScheduleDeleteTransaction() {
        defaultMaxTransactionFee = new Hbar(5);
    }

    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction)
     *            records
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    ScheduleDeleteTransaction(
            LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hiero.sdk.proto.Transaction>> txs)
            throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * Constructor.
     *
     * @param txBody protobuf TransactionBody
     */
    ScheduleDeleteTransaction(com.hiero.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    /**
     * Extract the schedule id.
     *
     * @return                          the schedule id
     */
    @Nullable
    public ScheduleId getScheduleId() {
        return scheduleId;
    }

    /**
     * Assign the scheduled id.
     *
     * @param scheduleId                the schedule id
     * @return {@code this}
     */
    public ScheduleDeleteTransaction setScheduleId(ScheduleId scheduleId) {
        Objects.requireNonNull(scheduleId);
        requireNotFrozen();
        this.scheduleId = scheduleId;
        return this;
    }

    /**
     * Initialize from the transaction body.
     */
    void initFromTransactionBody() {
        var body = sourceTransactionBody.getScheduleDelete();
        if (body.hasScheduleID()) {
            scheduleId = ScheduleId.fromProtobuf(body.getScheduleID());
        }
    }

    /**
     * Build the correct transaction body.
     *
     * @return {@link com.hiero.sdk.proto.ScheduleDeleteTransactionBody builder }
     */
    ScheduleDeleteTransactionBody.Builder build() {
        var builder = ScheduleDeleteTransactionBody.newBuilder();
        if (scheduleId != null) {
            builder.setScheduleID(scheduleId.toProtobuf());
        }

        return builder;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (scheduleId != null) {
            scheduleId.validateChecksum(client);
        }
    }

    @Override
    MethodDescriptor<com.hiero.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return ScheduleServiceGrpc.getDeleteScheduleMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setScheduleDelete(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setScheduleDelete(build());
    }
}
