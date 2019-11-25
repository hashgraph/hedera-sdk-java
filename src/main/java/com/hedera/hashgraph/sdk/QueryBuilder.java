package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.account.CryptoTransferTransaction;
import com.hederahashgraph.api.proto.java.Query;
import com.hederahashgraph.api.proto.java.QueryHeader;
import com.hederahashgraph.api.proto.java.Response;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;
import com.hederahashgraph.api.proto.java.ResponseHeader;
import com.hederahashgraph.api.proto.java.ResponseType;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import io.grpc.Channel;
import io.grpc.MethodDescriptor;

public abstract class QueryBuilder<Resp, T extends QueryBuilder<Resp, T>> extends HederaCall<Query, Response, Resp, T> {
    protected final Query.Builder inner = Query.newBuilder();

    @Nullable
    protected final Client client;

    @Nullable
    private Node pickedNode;

    @Nullable
    private Duration retryTimeout;

    protected QueryBuilder(@Nullable Client client) {
        this.client = client;
    }

    protected abstract QueryHeader.Builder getHeaderBuilder();

    @Override
    protected Channel getChannel() {
        return getNode().getChannel();
    }

    protected Client requireClient() {
        return Objects.requireNonNull(client,
            "QueryBuilder.client must be non-null in regular use");
    }

    private Node getNode() {

        if (pickedNode == null) {
            pickedNode = requireClient().pickNode();
        }

        return pickedNode;
    }

    @Override
    public final Query toProto() {
        setPaymentDefault();
        validate();
        return inner.build();
    }

    @SuppressWarnings("unchecked")
    public T setPayment(Transaction transaction) {
        getHeaderBuilder().setPayment(transaction.toProto());
        return (T) this;
    }

    /**
     * Explicitly specify that the operator account is paying for the query and set payment.
     * <p>
     * Only takes effect if payment is required, has not been set yet, and an operator ID
     * was provided to the {@link Client} used to construct this instance.
     *
     * @return {@code this} for fluent usage.
     * @deprecated query cost should be calculated by requesting it from the node so this function's
     * signature is insufficient to abstract over that operation.
     */
    @Deprecated
    public T setPaymentDefault() {
        return setPaymentDefault(100_000);
    }

    /**
     * Explicitly specify that the operator account is paying for the query and set payment
     * with the given amount.
     * <p>
     * Only takes effect if payment is required, has not been set yet, and an operator ID
     * was provided to the {@link Client} used to construct this instance.
     *
     * @return {@code this} for fluent usage.
     */
    public T setPaymentDefault(long paymentAmount) {
        if (client != null && isPaymentRequired() && !getHeaderBuilder().hasPayment()
            && client.getOperatorId() != null)
        {
            AccountId operatorId = client.getOperatorId();
            AccountId nodeId = getNode().accountId;
            Transaction txPayment = new CryptoTransferTransaction(client)
                .setNodeAccountId(nodeId)
                .setTransactionId(new TransactionId(operatorId))
                .addSender(operatorId, paymentAmount)
                .addRecipient(nodeId, paymentAmount)
                .build();

            setPayment(txPayment);
        }

        //noinspection unchecked
        return (T) this;
    }

    /**
     * If set, {@link #execute()} and {@link #executeAsync(Consumer, Consumer)} (both versions) will
     * retry on recoverable {@link HederaException}s until the timeout has elapsed.
     *
     * @param retryTimeout the maximum time to attempt executing this query for.
     * @return {@code this} for fluent usage.
     */
    public final T setRetryTimeout(Duration retryTimeout) {
        this.retryTimeout = retryTimeout;

        //noinspection unchecked
        return (T) this;
    }

    @Override
    protected Optional<Instant> getRetryUntil() {
        return Optional.ofNullable(retryTimeout).map(timeout -> Instant.now().plus(timeout));
    }

    public final long requestCost() throws HederaException, HederaNetworkException {
        return new CostQuery().execute();
    }

    public final void requestCostAsync(Consumer<Long> withCost, Consumer<HederaThrowable> onError) {
        new CostQuery().executeAsync(withCost, onError);
    }

    @Override
    protected void onPreExecute() throws HederaException, HederaNetworkException {
        final long maxQueryPayment = requireClient().getMaxQueryPayment();

        if (!getHeaderBuilder().hasPayment() && isPaymentRequired() && maxQueryPayment > 0) {
            final long cost = requestCost();
            if (cost > maxQueryPayment) {
                throw new MaxPaymentExceededException(this, cost, maxQueryPayment);
            }

            setPaymentDefault(requestCost());
        }
    }

    @Override
    protected void onPreExecuteAsync(Runnable onSuccess, Consumer<HederaThrowable> onError) {
        final long maxQueryPayment = requireClient().getMaxQueryPayment();

        if (!getHeaderBuilder().hasPayment() && isPaymentRequired() && maxQueryPayment > 0) {
            requestCostAsync(cost -> {
                if (cost > maxQueryPayment) {
                    onError.accept(new MaxPaymentExceededException(this, cost, maxQueryPayment));
                    return;
                }

                setPaymentDefault(cost);
                onSuccess.run();
            }, onError);
        } else {
            onSuccess.run();
        }
    }

    protected abstract void doValidate();

