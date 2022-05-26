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
    private FileId fileId = null;
    private byte[] fileHash = {};
    private FreezeType freezeType = FreezeType.UNKNOWN_FREEZE_TYPE;

    /**
     * Constructor.
     */
    public FreezeTransaction() {
    }

    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction)
     *            records
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    FreezeTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * Constructor.
     *
     * @param txBody protobuf TransactionBody
     */
    FreezeTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    /**
     * Extract the start time.
     *
     * @return                          the start time
     */
    public Instant getStartTime() {
        return startTime != null ? startTime : Instant.EPOCH;
    }

    /**
     * Assign the start time.
     *
     * @param startTime                 the start time
     * @return {@code this}
     */
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

    /**
     * @deprecated with no replacement
     */
    @Deprecated
    public Instant getEndTime() {
        return Instant.from(OffsetTime.of(endHour, endMinute, 0, 0, ZoneOffset.UTC));
    }

    /**
     * Sets the end time (in UTC).
     *
     * @deprecated with no replacement
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

    /**
     * @deprecated Use {@link #getFileId()} instead.
     */
    @Deprecated
    @Nullable
    public FileId getUpdateFileId() {
        return fileId;
    }

    /**
     * @deprecated Use {@link #setFileId(FileId)} instead.
     */
    @Deprecated
    public FreezeTransaction setUpdateFileId(FileId updateFileId) {
        return setFileId(updateFileId);
    }

    /**
     * @deprecated Use {@link #getFileHash()} instead.
     */
    @Deprecated
    public byte[] getUpdateFileHash() {
        return fileHash;
    }

    /**
     * @deprecated Use {@link #setFileHash(byte[])} instead.
     */
    @Deprecated
    public FreezeTransaction setUpdateFileHash(byte[] updateFileHash) {
        return setFileHash(updateFileHash);
    }

    /**
     * Extract the file id.
     *
     * @return                          the file id
     */
    @Nullable
    public FileId getFileId() {
        return fileId;
    }

    /**
     * Assign the file id.
     *
      * @param fileId                    the file id
     * @return {@code this}
     */
    public FreezeTransaction setFileId(FileId fileId) {
        requireNotFrozen();
        Objects.requireNonNull(fileId);
        this.fileId = fileId;
        return this;
    }

    /**
     * Extract the file's hash.
     *
     * @return                          the file's hash
     */
    public byte[] getFileHash() {
        return fileHash;
    }

    public FreezeTransaction setFileHash(byte[] fileHash) {
        requireNotFrozen();
        Objects.requireNonNull(fileHash);
        this.fileHash = fileHash;
        return this;
    }

    /**
     * Extract the freeze type.
     *
     * @return                          the freeze type
     */
    public FreezeType getFreezeType() {
        return freezeType;
    }

    /**
     * Assign the freeze type.
     * {@link com.hedera.hashgraph.sdk.FreezeTransaction}
     *
     * @param freezeType                the freeze type
     * @return {@code this}
     */
    public FreezeTransaction setFreezeType(FreezeType freezeType) {
        requireNotFrozen();
        Objects.requireNonNull(freezeType);
        this.freezeType = freezeType;
        return this;
    }

    @Override
    void validateChecksums(Client client) {
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return FreezeServiceGrpc.getFreezeMethod();
    }

    /**
     * Initialize from the transaction body.
     */
    void initFromTransactionBody() {
        var body = sourceTransactionBody.getFreeze();
        freezeType = FreezeType.valueOf(body.getFreezeType());
        if (body.hasUpdateFile()) {
            fileId = FileId.fromProtobuf(body.getUpdateFile());
        }
        fileHash = body.getFileHash().toByteArray();
        if (body.hasStartTime()) {
            startTime = InstantConverter.fromProtobuf(body.getStartTime());
        }
    }

    /**
     * Build the correct transaction body.
     *
     * @return {@link com.hedera.hashgraph.sdk.proto.FreezeTransactionBody builder }
     */
    FreezeTransactionBody.Builder build() {
        var builder = FreezeTransactionBody.newBuilder();
        builder.setFreezeType(freezeType.code);
        if (fileId != null) {
            builder.setUpdateFile(fileId.toProtobuf());
        }
        builder.setFileHash(ByteString.copyFrom(fileHash));
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
