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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The fees for a specific transaction or query based on the fee data.
 *
 * See <a href="https://docs.hedera.com/guides/docs/hedera-api/basic-types/transactionfeeschedule”>Hedera Documentation</a>
 */
public class TransactionFeeSchedule {
    private RequestType requestType;
    @Nullable
    private FeeData feeData;
    private List<FeeData> fees;

    /**
     * Constructor.
     */
    public TransactionFeeSchedule() {
        requestType = RequestType.NONE;
        feeData = null;
        fees = new ArrayList<>();
    }

    /**
     * Create a transaction fee schedule object from a protobuf.
     *
     * @param transactionFeeSchedule    the protobuf
     * @return                          the new transaction fee schedule
     */
    static TransactionFeeSchedule fromProtobuf(com.hedera.hashgraph.sdk.proto.TransactionFeeSchedule transactionFeeSchedule) {
        var returnFeeSchedule = new TransactionFeeSchedule()
            .setRequestType(RequestType.valueOf(transactionFeeSchedule.getHederaFunctionality()))
            .setFeeData(transactionFeeSchedule.hasFeeData() ? FeeData.fromProtobuf(transactionFeeSchedule.getFeeData()) : null);
        for (var feeData : transactionFeeSchedule.getFeesList()) {
            returnFeeSchedule.addFee(FeeData.fromProtobuf(feeData));
        }
        return returnFeeSchedule;
    }

    /**
     * Create a transaction fee schedule object from a byte array.
     *
     * @param bytes                     the byte array
     * @return                          the new transaction fee schedule
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    public static TransactionFeeSchedule fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.TransactionFeeSchedule.parseFrom(bytes).toBuilder().build());
    }

    /**
     * @return                          the request type
     */
    public RequestType getRequestType() {
        return requestType;
    }

    /**
     * Assign the request type.
     *
     * @param requestType               the request type
     * @return {@code this}
     */
    public TransactionFeeSchedule setRequestType(RequestType requestType) {
        this.requestType = requestType;
        return this;
    }

    @Deprecated
    @Nullable
    public FeeData getFeeData() {
        return feeData;
    }

    @Deprecated
    public TransactionFeeSchedule setFeeData(@Nullable FeeData feeData) {
        this.feeData = feeData;
        return this;
    }

    /**
     * @return                          the list of fee's
     */
    public List<FeeData> getFees() {
        return fees;
    }

    /**
     * Add a fee to the schedule.
     *
     * @param fee                       the fee to add
     * @return {@code this}
     */
    public TransactionFeeSchedule addFee(FeeData fee) {
        fees.add(Objects.requireNonNull(fee));
        return this;
    }

    /**
     * Build the transaction body.
     *
     * @return {@code {@link
     *         com.hedera.hashgraph.sdk.proto.TransactionFeeSchedule}}
     */
    com.hedera.hashgraph.sdk.proto.TransactionFeeSchedule toProtobuf() {
        var returnBuilder = com.hedera.hashgraph.sdk.proto.TransactionFeeSchedule.newBuilder()
            .setHederaFunctionality(getRequestType().code);
        if (feeData != null) {
            returnBuilder.setFeeData(feeData.toProtobuf());
        }
        for (var fee : fees) {
            returnBuilder.addFees(fee.toProtobuf());
        }
        return returnBuilder.build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("requestType", getRequestType())
            .add("feeData", getFeeData())
            .add("fees", getFees())
            .toString();
    }

    /**
     * @return                          the byte array representation
     */
    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
