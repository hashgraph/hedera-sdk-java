package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.FreezeServiceGrpc;
import com.hedera.hashgraph.sdk.proto.FreezeTransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import org.threeten.bp.Instant;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneOffset;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Set the freezing period in which the platform will stop creating events and accepting transactions.
 * This is used before safely shut down the platform for maintenance.
 */
public final class FreezeTransaction extends Transaction<FreezeTransaction> {
    private int endHour = 0;
    private int endMinute = 0;
    @Nullable
    private Instant startTime = null;
    @Nullable
    private FileId updateFileId = null;
    private byte[] updateFileHash = {};

    public FreezeTransaction() {
    }

    FreezeTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    FreezeTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    public Instant getStartTime() {
        return startTime != null ? startTime : Instant.EPOCH;
    }

    public FreezeTransaction setStartTime(Instant startTime) {
        requireNotFrozen();
        Objects.requireNonNull(startTime);
        this.startTime = startTime;
        return this;
    }

    /**
     * @deprecated Use {@link #setStartTime(Instant)} instead.
     */
    @Deprecated
    public FreezeTransaction setStartTime(int hour, int minute) {
        return setStartTime(Instant.from(OffsetTime.of(hour, minute, 0, 0, ZoneOffset.UTC)));
    }

    @Deprecated
    public Instant getEndTime() {
        return Instant.from(OffsetTime.of(endHour, endMinute, 0, 0, ZoneOffset.UTC));
    }

    /**
     * Sets the end time (in UTC).
     *
     * @param hour   The hour to be set
     * @param minute The minute to be set
     * @return {@code this}
     */
    @Deprecated
    public FreezeTransaction setEndTime(int hour, int minute) {
        requireNotFrozen();

        endHour = hour;
        endMinute = minute;

        return this;
    }

    @Nullable
    public FileId getUpdateFileId() {
        return updateFileId;
    }

    public FreezeTransaction setUpdateFileId(FileId updateFileId) {
        requireNotFrozen();
        Objects.requireNonNull(updateFileId);
        this.updateFileId = updateFileId;
        return this;
    }

    public byte[] getUpdateFileHash() {
        return updateFileHash;
    }

    public FreezeTransaction setUpdateFileHash(byte[] updateFileHash) {
        requireNotFrozen();
        Objects.requireNonNull(updateFileHash);
        this.updateFileHash = updateFileHash;
        return this;
    }

    @Override
    void validateChecksums(Client client) {
    }


    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return FreezeServiceGrpc.getFreezeMethod();
    }

    void initFromTransactionBody() {
        var body = sourceTransactionBody.getFreeze();
        endHour = body.getEndHour();
        endMinute = body.getEndMin();
        if (body.hasUpdateFile()) {
            updateFileId = FileId.fromProtobuf(body.getUpdateFile());
        }
        updateFileHash = body.getFileHash().toByteArray();
        if (body.hasStartTime()) {
            startTime = InstantConverter.fromProtobuf(body.getStartTime());
        }
    }

    FreezeTransactionBody.Builder build() {
        var builder = FreezeTransactionBody.newBuilder();
        builder.setEndHour(endHour);
        builder.setEndMin(endMinute);
        if (updateFileId != null) {
            builder.setUpdateFile(updateFileId.toProtobuf());
        }
        builder.setFileHash(ByteString.copyFrom(updateFileHash));
        if (startTime != null) {
            builder.setStartTime(InstantConverter.toProtobuf(startTime));
        }
        return builder;
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setFreeze(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setFreeze(build());
    }
}
