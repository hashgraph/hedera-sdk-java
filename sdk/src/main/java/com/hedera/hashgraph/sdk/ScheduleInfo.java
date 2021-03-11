package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.SignedTransaction;
import com.hedera.hashgraph.sdk.proto.TransactionList;

import javax.annotation.Nullable;

public final class ScheduleInfo {

    public final ScheduleId scheduleId;

    public final AccountId creatorAccountId;

    public final AccountId payerAccountId;

    public final byte[] transactionBody;

    public final KeyList signatories;

    @Nullable
    public final Key adminKey;

    @Nullable
    public final TransactionId scheduledTransactionId;

    private ScheduleInfo(
        ScheduleId scheduleId,
        AccountId creatorAccountId,
        AccountId payerAccountId,
        byte[] transactionBody,
        KeyList signers,
        @Nullable Key adminKey,
        @Nullable TransactionId scheduledTransactionId
    ) {
        this.scheduleId = scheduleId;
        this.creatorAccountId = creatorAccountId;
        this.payerAccountId = payerAccountId;
        this.signatories = signers;
        this.adminKey = adminKey;
        this.transactionBody = transactionBody;
        this.scheduledTransactionId = scheduledTransactionId;
    }

    static ScheduleInfo fromProtobuf(com.hedera.hashgraph.sdk.proto.ScheduleGetInfoResponse scheduleInfo) {
        com.hedera.hashgraph.sdk.proto.ScheduleInfo info = scheduleInfo.getScheduleInfo();

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
            info.getTransactionBody().toByteArray(),
            KeyList.fromProtobuf(info.getSignatories(), null),
            adminKey,
            scheduledTransactionId
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

        return info
            .setScheduleID(scheduleId.toProtobuf())
            .setCreatorAccountID(creatorAccountId.toProtobuf())
            .setTransactionBody(ByteString.copyFrom(transactionBody))
            .setPayerAccountID(payerAccountId.toProtobuf())
            .setSignatories(signatories.toProtobuf())
            .build();
    }

    public final Transaction<?> getTransaction() {
        try {
            return Transaction.fromBytes(TransactionList.newBuilder()
                .addTransactionList(com.hedera.hashgraph.sdk.proto.Transaction.newBuilder()
                    .setSignedTransactionBytes(SignedTransaction.newBuilder()
                        .setBodyBytes(ByteString.copyFrom(transactionBody))
                        .build()
                        .toByteString())
                    .build())
                .build()
                .toByteArray()
            );
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Failed to build transaction of `bodyBytes` inside `ScheduleInfo`", e);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("scheduleId", scheduleId)
            .add("creatorAccountId", creatorAccountId)
            .add("payerAccountId", payerAccountId)
            .add("signatories", signatories)
            .add("adminKey", adminKey)
            .toString();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
