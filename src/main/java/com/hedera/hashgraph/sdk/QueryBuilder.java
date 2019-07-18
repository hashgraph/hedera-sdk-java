package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.account.CryptoTransferTransaction;
import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseCodeEnum;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import com.hedera.hashgraph.sdk.proto.ResponseType;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

import javax.annotation.Nullable;

import io.grpc.Channel;
import io.grpc.MethodDescriptor;

public abstract class QueryBuilder<Resp, T extends QueryBuilder<Resp, T>> extends HederaCall<Query, Response, Resp, T> {
    protected final Query.Builder inner = Query.newBuilder();

    @Nullable
    private final Client client;

    @Nullable
    private Node pickedNode;

    private boolean autoPay = false;

    protected QueryBuilder(@Nullable Client client) {
        this.client = client;
    }

    protected abstract QueryHeader.Builder getHeaderBuilder();

    @Override
    protected Channel getChannel() {
        return getNode().getChannel();
    }

    private Node getNode() {
        Objects.requireNonNull(client, "QueryBuilder.client must be non-null in regular use");

        if (pickedNode == null) {
            pickedNode = client.pickNode();
        }

        return pickedNode;
    }

    @Override
    public final Query toProto() {
        getHeaderBuilder().setResponseType(ResponseType.ANSWER_ONLY);
        validate();
        return inner.build();
    }

    @SuppressWarnings("unchecked")
    public T setPayment(Transaction transaction) {
        getHeaderBuilder().setPayment(transaction.toProto());
        return (T) this;
    }

    @Override
    protected void preExecute() throws HederaException, HederaNetworkException {
        if (!autoPay || !isPaymentRequired()) return;

        final var cost = requestCost();
        // if cost is 0 we shouldn't have to set a payment but the network currently requires it
        addAutoPayment(cost);
    }

    @Override
    protected void preExecuteAsync(Runnable onSuccess, Consumer<HederaThrowable> onError) {
        if (!autoPay || !isPaymentRequired()) {
            onSuccess.run();
            return;
        }

        requestCostAsync(cost -> {
            addAutoPayment(cost);
            onSuccess.run();
        }, onError);
    }

    private void addAutoPayment(long cost) {
        if (client != null && isPaymentRequired() && !getHeaderBuilder().hasPayment() && client.getOperatorId() != null) {
            // FIXME: queries that say they have 0 cost are still requiring fee payments
            final var minCost = Math.max(cost, 100_000);

            var operatorId = client.getOperatorId();
            var nodeId = getNode().accountId;
            var txPayment = new CryptoTransferTransaction(client)
                .setNodeAccountId(nodeId)
                .setTransactionId(new TransactionId(operatorId))
                .addSender(operatorId, minCost)
                .addRecipient(nodeId, minCost)
                .build();

            setPayment(txPayment);
        }
    }

    /**
     * Explicitly specify that the operator account is paying for the query.
     * <p>
     * On execute, the query will first request its cost and then add a payment transaction
     * from the operator account with that amount.
     * <p>
     * Only takes effect if payment is required, has not been set yet, and an operator ID
     * was provided to the {@link Client} used to construct this instance.
     *
     * @return {@code this} for fluent usage.
     */
    @SuppressWarnings("unchecked")
    public T setPaymentDefault() {
        autoPay = true;
        return (T) this;
    }

    /**
     * Ask the network how much this query will cost to execute as configured.
     *
     * @return the query cost, in tinybar.
     * @throws HederaException
     * @throws HederaNetworkException
     */
    public long requestCost() throws HederaException, HederaNetworkException {
        return new QueryCostRequest().execute();
    }

    /**
     * Ask the network how much this query will cost to execute as configured.
     */
    public void requestCostAsync(LongConsumer withCost, Consumer<HederaThrowable> onError) {
        new QueryCostRequest().executeAsync(withCost::accept, onError);
    }

    protected abstract void doValidate();

    protected boolean isPaymentRequired() {
        return true;
    }

    /** Check that the query was built properly, throwing an exception on any errors. */
    @Override
    public final void validate() {
        if (isPaymentRequired()) {
            require(getHeaderBuilder().hasPayment(), ".setPayment() required");
        }

        doValidate();
        checkValidationErrors("query builder failed validation");
    }

    private ResponseHeader getResponseHeader(Response raw) {
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
        validatePrecheckCode(raw);
        return fromResponse(raw);
    }

    protected abstract Resp fromResponse(Response raw);

    private ResponseHeader validatePrecheckCode(Response raw) throws HederaException {
        final var header = getResponseHeader(raw);
        final ResponseCodeEnum precheckCode = header.getNodeTransactionPrecheckCode();

        final var responseCase = raw.getResponseCase();

        final var unknownIsExceptional = responseCase == Response.ResponseCase.TRANSACTIONGETRECEIPT
            || responseCase == Response.ResponseCase.TRANSACTIONGETRECORD;

        HederaException.throwIfExceptional(precheckCode, unknownIsExceptional);

        return header;
    }

    private class QueryCostRequest extends HederaCall<Query, Response, Long, QueryCostRequest> {

        @Override
        protected MethodDescriptor<Query, Response> getMethod() {
            return QueryBuilder.this.getMethod();
        }

        @Override
        public Query toProto() {
            validate();

            final var header = getHeaderBuilder();
            final var responseType = header.getResponseType();

            // set response type to COST before serializing
            header.setResponseType(ResponseType.COST_ANSWER);
            // copy the request so we can set the ResponseType back
            final var proto = inner.clone();
            header.setResponseType(responseType);

            return proto.build();
        }

        @Override
        protected Channel getChannel() {
            return QueryBuilder.this.getChannel();
        }

        @Override
        protected Long mapResponse(Response raw) throws HederaException {
            return validatePrecheckCode(raw).getCost();
        }

        @Override
        public void validate() {
            // ignore payment since we're just now checking what it costs
            QueryBuilder.this.doValidate();
            QueryBuilder.this.checkValidationErrors(
                "cannot get cost for incomplete query builder");
        }
    }
}
