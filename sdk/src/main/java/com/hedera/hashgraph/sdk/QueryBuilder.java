package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import com.hedera.hashgraph.sdk.proto.ResponseType;
import com.hedera.hashgraph.sdk.proto.Transaction;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import io.grpc.MethodDescriptor;
import java8.util.concurrent.CompletableFuture;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class QueryBuilder<O, T extends QueryBuilder<O, T>> extends HederaExecutable<Query, Response, O> implements WithGetCost {
    private final Query.Builder builder;

    private final QueryHeader.Builder headerBuilder;

    @Nullable
    private TransactionId paymentTransactionId;

    @Nullable
    private List<Transaction> paymentTransactions;

    @Nullable
    private List<AccountId> paymentTransactionNodeIds;

    private int nextPaymentTransactionIndex = 0;

    @Nullable
    private Hbar queryPayment;

    @Nullable
    private Hbar maxQueryPayment;

    QueryBuilder() {
        builder = Query.newBuilder();
        headerBuilder = QueryHeader.newBuilder();
    }

    public T setQueryPayment(Hbar queryPayment) {
        this.queryPayment = queryPayment;

        // noinspection unchecked
        return (T) this;
    }

    public T setMaxQueryPayment(Hbar maxQueryPayment) {
        this.maxQueryPayment = maxQueryPayment;

        // noinspection unchecked
        return (T) this;
    }

    @Override
    @FunctionalExecutable(type = "Hbar")
    public CompletableFuture<Hbar> getCostAsync(Client client) {
        return getCostExecutable().executeAsync(client);
    }

    boolean isPaymentRequired() {
        // nearly all queries require a payment
        return true;
    }

    /**
     * Called in {@link #makeRequest} just before the query is built. The intent is for the derived
     * class to assign their data variant to the query.
     */
    abstract void onMakeRequest(Query.Builder queryBuilder, QueryHeader header);

    /**
     * The derived class should access its response header and return.
     */
    abstract ResponseHeader mapResponseHeader(Response response);

    abstract QueryHeader mapRequestHeader(Query request);

    private Executable<Hbar> getCostExecutable() {
        return new QueryCostQuery();
    }

    @Override
    CompletableFuture<Void> onExecuteAsync(Client client) {
        if ((paymentTransactions != null) || !isPaymentRequired()) {
            return CompletableFuture.completedFuture(null);
        }

        // Generate payment transactions if one was
        // not set and payment is required

        var operator = client.getOperator();

        if (operator == null) {
            throw new IllegalStateException(
                "`client` must have an `operator` or an explicit payment transaction must be provided");
        }

        return CompletableFuture.supplyAsync(() -> {
            if (queryPayment == null) {
                // No payment was specified so we need to go ask
                // This is a query in its own right so we use a nested future here

                return getCostAsync(client).thenCompose(cost -> {
                    // Check if this is below our configured maximum query payment
                    var maxCost = MoreObjects.firstNonNull(maxQueryPayment, client.maxQueryPayment);

                    if (cost.compareTo(maxCost) > 0) {
                        return CompletableFuture.failedFuture(new MaxQueryPaymentExceededException(
                            this,
                            cost,
                            maxCost
                        ));
                    }

                    return CompletableFuture.completedFuture(cost);
                });
            }

            return CompletableFuture.completedFuture(queryPayment);
        }, client.executor)
            .thenCompose(x -> x)
            .thenAccept((paymentAmount) -> {
                paymentTransactionId = TransactionId.generate(operator.accountId);

                // Like how TransactionBuilder has to build (N / 3) native transactions to handle multi-node retry,
                // so too does the QueryBuilder for payment transactions

                var size = client.getNumberOfNodesForSuperMajority();
                paymentTransactions = new ArrayList<>(size);
                paymentTransactionNodeIds = new ArrayList<>(size);

                for (var i = 0; i < size; ++i) {
                    var nodeId = client.getNextNodeId();

                    paymentTransactionNodeIds.add(nodeId);
                    paymentTransactions.add(new CryptoTransferTransaction()
                        .setTransactionId(paymentTransactionId)
                        .setNodeAccountId(nodeId)
                        .setMaxTransactionFee(new Hbar(1)) // 1 Hbar
                        .addSender(operator.accountId, paymentAmount)
                        .addRecipient(nodeId, paymentAmount)
                        .build(null)
                        .signWith(operator.publicKey, operator.transactionSigner)
                        .makeRequest()
                    );
                }
            });
    }

    @Override
    final Query makeRequest() {
        // If payment is required, set the next payment transaction on the query
        if (isPaymentRequired() && paymentTransactions != null) {
            headerBuilder.setPayment(paymentTransactions.get(nextPaymentTransactionIndex));

            // each time we move our cursor to the next transaction
            // wrapping around to ensure we are cycling
            nextPaymentTransactionIndex = (nextPaymentTransactionIndex + 1) % paymentTransactions.size();
        }

        // Delegate to the derived class to apply the header because the common header struct is
        // within the nested type
        onMakeRequest(builder, headerBuilder.setResponseType(ResponseType.ANSWER_ONLY).build());

        return builder.build();
    }

    @Override
    final Status mapResponseStatus(Response response) {
        var preCheckCode = mapResponseHeader(response).getNodeTransactionPrecheckCode();

        return Status.valueOf(preCheckCode);
    }

    @Override
    final AccountId getNodeId(Client client) {
        if (paymentTransactionNodeIds != null) {
            // If this query needs a payment transaction we need to pick the node ID from the next
            // payment transaction
            return paymentTransactionNodeIds.get(nextPaymentTransactionIndex);
        }

        // Otherwise just pick the next node in the round robin
        return client.getNextNodeId();
    }

    @Override
    TransactionId getTransactionId() {
        // this is only called on an error about either the payment transaction or missing a payment transaction
        // as we make sure the latter can't happen, this will never be null
        return Objects.requireNonNull(paymentTransactionId);
    }

    @Override
    @SuppressWarnings("LiteProtoToString")
    String debugToString(Query request) {
        StringBuilder builder = new StringBuilder(request.toString());

        var queryHeader = mapRequestHeader(request);
        if (queryHeader.hasPayment()) {
            builder.append("\n");

            try {
                builder.append(TransactionBody.parseFrom(queryHeader.getPayment().getBodyBytes()).toString());
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }
        }

        return builder.toString();
    }

    @SuppressWarnings("NullableDereference")
    private class QueryCostQuery extends QueryBuilder<Hbar, QueryCostQuery> {
        @Override
        void onMakeRequest(Query.Builder queryBuilder, QueryHeader header) {
            headerBuilder.setResponseType(ResponseType.COST_ANSWER);

            // COST_ANSWER requires a payment to pass validation but doesn't actually process it
            // yes, this transaction is completely invalid
            // that is okay
            // now go back to sleep
            // without this, an error of MISSING_QUERY_HEADER is returned
            headerBuilder.setPayment(new CryptoTransferTransaction()
                .setNodeAccountId(new AccountId(0))
                .setTransactionId(new TransactionId(new AccountId(0), Instant.ofEpochSecond(0)))
                .build(null)
                .makeRequest());

            QueryBuilder.this.onMakeRequest(queryBuilder, headerBuilder.build());
        }

        @Override
        ResponseHeader mapResponseHeader(Response response) {
            return QueryBuilder.this.mapResponseHeader(response);
        }

        @Override
        QueryHeader mapRequestHeader(Query request) {
            return QueryBuilder.this.mapRequestHeader(request);
        }

        @Override
        Hbar mapResponse(Response response) {
            return Hbar.fromTinybar(mapResponseHeader(response).getCost());
        }

        @Override
        MethodDescriptor<Query, Response> getMethodDescriptor() {
            return QueryBuilder.this.getMethodDescriptor();
        }

        @Override
        boolean isPaymentRequired() {
            // combo breaker
            return false;
        }
    }
}
