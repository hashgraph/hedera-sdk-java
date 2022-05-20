package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.ScheduleCreateTransactionBody;
import com.hedera.hashgraph.sdk.proto.ScheduleServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Schedule a create transaction.
 *
 * {@link https://docs.hedera.com/guides/docs/sdks/schedule-transaction/create-a-schedule-transaction}
 */
public final class ScheduleCreateTransaction extends Transaction<ScheduleCreateTransaction> {
    @Nullable
    private AccountId payerAccountId = null;
    @Nullable
    private SchedulableTransactionBody transactionToSchedule = null;
    @Nullable
    private Key adminKey = null;
    private String scheduleMemo = "";

    /**
     * Constructor.
     */
    public ScheduleCreateTransaction() {
        defaultMaxTransactionFee = new Hbar(5);
    }

    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction)
     *            records
     * @throws InvalidProtocolBufferException
     */
    ScheduleCreateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * @return                          the account id
     */
    @Nullable
    public AccountId getPayerAccountId() {
        return payerAccountId;
    }

    /**
     * Assign the payer's account id.
     *
     * @param accountId                 the payer's account id
     * @return {@code this}
     */
    public ScheduleCreateTransaction setPayerAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId);
        requireNotFrozen();
        this.payerAccountId = accountId;
        return this;
    }

    /**
     * Assign the transaction to schedule.
     *
     * @param transaction               the transaction to schedule
     * @return {@code this}
     */
    public ScheduleCreateTransaction setScheduledTransaction(Transaction<?> transaction) {
        requireNotFrozen();
        Objects.requireNonNull(transaction);

        var scheduled = transaction.schedule();
        transactionToSchedule = scheduled.transactionToSchedule;

        return this;
    }

    /**
     * Assign the transaction body to schedule.
     *
     * @param tx                        the transaction body to schedule
     * @return {@code this}
     */
    ScheduleCreateTransaction setScheduledTransactionBody(SchedulableTransactionBody tx) {
        requireNotFrozen();
        Objects.requireNonNull(tx);
        transactionToSchedule = tx;
        return this;
    }

    /**
     * @return                          the admin key
     */
    @Nullable
    public Key getAdminKey() {
        return adminKey;
    }

    /**
     * Assign the admin key.
     *
     * @param key                       the admin key
     * @return {@code this}
     */
    public ScheduleCreateTransaction setAdminKey(Key key) {
        requireNotFrozen();
        adminKey = key;
        return this;
    }

    /**
     * @return                          the schedule's memo
     */
    public String getScheduleMemo() {
        return scheduleMemo;
    }

    /**
     * Assign the schedule's memo.
     *
     * @param memo                      the schedule's memo
     * @return {@code this}
     */
    public ScheduleCreateTransaction setScheduleMemo(String memo) {
        requireNotFrozen();
        scheduleMemo = memo;
        return this;
    }

    /**
     * Build the correct transaction body.
     *
     * @return {@code {@link com.hedera.hashgraph.sdk.proto.ScheduleCreateTransactionBody builder }}
     */
    ScheduleCreateTransactionBody.Builder build() {
        var builder = ScheduleCreateTransactionBody.newBuilder();
        if (payerAccountId != null) {
            builder.setPayerAccountID(payerAccountId.toProtobuf());
        }
        if (transactionToSchedule != null) {
            builder.setScheduledTransactionBody(transactionToSchedule);
        }
        if (adminKey != null) {
            builder.setAdminKey(adminKey.toProtobufKey());
        }
        builder.setMemo(scheduleMemo);

        return builder;
    }

    /**
     * Initialize from the transaction body.
     */
    void initFromTransactionBody() {
        var body = sourceTransactionBody.getScheduleCreate();
        if (body.hasPayerAccountID()) {
            payerAccountId = AccountId.fromProtobuf(body.getPayerAccountID());
        }
        if (body.hasScheduledTransactionBody()) {
            transactionToSchedule = body.getScheduledTransactionBody();
        }
        if (body.hasAdminKey()) {
            adminKey = Key.fromProtobufKey(body.getAdminKey());
        }
        scheduleMemo = body.getMemo();
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (payerAccountId != null) {
            payerAccountId.validateChecksum(client);
        }
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return ScheduleServiceGrpc.getCreateScheduleMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setScheduleCreate(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        throw new IllegalStateException("Cannot schedule `ScheduleCreateTransaction`");
    }
}
