package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.ScheduleCreateTransactionBody;
import com.hedera.hashgraph.sdk.proto.SignedTransaction;
import com.hedera.hashgraph.sdk.proto.SignedTransactionOrBuilder;
import com.hedera.hashgraph.sdk.proto.TransactionList;

public final class ScheduleInfo {

    public final ScheduleId scheduleId;

    public final AccountId creatorAccountId;

    public final AccountId payerAccountId;

    public final byte[] transactionBody;

    public final KeyList signatories;

    public final Key adminKey;

    private ScheduleInfo(
        ScheduleId scheduleId,
        AccountId creatorAccountId,
        AccountId payerAccountId,
        byte[] transactionBody,
        KeyList signers,
        Key adminKey
    ) {
        this.scheduleId = scheduleId;
        this.creatorAccountId = creatorAccountId;
        this.payerAccountId = payerAccountId;
        this.signatories = signers;
        this.adminKey = adminKey;
        this.transactionBody = transactionBody;
    }

    static ScheduleInfo fromProtobuf(com.hedera.hashgraph.sdk.proto.ScheduleGetInfoResponse scheduleInfo) {
        com.hedera.hashgraph.sdk.proto.ScheduleInfo info = scheduleInfo.getScheduleInfo();


        var scheduleId = ScheduleId.fromProtobuf(info.getScheduleID());

        var creatorAccountId = AccountId.fromProtobuf(info.getCreatorAccountID());

        var payerAccountId = AccountId.fromProtobuf(info.getPayerAccountID());

        return new ScheduleInfo(
            scheduleId,
            creatorAccountId,
            payerAccountId,
            info.getTransactionBody().toByteArray(),
            KeyList.fromProtobuf(info.getSignatories(), null),
            Key.fromProtobufKey(info.getAdminKey())
        );
    }

    public static ScheduleInfo fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.ScheduleGetInfoResponse.parseFrom(bytes).toBuilder().build());
    }

    com.hedera.hashgraph.sdk.proto.ScheduleInfo toProtobuf() {
        var adminKey = this.adminKey.toProtobufKey() != null
            ? this.adminKey.toProtobufKey()
            : null;

        var signers = this.signatories.toProtobuf() != null
            ? this.signatories.toProtobuf()
            : null;

        var scheduleInfoBuilder = com.hedera.hashgraph.sdk.proto.ScheduleInfo.newBuilder()
            .setScheduleID(scheduleId.toProtobuf())
            .setCreatorAccountID(creatorAccountId.toProtobuf())
            .setTransactionBody(ByteString.copyFrom(transactionBody))
            .setPayerAccountID(payerAccountId.toProtobuf())
            .setSignatories(signers)
            .setAdminKey(adminKey);

        return scheduleInfoBuilder.build();
    }

    public final Transaction<?> getTransaction() {
        SignedTransaction.Builder builder = SignedTransaction.newBuilder();
        builder.setBodyBytes(ByteString.copyFrom(transactionBody));

        try {
            return Transaction.fromBytes(TransactionList.newBuilder()
                .addTransactionList(com.hedera.hashgraph.sdk.proto.Transaction.newBuilder()
                    .setSignedTransactionBytes(builder.build().toByteString())
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
            .add("signers", signatories)
            .add("adminKey", adminKey)
            .toString();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
