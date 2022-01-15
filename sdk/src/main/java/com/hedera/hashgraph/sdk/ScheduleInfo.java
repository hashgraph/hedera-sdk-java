package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.ScheduleGetInfoResponse;

import java.time.Instant;
import javax.annotation.Nullable;

public final class ScheduleInfo {

    public final ScheduleId scheduleId;

    public final AccountId creatorAccountId;

    public final AccountId payerAccountId;
    public final KeyList signatories;
    @Nullable
    public final Key adminKey;
    @Nullable
    public final TransactionId scheduledTransactionId;
    public final String memo;
    @Nullable
    public final Instant expirationTime;
    @Nullable
    public final Instant executedAt;
    @Nullable
    public final Instant deletedAt;
    final SchedulableTransactionBody transactionBody;
    final LedgerId ledgerId;

    private ScheduleInfo(
        ScheduleId scheduleId,
        AccountId creatorAccountId,
        AccountId payerAccountId,
        SchedulableTransactionBody transactionBody,
        KeyList signers,
        @Nullable Key adminKey,
        @Nullable TransactionId scheduledTransactionId,
        String memo,
        @Nullable Instant expirationTime,
        @Nullable Instant executed,
        @Nullable Instant deleted,
        LedgerId ledgerId
    ) {
        this.scheduleId = scheduleId;
        this.creatorAccountId = creatorAccountId;
        this.payerAccountId = payerAccountId;
        this.signatories = signers;
        this.adminKey = adminKey;
        this.transactionBody = transactionBody;
        this.scheduledTransactionId = scheduledTransactionId;
        this.memo = memo;
        this.expirationTime = expirationTime;
        this.executedAt = executed;
        this.deletedAt = deleted;
        this.ledgerId = ledgerId;
    }

    static ScheduleInfo fromProtobuf(ScheduleGetInfoResponse scheduleInfo) {
        var info = scheduleInfo.getScheduleInfo();

        var scheduleId = ScheduleId.fromProtobuf(info.getScheduleID());
        var creatorAccountId = AccountId.fromProtobuf(info.getCreatorAccountID());
        var payerAccountId = AccountId.fromProtobuf(info.getPayerAccountID());
        var adminKey = info.hasAdminKey() ? Key.fromProtobufKey(info.getAdminKey()) : null;
        var scheduledTransactionId = info.hasScheduledTransactionID() ?
            TransactionId.fromProtobuf(info.getScheduledTransactionID()) :
            null;

        return new ScheduleInfo(
            scheduleId,
            creatorAccountId,
            payerAccountId,
            info.getScheduledTransactionBody(),
            KeyList.fromProtobuf(info.getSigners(), null),
            adminKey,
            scheduledTransactionId,
            info.getMemo(),
            info.hasExpirationTime() ? InstantConverter.fromProtobuf(info.getExpirationTime()) : null,
            info.hasExecutionTime() ? InstantConverter.fromProtobuf(info.getExecutionTime()) : null,
            info.hasDeletionTime() ? InstantConverter.fromProtobuf(info.getDeletionTime()) : null,
            LedgerId.fromByteString(info.getLedgerId())
        );
    }

    public static ScheduleInfo fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(ScheduleGetInfoResponse.parseFrom(bytes).toBuilder().build());
    }

    com.hedera.hashgraph.sdk.proto.ScheduleInfo toProtobuf() {
        var info = com.hedera.hashgraph.sdk.proto.ScheduleInfo.newBuilder();

        if (adminKey != null) {
            info.setAdminKey(adminKey.toProtobufKey());
        }

        if (scheduledTransactionId != null) {
            info.setScheduledTransactionID(scheduledTransactionId.toProtobuf());
        }

        if (expirationTime != null) {
            info.setExpirationTime(InstantConverter.toProtobuf(expirationTime));
        }

        if (executedAt != null) {
            info.setExecutionTime(InstantConverter.toProtobuf(executedAt));
        }

        if (deletedAt != null) {
            info.setDeletionTime(InstantConverter.toProtobuf(deletedAt));
        }

        return info
            .setScheduleID(scheduleId.toProtobuf())
            .setCreatorAccountID(creatorAccountId.toProtobuf())
            .setScheduledTransactionBody(transactionBody)
            .setPayerAccountID(payerAccountId.toProtobuf())
            .setSigners(signatories.toProtobuf())
            .setMemo(memo)
            .setLedgerId(ledgerId.toByteString())
            .build();
    }

    public Transaction<?> getScheduledTransaction() {
        return Transaction.fromScheduledTransaction(transactionBody);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("scheduleId", scheduleId)
            .add("scheduledTransactionId", scheduledTransactionId)
            .add("creatorAccountId", creatorAccountId)
            .add("payerAccountId", payerAccountId)
            .add("signatories", signatories)
            .add("adminKey", adminKey)
            .add("expirationTime", expirationTime)
            .add("memo", memo)
            .add("executedAt", executedAt)
            .add("deletedAt", deletedAt)
            .add("ledgerId", ledgerId)
            .toString();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
