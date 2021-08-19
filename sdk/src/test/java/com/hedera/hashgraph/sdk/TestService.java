package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.Transaction;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.stub.StreamObserver;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public abstract class TestService {
    private final List<Transaction> transactionRequestsReceived = new ArrayList<>();
    private final Queue<TransactionResponse> transactionResponsesToSend = new LinkedList<>();

    private final List<Transaction> queryRequestsReceived = new ArrayList<>();
    private final Queue<Response> queryResponsesToSend = new LinkedList<>();

    public List<Transaction> getTransactionRequestsReceived() {
        return transactionRequestsReceived;
    }

    public List<Transaction> getQueryRequestsReceived() {
        return queryRequestsReceived;
    }

    // transaction responses

    protected void respondToTransactionFromQueue(StreamObserver<TransactionResponse> observer) {
        observer.onNext(transactionResponsesToSend.remove());
        observer.onCompleted();
    }

    private static TransactionResponse buildTransactionResponse(Status status, Hbar cost) {
        return TransactionResponse.newBuilder()
            .setNodeTransactionPrecheckCode(status.code)
            .setCost(cost.toTinybars())
            .build();
    }

    protected void respondToTransaction(StreamObserver<TransactionResponse> observer, Status status, Hbar cost) {
        observer.onNext(buildTransactionResponse(status, cost));
    }

    protected void respondToTransaction(StreamObserver<TransactionResponse> observer, Status status) {
        respondToTransaction(observer, status, new Hbar(1));
    }

    protected void respondOkToTransaction(StreamObserver<TransactionResponse> observer, Hbar cost) {
        respondToTransaction(observer, Status.OK, cost);
    }

    protected void respondOkToTransaction(StreamObserver<TransactionResponse> observer) {
        respondOkToTransaction(observer, new Hbar(1));
    }

    public void enqueueTransactionResponse(Status status, Hbar cost) {
        transactionResponsesToSend.add(buildTransactionResponse(status, cost));
    }

    public void enqueueTransactionResponse(Status status) {
        enqueueTransactionResponse(status, new Hbar(1));
    }

    public void enqueueTransactionOkResponse(Hbar cost) {
        enqueueTransactionResponse(Status.OK, cost);
    }

    public void enqueueTransactionOkResponse() {
        enqueueTransactionOkResponse(new Hbar(1));
    }

    // query responses

    protected void respondToQuery(StreamObserver<Response> observer, Response response) {
        observer.onNext(response);
        observer.onCompleted();
    }

    protected void respondToQueryFromQueue(StreamObserver<Response> observer) {
        observer.onNext(queryResponsesToSend.remove());
        observer.onCompleted();
    }

    public void enqueueQueryResponse(Response response) {
        queryResponsesToSend.add(response);
    }
}
