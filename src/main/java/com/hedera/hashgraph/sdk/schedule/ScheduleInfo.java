package com.hedera.hashgraph.sdk.schedule;

import com.google.common.base.MoreObjects;
import com.google.protobuf.ByteString;
import com.hedera.hashgraph.proto.Key;
import com.hedera.hashgraph.proto.ScheduleGetInfoResponse;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.KeyList;
import com.hedera.hashgraph.sdk.crypto.PublicKey;

import javax.annotation.Nullable;
import java.time.Instant;

public class ScheduleInfo {
    /**
     * The ID of the Scheduled Entity
     */
    public final ScheduleId scheduleId;

    /**
     * The Schedule ID which created the Scheduled TX
     */
    public final AccountId creatorAccountId;

    /**
     * The account which is going to pay for the execution of the Scheduled TX
     */
    @Nullable
    public final AccountId payerAccountId;

    /**
     * The transaction serialized into bytes that must be signed
     */
    public final ByteString bodyBytes;

    /**
     * The signatories that have provided signatures so far for the Scheduled TX
     */
    public final KeyList signatories;

    /**
     * The Key which is able to delete the Scheduled Transaction if set
     */
    @Nullable
    public final PublicKey adminKey;

    /**
     * Publicly visible information about the Scheduled entity, up to 100 bytes. No guarantee of uniqueness.
     */
    public final String memo;

    /**
     * The epoch second at which the schedule will expire
     */
    public final Instant expirationTime;

    ScheduleInfo(com.hedera.hashgraph.proto.ScheduleInfo info) {
        scheduleId = new ScheduleId(info.getScheduleID());
        creatorAccountId = new AccountId(info.getCreatorAccountID());
        payerAccountId = new AccountId(info.getPayerAccountID());
        bodyBytes = info.getTransactionBody();
        signatories = info.hasSignatories() ? (KeyList) PublicKey.fromProtoKey(Key.newBuilder().setKeyList(info.getSignatories())) : null;
        adminKey = info.hasAdminKey() ? PublicKey.fromProtoKey(info.getAdminKey()) : null;
        memo = info.getMemo();
        expirationTime = info.hasExpirationTime() ? TimestampHelper.timestampTo(info.getExpirationTime()) : null;
    }

    static ScheduleInfo fromResponse(Response response) {
        if (!response.hasScheduleGetInfo()) {
            throw new IllegalArgumentException("query response was not `ScheduleGetInfoResponse`");
        }

        ScheduleGetInfoResponse infoResponse = response.getScheduleGetInfo();

        return new ScheduleInfo(infoResponse.getScheduleInfo());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("scheduleId", scheduleId)
            .add("creatorAccountId", creatorAccountId)
            .add("payerAccountId", payerAccountId)
            .add("bodyBytes", bodyBytes)
            .add("signatories", signatories)
            .add("adminKey", adminKey)
            .add("memo", memo)
            .add("expirationTime", expirationTime)
            .toString();
    }
}
