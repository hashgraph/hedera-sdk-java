package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.Response;
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
