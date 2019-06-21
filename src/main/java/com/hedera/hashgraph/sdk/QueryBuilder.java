package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.account.CryptoTransferTransaction;
import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseCodeEnum;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import com.hedera.hashgraph.sdk.proto.ResponseType;

import java.util.Objects;

import javax.annotation.Nullable;

import io.grpc.Channel;
import io.grpc.MethodDescriptor;

public abstract class QueryBuilder<Resp, T extends QueryBuilder<Resp, T>> extends HederaCall<Query, Response, Resp, T> {
    protected final Query.Builder inner = Query.newBuilder();

    @Nullable
    private final Client client;

    @Nullable
    private Node pickedNode;

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
     */
    @SuppressWarnings("unchecked")
    public T setPaymentDefault() {
        if (client != null && isPaymentRequired() && !getHeaderBuilder().hasPayment() && client.getOperatorId() != null) {
            // FIXME: Require setAutoPayment to be set ?

            var cost = getCost();
            var operatorId = client.getOperatorId();
            var nodeId = getNode().accountId;
            var txPayment = new CryptoTransferTransaction(client)
                .setNodeAccountId(nodeId)
                .setTransactionId(new TransactionId(operatorId))
                .addSender(operatorId, cost)
                .addRecipient(nodeId, cost)
                .build();

            setPayment(txPayment);
        }

        return (T) this;
    }

    public long requestCost() throws HederaException, HederaNetworkException {
        return new QueryCostRequest().execute();
    }

    protected int getCost() {
        // FIXME: Currently query costs are fixed at 100,000 tinybar; this should change to
        //        an actual hedera request with the response type of COST (and cache this information on the client)
        return 100_000;
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
        final ResponseCodeEnum precheckCode = getResponseHeader(raw)
            .getNodeTransactionPrecheckCode();

        final var responseCase = raw.getResponseCase();

        final var unknownIsExceptional = responseCase == Response.ResponseCase.TRANSACTIONGETRECEIPT
            || responseCase == Response.ResponseCase.TRANSACTIONGETRECORD;

        HederaException.throwIfExceptional(precheckCode, unknownIsExceptional);
        return fromResponse(raw);
    }

    protected abstract Resp fromResponse(Response raw);

    private class QueryCostRequest extends HederaCall<Query, Response, Long> {

        @Override
        protected MethodDescriptor<Query, Response> getMethod() {
            return QueryBuilder.this.getMethod();
        }

        @Override
        public Query toProto() {
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
            return getResponseHeader(raw).getCost();
        }

        @Override
        public void validate() {
            // ignore payment since we're just now checking what it costs
            doValidate();
            checkValidationErrors("cannot get cost for incomplete query builder");
        }
    }
}
