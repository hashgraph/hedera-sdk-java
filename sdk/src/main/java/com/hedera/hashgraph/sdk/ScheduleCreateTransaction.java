/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.ScheduleCreateTransactionBody;
import com.hedera.hashgraph.sdk.proto.ScheduleServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.grpc.MethodDescriptor;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Create a scheduled transaction.
 * <p>
 * See <a href="https://docs.hedera.com/guides/docs/sdks/schedule-transaction/create-a-schedule-transaction">Hedera Documentation</a>
 */
public final class ScheduleCreateTransaction extends Transaction<ScheduleCreateTransaction> {
    @Nullable
    private AccountId payerAccountId;
    @Nullable
    private SchedulableTransactionBody transactionToSchedule;
    @Nullable
    private Key adminKey;
    private String scheduleMemo = "";

    @Nullable
    private Instant expirationTime;

    private boolean waitForExpiry;

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
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    ScheduleCreateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * Get the expiration time
     *
     * @return The expiration time
     */
    @Nullable
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "An Instant can't actually be mutated"
    )
    public Instant getExpirationTime() {
        return expirationTime;
    }

    /**
     * An optional timestamp for specifying when the transaction should be evaluated for execution and then expire.
     * Defaults to 30 minutes after the transaction's consensus timestamp.
     * <p>
     * Note: This field is unused and forced to be unset until Long Term Scheduled Transactions are enabled - Transactions will always
     *       expire in 30 minutes if Long Term Scheduled Transactions are not enabled.
     *
     * @param expirationTime The expiration time
     * @return {@code this}
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "An Instant can't actually be mutated"
    )
    public ScheduleCreateTransaction setExpirationTime(Instant expirationTime) {
        this.expirationTime = expirationTime;
        return this;
    }

    /**
     * get the status of the waitForExpiry boolean
     *
     * @return waitForExpiry boolean
     */
    public boolean isWaitForExpiry() {
        return waitForExpiry;
    }

    /**
     * When set to true, the transaction will be evaluated for execution at expiration_time instead
     * of when all required signatures are received.
     * When set to false, the transaction will execute immediately after sufficient signatures are received
     * to sign the contained transaction. During the initial ScheduleCreate transaction or via ScheduleSign transactions.
     * Defaults to false.
     * <p>
     * Setting this to false does not necessarily mean that the transaction will never execute at expiration_time.
     *  <p>
     *  For Example - If the signature requirements for a Scheduled Transaction change via external means (e.g. CryptoUpdate)
     *  such that the Scheduled Transaction would be allowed to execute, it will do so autonomously at expiration_time, unless a
     *  ScheduleSign comes in to “poke” it and force it to go through immediately.
     * <p>
     * Note: This field is unused and forced to be unset until Long Term Scheduled Transactions are enabled. Before Long Term
     *       Scheduled Transactions are enabled, Scheduled Transactions will _never_ execute at expiration  - they will _only_
     *       execute during the initial ScheduleCreate transaction or via ScheduleSign transactions and will _always_
     *       expire at expiration_time.
     *
     * @param waitForExpiry Whether to wait for expiry
     * @return {@code this}
     */
    public ScheduleCreateTransaction setWaitForExpiry(boolean waitForExpiry) {
        this.waitForExpiry = waitForExpiry;
        return this;
    }

    /**
     * Get the payer's account ID.
     *
     * @return The payer's account ID
     */
    @Nullable
    public AccountId getPayerAccountId() {
        return payerAccountId;
    }

    /**
     * Assign the payer's account ID.
     *
     * @param accountId                 the payer's account ID
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
     * Extract the admin key.
     *
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
     * Extract the schedule's memo.
     *
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
     * @return {@link com.hedera.hashgraph.sdk.proto.ScheduleCreateTransactionBody builder }
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
        if (expirationTime != null) {
            builder.setExpirationTime(InstantConverter.toProtobuf(expirationTime));
        }
        builder.setMemo(scheduleMemo).setWaitForExpiry(waitForExpiry);
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
        if (body.hasExpirationTime()) {
            expirationTime = InstantConverter.fromProtobuf(body.getExpirationTime());
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
        throw new UnsupportedOperationException("Cannot schedule ScheduleCreateTransaction");
    }
}
