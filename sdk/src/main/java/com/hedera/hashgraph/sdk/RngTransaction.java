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

import com.hedera.hashgraph.sdk.proto.RandomGenerateTransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;

/**
 * Random Number Generator Transaction.
 */
public class RngTransaction extends com.hedera.hashgraph.sdk.Transaction<RngTransaction> {

    /**
     * If provided and is positive, returns a 32-bit pseudorandom number from the given range in the transaction record.
     * If not set or set to zero, will return a 384-bit pseudorandom data in the record.
     */
    @Nullable
    private Integer range = null;

    /**
     * Constructor.
     */
    public RngTransaction() {
    }

    /**
     * Assign the range.
     *
     * @param range                     if > 0 32 bit else 384 bit
     * @return {@code this}
     */
    public RngTransaction setRange(Integer range) {
        this.range = range;
        return this;
    }

    /**
     * Retrieve the range.
     *
     * @return                          the range
     */
    public Integer getRange() {
        return range;
    }

    RandomGenerateTransactionBody.Builder build() {
        var builder = RandomGenerateTransactionBody.newBuilder();

        if (range != null) {
            builder.setRange(range);
        }

        return builder;
    }

    // TODO: Need to implement the following methods

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
//        bodyBuilder.setRandomGenerate(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        throw new UnsupportedOperationException("cannot schedule RngTransaction");
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
//        return TokenServiceGrpc.getr  .getRandomGenerateMethod();
//        return UtilService.getRandomGenerateMethod();
        return null;
    }



}
