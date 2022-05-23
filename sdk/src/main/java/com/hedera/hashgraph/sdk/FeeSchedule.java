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

import com.google.common.base.MoreObjects;
import com.google.protobuf.InvalidProtocolBufferException;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The fee schedule for a specific hedera functionality and the time period this fee schedule will expire.
 *
 * See <a href="https://docs.hedera.com/guides/docs/hedera-api/basic-types/feeschedule”>Hedera Documentation</a>
 */
public class FeeSchedule {
    private List<TransactionFeeSchedule> transactionFeeSchedules = new ArrayList<>();
    @Nullable
    private Instant expirationTime;

    /**
     * Constructor.
     */
    public FeeSchedule() {
    }

    /**
     * Create a fee schedule from a protobuf.
     *
     * @param feeSchedule               the protobuf
     * @return                          the fee schedule
     */
    static FeeSchedule fromProtobuf(com.hedera.hashgraph.sdk.proto.FeeSchedule feeSchedule) {
        FeeSchedule returnFeeSchedule = new FeeSchedule()
            .setExpirationTime(feeSchedule.hasExpiryTime() ? InstantConverter.fromProtobuf(feeSchedule.getExpiryTime()) : null);
        for (var transactionFeeSchedule : feeSchedule.getTransactionFeeScheduleList()) {
            returnFeeSchedule
                .getTransactionFeeSchedules()
                .add(TransactionFeeSchedule.fromProtobuf(transactionFeeSchedule));
        }
        return returnFeeSchedule;
    }

    /**
     * Create a fee schedule from byte array.
     *
     * @param bytes                     the bye array
     * @return                          the fee schedule
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    public static FeeSchedule fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.FeeSchedule.parseFrom(bytes).toBuilder().build());
    }

    /**
     * @return                          list of transaction fee schedules
     */
    public List<TransactionFeeSchedule> getTransactionFeeSchedules() {
        return transactionFeeSchedules;
    }

    /**
     * Assign the list of transaction fee schedules.
     *
     * @param transactionFeeSchedules   list of transaction fee schedules
     * @return
     */
    public FeeSchedule setTransactionFeeSchedules(List<TransactionFeeSchedule> transactionFeeSchedules) {
        this.transactionFeeSchedules = Objects.requireNonNull(transactionFeeSchedules);
        return this;
    }

    /**
     * Add a transaction fee schedule.
     *
     * @param transactionFeeSchedule    transaction fee schedule to add
     * @return {@code this}
     */
    public FeeSchedule addTransactionFeeSchedule(TransactionFeeSchedule transactionFeeSchedule) {
        transactionFeeSchedules.add(Objects.requireNonNull(transactionFeeSchedule));
        return this;
    }

    /**
     * @return                          the expiration time
     */
    @Nullable
    public Instant getExpirationTime() {
        return expirationTime;
    }

    /**
     * Assign the expiration time.
     *
     * @param expirationTime            the expiration time
     * @return
     */
    public FeeSchedule setExpirationTime(@Nullable Instant expirationTime) {
        this.expirationTime = expirationTime;
        return this;
    }

    /**
     * Convert to a protobuf.
     *
     * @return                          the protobuf
     */
    com.hedera.hashgraph.sdk.proto.FeeSchedule toProtobuf() {
        var returnBuilder = com.hedera.hashgraph.sdk.proto.FeeSchedule.newBuilder();
        if (expirationTime != null) {
            returnBuilder.setExpiryTime(InstantConverter.toSecondsProtobuf(expirationTime));
        }
        for (TransactionFeeSchedule tFeeSchedule : getTransactionFeeSchedules()) {
            returnBuilder.addTransactionFeeSchedule(tFeeSchedule.toProtobuf());
        }
        return returnBuilder.build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("transactionFeeSchedules", getTransactionFeeSchedules())
            .add("expirationTime", getExpirationTime())
            .toString();
    }

    /**
     * @return                          a byte array representation
     */
    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
