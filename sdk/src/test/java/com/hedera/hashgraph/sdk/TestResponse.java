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

import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.TransactionGetReceiptResponse;
import com.hedera.hashgraph.sdk.proto.TransactionReceipt;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.StatusRuntimeException;

import javax.annotation.Nullable;

public class TestResponse {
    @Nullable
    public final TransactionResponse transactionResponse;
    @Nullable
    public final Response queryResponse;
    @Nullable
    public final StatusRuntimeException errorResponse;

    private TestResponse(
        @Nullable TransactionResponse transactionResponse,
        @Nullable Response queryResponse,
        @Nullable StatusRuntimeException errorResponse
    ) {
        this.transactionResponse = transactionResponse;
        this.queryResponse = queryResponse;
        this.errorResponse = errorResponse;
    }

    public static TestResponse transaction(Status status, Hbar cost) {
        return new TestResponse(buildTransactionResponse(status, cost), null, null);
    }

    public static TestResponse transaction(Status status) {
        return transaction(status, new Hbar(1));
    }

    public static TestResponse transactionOk(Hbar cost) {
        return transaction(Status.OK, cost);
    }

    public static TestResponse transactionOk() {
        return transactionOk(new Hbar(1));
    }

    public static TestResponse query(Response queryResponse) {
        return new TestResponse(null, queryResponse, null);
    }

    public static TestResponse receipt(Status status) {
        var response = Response.newBuilder().setTransactionGetReceipt(
            TransactionGetReceiptResponse.newBuilder().setReceipt(
                TransactionReceipt.newBuilder().setStatus(status.code).build()
            ).build()
        ).build();
        return new TestResponse(null, response, null);
    }

    public static TestResponse successfulReceipt() {
        return receipt(Status.SUCCESS);
    }

    public static TestResponse error(StatusRuntimeException exception) {
        return new TestResponse(null, null, exception);
    }

    public static TransactionResponse buildTransactionResponse(Status status, Hbar cost) {
        return TransactionResponse.newBuilder()
            .setNodeTransactionPrecheckCode(status.code)
            .setCost(cost.toTinybars())
            .build();
    }
}
