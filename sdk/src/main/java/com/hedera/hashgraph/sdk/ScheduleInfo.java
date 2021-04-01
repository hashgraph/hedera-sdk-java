package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.SignedTransaction;
import com.hedera.hashgraph.sdk.proto.TransactionList;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;

public final class ScheduleInfo {

    public final ScheduleId scheduleId;

    public final AccountId creatorAccountId;

    public final AccountId payerAccountId;

    public final SchedulableTransactionBody transactionBody;

    public final KeyList signatories;

    @Nullable
    public final Key adminKey;

    @Nullable
    public final TransactionId scheduledTransactionId;

    public final String memo;

    @Nullable
    public final Instant expirationTime;

    @Nullable
    public final Instant executed;

    @Nullable
    public final Instant deleted;

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
        @Nullable Instant deleted
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
        this.executed = executed;
        this.deleted = deleted;
    }

    static ScheduleInfo fromProtobuf(com.hedera.hashgraph.sdk.proto.ScheduleGetInfoResponse scheduleInfo) {
        var info = scheduleInfo.getScheduleInfo();

        var scheduleId = ScheduleId.fromProtobuf(info.getScheduleID());
        var creatorAccountId = AccountId.fromProtobuf(info.getCreatorAccountID());
        var payerAccountId = AccountId.fromProtobuf(info.getPayerAccountID());
        var adminKey = info.hasAdminKey() ? Key.fromProtobufKey(info.getAdminKey()) : null;
        var scheduledTransactionId = info.hasScheduledTransactionID() ?
            TransactionId.fromProtobuf(info.getScheduledTransactionID()) :
            null;
        SchedulableTransactionBody scheduledTransactionBody;

        try{
            scheduledTransactionBody = SchedulableTransactionBody.fromProtobuf(info.getScheduledTransactionBody());
        }  catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Failed to build Transaction from ScheduledTransactionBody inside ScheduleInfo ", e);
        }

        return new ScheduleInfo(
            scheduleId,
            creatorAccountId,
            payerAccountId,
            scheduledTransactionBody,
            KeyList.fromProtobuf(info.getSigners(), null),
            adminKey,
            scheduledTransactionId,
            info.getMemo(),
            info.hasExpirationTime() ? InstantConverter.fromProtobuf(info.getExpirationTime()) : null,
            info.hasExecutionTime() ? InstantConverter.fromProtobuf(info.getExecutionTime()) : null,
            info.hasDeletionTime() ? InstantConverter.fromProtobuf(info.getDeletionTime()) : null
        );
    }

    public static ScheduleInfo fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.ScheduleGetInfoResponse.parseFrom(bytes).toBuilder().build());
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

        if (executed != null) {
            info.setExecutionTime(InstantConverter.toProtobuf(executed));
        }

        if (deleted != null) {
            info.setDeletionTime(InstantConverter.toProtobuf(deleted));
        }

        return info
            .setScheduleID(scheduleId.toProtobuf())
            .setCreatorAccountID(creatorAccountId.toProtobuf())
            .setScheduledTransactionBody(transactionBody.toProtobuf())
            .setPayerAccountID(payerAccountId.toProtobuf())
            .setSigners(signatories.toProtobuf())
            .setMemo(memo)
            .build();
    }

    public final SchedulableTransactionBody getTransaction() {
            return transactionBody;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("scheduleId", scheduleId)
            .add("creatorAccountId", creatorAccountId)
            .add("payerAccountId", payerAccountId)
            .add("signatories", signatories)
            .add("adminKey", adminKey)
            .add("expirationTime", expirationTime)
            .add("memo", memo)
            .add("execution time", executed)
            .add("time deleted", deleted)
            .toString();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
