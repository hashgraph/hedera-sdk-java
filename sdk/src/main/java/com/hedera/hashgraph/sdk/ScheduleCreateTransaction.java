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
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Objects;

public final class ScheduleCreateTransaction extends Transaction<ScheduleCreateTransaction> {
    @Nullable
    private AccountId payerAccountId = null;
    @Nullable
    private SchedulableTransactionBody transactionToSchedule = null;
    @Nullable
    private Key adminKey = null;
    private String scheduleMemo = "";

    public ScheduleCreateTransaction() {
        defaultMaxTransactionFee = new Hbar(5);
    }

    ScheduleCreateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    @Nullable
    public AccountId getPayerAccountId() {
        return payerAccountId;
    }

    public ScheduleCreateTransaction setPayerAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId);
        requireNotFrozen();
        this.payerAccountId = accountId;
        return this;
    }

    public ScheduleCreateTransaction setScheduledTransaction(Transaction<?> transaction) {
        requireNotFrozen();
        Objects.requireNonNull(transaction);

        var scheduled = transaction.schedule();
        transactionToSchedule = scheduled.transactionToSchedule;

        return this;
    }

    ScheduleCreateTransaction setScheduledTransactionBody(SchedulableTransactionBody tx) {
        requireNotFrozen();
        Objects.requireNonNull(tx);
        transactionToSchedule = tx;
        return this;
    }

    @Nullable
    public Key getAdminKey() {
        return adminKey;
    }

    public ScheduleCreateTransaction setAdminKey(Key key) {
        requireNotFrozen();
        adminKey = key;
        return this;
    }

    public String getScheduleMemo() {
        return scheduleMemo;
    }

    public ScheduleCreateTransaction setScheduleMemo(String memo) {
        requireNotFrozen();
        scheduleMemo = memo;
        return this;
    }

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
        throw new UnsupportedOperationException("Cannot schedule ScheduleCreateTransaction");
    }
}