    protected boolean isPaymentRequired() {
        return true;
    }

    /**
     * Check that the query was built properly, throwing an exception on any errors.
     */
    @Override
    public final void validate() {
        if (isPaymentRequired()) {
            require(getHeaderBuilder().hasPayment(), ".setPayment() required");
        }

        doValidate();

        checkValidationErrors("query builder failed validation");
    }

    private static ResponseHeader getResponseHeader(Response raw) {
        switch (raw.getResponseCase()) {
            case GETBYKEY:
                return raw.getGetByKey().getHeader();
            case GETBYSOLIDITYID:
                return raw.getGetBySolidityID().getHeader();
            case CONTRACTCALLLOCAL:
                return raw.getContractCallLocal().getHeader();
            case CONTRACTGETBYTECODERESPONSE:
                return raw.getContractGetBytecodeResponse().getHeader();
            case CONTRACTGETINFO:
                return raw.getContractGetInfo().getHeader();
            case CONTRACTGETRECORDSRESPONSE:
                return raw.getContractGetRecordsResponse().getHeader();
            case CRYPTOGETACCOUNTBALANCE:
                return raw.getCryptogetAccountBalance().getHeader();
            case CRYPTOGETACCOUNTRECORDS:
                return raw.getCryptoGetAccountRecords().getHeader();
            case CRYPTOGETINFO:
                return raw.getCryptoGetInfo().getHeader();
            case CRYPTOGETCLAIM:
                return raw.getCryptoGetClaim().getHeader();
            case CRYPTOGETPROXYSTAKERS:
                return raw.getCryptoGetProxyStakers().getHeader();
            case FILEGETCONTENTS:
                return raw.getFileGetContents().getHeader();
            case FILEGETINFO:
                return raw.getFileGetInfo().getHeader();
            case TRANSACTIONGETRECEIPT:
                return raw.getTransactionGetReceipt().getHeader();
            case TRANSACTIONGETRECORD:
                return raw.getTransactionGetRecord().getHeader();
            case RESPONSE_NOT_SET:
                throw new IllegalStateException("Response not set");
            default:
                // NOTE: TRANSACTIONGETFASTRECORD shouldn't be handled as we don't expose that query
                throw new RuntimeException("Unhandled response case");
        }
    }

    @Override
    protected final Resp mapResponse(Response raw) throws HederaException {
        final ResponseCodeEnum precheckCode = getResponseHeader(raw).getNodeTransactionPrecheckCode();
        final Response.ResponseCase responseCase = raw.getResponseCase();
        boolean unknownIsExceptional = false;

        switch (responseCase) {
            case TRANSACTIONGETRECEIPT:
            case TRANSACTIONGETRECORD:
                break;
            default:
                unknownIsExceptional = true;
        }

        HederaException.throwIfExceptional(precheckCode, unknownIsExceptional);
        return fromResponse(raw);
    }

    protected abstract Resp fromResponse(Response raw);

    private final class CostQuery extends HederaCall<Query, Response, Long, CostQuery> {

        @Override
        protected MethodDescriptor<Query, Response> getMethod() {
            return QueryBuilder.this.getMethod();
        }

        @Override
        public Query toProto() {
            final QueryHeader.Builder header = getHeaderBuilder();

            final com.hederahashgraph.api.proto.java.Transaction origPayment = header.hasPayment() ? header.getPayment() : null;
            final ResponseType origResponseType = header.getResponseType();

            final AccountId nodeAccountId = getNode().accountId;
            final AccountId operatorId = Objects.requireNonNull(
                requireClient().getOperatorId(),
                "COST_ANSWER requires an operator ID to be set");

            // COST_ANSWER requires a payment to pass validation but doesn't actually process it
            final com.hederahashgraph.api.proto.java.Transaction fakePayment = new CryptoTransferTransaction(client)
                .addRecipient(nodeAccountId, 0)
                .addSender(operatorId, 0)
                .build()
                .toProto();

            // set our fake values, build and then reset
            header.setPayment(fakePayment);
            header.setResponseType(ResponseType.COST_ANSWER);

            final Query built = inner.build();

            if (origPayment != null) {
                header.setPayment(origPayment);
            } else {
                header.clearPayment();
            }
            header.setResponseType(origResponseType);

            return built;
        }

        @Override
        protected Channel getChannel() {
            return QueryBuilder.this.getChannel();
        }

        @Override
        protected Long mapResponse(Response raw) throws HederaException {
            return getResponseHeader(raw).getCost();
        }

        @Override
        protected Optional<Instant> getRetryUntil() {
            return QueryBuilder.this.getRetryUntil();
        }

        @Override
        public void validate() {
            // skip payment validation
            doValidate();
            QueryBuilder.this.checkValidationErrors("cannot get cost for incomplete query");
        }
    }

    public static final class MaxPaymentExceededException extends RuntimeException implements HederaThrowable {
        private MaxPaymentExceededException(QueryBuilder builder, long cost, long maxQueryPayment) {
            super(String.format(
                "cost of %s (%d) without explicit payment is greater than "
                    + "Client.maxQueryPayment (%d)",
                builder.getClass().getSimpleName(),
                cost,
                maxQueryPayment));
        }
    }
}
