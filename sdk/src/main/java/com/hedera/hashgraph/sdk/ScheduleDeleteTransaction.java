package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.ScheduleDeleteTransactionBody;
import com.hedera.hashgraph.sdk.proto.ScheduleServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Schedule a delete` transaction.
 *
 * {@link https://docs.hedera.com/guides/docs/sdks/schedule-transaction/delete-a-schedule-transaction}
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
     * @throws InvalidProtocolBufferException
     */
    ScheduleDeleteTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * Constructor.
     *
     * @param txBody protobuf TransactionBody
     */
    ScheduleDeleteTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    /**
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
     * @return {@cod this}
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
     * @return {@code {@link com.hedera.hashgraph.sdk.proto.ScheduleDeleteTransactionBody builder }}
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
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
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
