package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.account.CryptoTransferTransaction;
import com.hedera.hashgraph.proto.AccountAmount;
import com.hedera.hashgraph.proto.Query;
import com.hedera.hashgraph.proto.QueryHeader;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.proto.ResponseCodeEnum;
import com.hedera.hashgraph.proto.ResponseHeader;
import com.hedera.hashgraph.proto.ResponseType;
import com.hedera.hashgraph.proto.TransactionBody;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import io.grpc.Channel;
import io.grpc.MethodDescriptor;

public abstract class QueryBuilder<Resp, T extends QueryBuilder<Resp, T>> extends HederaCall<Query, Response, Resp, T> {
    protected final Query.Builder inner = Query.newBuilder();

    @Nullable
    protected final Client client;

    @Nullable
    private AccountId nodeId;

    private long paymentAmount;
    private long maxPayment = 0;

    protected QueryBuilder(@Nullable Client client) {
        this.client = client;
    }

    protected QueryBuilder() {
        this.client = null;
    }

    protected abstract QueryHeader.Builder getHeaderBuilder();

    @Override
    protected Channel getChannel() {
        return getNode(requireClient()).getChannel();
    }

    @Override
    protected Channel getChannel(Client client) {
        return getNode(client).getChannel();
    }

    protected Client requireClient() {
        return Objects.requireNonNull(client,
            "QueryBuilder.client must be non-null in regular use");
    }

    private Node getNode(Client client) {
        if (nodeId == null && getHeaderBuilder().hasPayment()) {
            TransactionBody paymentBody;

            try {
                paymentBody = TransactionBody.parseFrom(getHeaderBuilder().getPayment().getBodyBytes());
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException("payment transaction was not properly encoded", e);
            }

            List<AccountAmount> transfers = paymentBody.getCryptoTransfer()
                .getTransfers().getAccountAmountsList();

            //
            for (AccountAmount transfer : transfers) {
                if (transfer.getAmount() > 0) {
                    nodeId = new AccountId(transfer.getAccountID());
                    break;
                }
            }
        }

        if (nodeId != null) {
            return client.getNodeForId(nodeId);
        } else {
            Node node = client.pickNode();
            nodeId = node.accountId;
            return node;
        }
    }

    private long getMaxPayment(Client client) {
        if (maxPayment > 0) return maxPayment;
        return client.getMaxQueryPayment();
    }

    @Override
    public final Query toProto() {
        localValidate();
        return inner.build();
    }

    /**
     * Explicitly set a payment for this query.
     *
     * The payment must only be a single payer and a single payee.
     *
     * @param transaction
     * @return
     */
    public T setPayment(Transaction transaction) {
        getHeaderBuilder().setPayment(transaction.toProto());
        // noinspection unchecked
        return (T) this;
    }

    /**
     * Explicitly specify that the operator account is paying for the query and set payment
     * with the given amount.
     * <p>
     * Only takes effect if payment is required, has not been set yet, and an operator ID
     * was provided to the {@link Client} used to construct this instance.
     *
     * @return {@code this} for fluent usage.
     * @deprecated use {@link #setPaymentAmount(long)} instead.
     */
    @Deprecated
    public T setPaymentDefault(long paymentAmount) {
        setPaymentAmount(paymentAmount);
        if (client != null) generatePayment(client);
        //noinspection unchecked
        return (T) this;
    }

    /**
     * Explicitly specify that the operator account is paying for the query; when the query is
     * executed a payment transaction will be constructed with a transfer of this amount
     * from the operator account to the node which will handle the query.
     *
     * @return {@code this} for fluent usage.
     */
    public T setPaymentAmount(long paymentAmount) {
        this.paymentAmount = paymentAmount;

        //noinspection unchecked
        return (T) this;
    }

    public T setMaxQueryPayment(long maxPayment) {
        this.maxPayment = maxPayment;

        //noinspection unchecked
        return (T) this;
    }

    public final long getCost(Client client) throws HederaException, HederaNetworkException {
        // set which node we're going to be working with
        return new CostQuery(client).execute(client);
    }

    public final void getCostAsync(Client client, Consumer<Long> withCost, Consumer<HederaThrowable> onError) {
        new CostQuery(client).executeAsync(client, withCost, onError);
    }

    /**
     * @deprecated renamed to {@link #getCost(Client)}
     */
    public final long queryCost() throws HederaException, HederaNetworkException {
        getNode(requireClient());
        return new CostQuery(requireClient()).execute();
    }

    /**
     * @deprecated renamed to {@link #getCostAsync(Client, Consumer, Consumer)}
     */
    public final void queryCostAsync(Consumer<Long> withCost, Consumer<HederaThrowable> onError) {
        getNode(requireClient());
        new CostQuery(requireClient()).executeAsync(withCost, onError);
    }

    private void generatePayment(Client client) {
        if (isPaymentRequired() && !getHeaderBuilder().hasPayment()
            && client.getOperatorId() != null)
        {
            AccountId operatorId = client.getOperatorId();
            AccountId nodeId = getNode(client).accountId;
            Transaction txPayment = new CryptoTransferTransaction()
                .setNodeAccountId(nodeId)
                .setTransactionId(new TransactionId(operatorId))
                .addSender(operatorId, paymentAmount)
                .addRecipient(nodeId, paymentAmount)
                .build(client);

            setPayment(txPayment);
        }
    }

    @Override
    public final Resp execute(Client client, Duration timeout) throws HederaException, HederaNetworkException {
        final long maxQueryPayment = client.getMaxQueryPayment();

        if (!getHeaderBuilder().hasPayment() && isPaymentRequired() && maxQueryPayment > 0) {
            if (paymentAmount == 0) {
                final long cost = getCost(client);
                if (cost > maxQueryPayment) {
                    throw new MaxPaymentExceededException(this, cost, maxQueryPayment);
                }

                this.paymentAmount = cost;
            }

            generatePayment(client);
        }

        return super.execute(client, timeout);
    }

    @Override
    public final void executeAsync(Client client, Duration timeout, Consumer<Resp> onSuccess, Consumer<HederaThrowable> onError) {
        final long maxQueryPayment = client.getMaxQueryPayment();

        if (!getHeaderBuilder().hasPayment() && isPaymentRequired() && maxQueryPayment > 0) {
            getCostAsync(client, cost -> {
                if (cost > maxQueryPayment) {
                    onError.accept(new MaxPaymentExceededException(this, cost, maxQueryPayment));
                    return;
                }
                paymentAmount = cost;

                super.executeAsync(client, timeout, onSuccess, onError);
            }, onError);
        } else {
            super.executeAsync(client, timeout, onSuccess, onError);
        }
    }

    @Deprecated
    @Override
    public Resp execute(Duration timeout) throws HederaException, HederaNetworkException {
        return execute(requireClient(), timeout);
    }

    @Deprecated
    @Override
    public void executeAsync(Duration timeout, Consumer<Resp> onSuccess, Consumer<HederaThrowable> onError) {
        executeAsync(requireClient(), timeout, onSuccess, onError);
    }

    protected abstract void doValidate();

    protected boolean isPaymentRequired() {
        return true;
    }

    @Override
    protected final void localValidate() {
        if (isPaymentRequired()) {
            require(getHeaderBuilder().hasPayment(), ".setPayment() required");
        }

        doValidate();

        checkValidationErrors("query builder failed local validation");
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
            case CONSENSUSGETTOPICINFO:
                return raw.getConsensusGetTopicInfo().getHeader();
            default:
                // NOTE: TRANSACTIONGETFASTRECORD shouldn't be handled as we don't expose that query
                throw new RuntimeException("Unhandled response case");
        }
    }

    @Override
    protected final Resp mapResponse(Response raw) throws HederaException {
        final ResponseCodeEnum precheckCode = getResponseHeader(raw).getNodeTransactionPrecheckCode();
        HederaException.throwIfExceptional(precheckCode);

        switch (raw.getResponseCase()) {
            case TRANSACTIONGETRECEIPT:
                HederaException.throwIfExceptional(raw.getTransactionGetReceipt().getReceipt().getStatus());
                break;
            case TRANSACTIONGETRECORD:
                HederaException.throwIfExceptional(raw.getTransactionGetRecord().getTransactionRecord().getReceipt().getStatus());
                break;
            default:
        }

        return fromResponse(raw);
    }

    protected abstract Resp fromResponse(Response raw);

    private final class CostQuery extends HederaCall<Query, Response, Long, CostQuery> {
        private final Client client;

        CostQuery(Client client) {
            this.client = client;
        }

        @Override
        protected MethodDescriptor<Query, Response> getMethod() {
            return QueryBuilder.this.getMethod();
        }

        @Override
        public Query toProto() {
            final QueryHeader.Builder header = getHeaderBuilder();

            final com.hedera.hashgraph.proto.Transaction origPayment = header.hasPayment() ? header.getPayment() : null;
            final ResponseType origResponseType = header.getResponseType();

            final AccountId operatorId = Objects.requireNonNull(
                client.getOperatorId(),
                "COST_ANSWER requires an operator ID to be set");

            // COST_ANSWER requires a payment to pass validation but doesn't actually process it
            final com.hedera.hashgraph.proto.Transaction fakePayment = new CryptoTransferTransaction()
                .addRecipient(Objects.requireNonNull(nodeId), 0)
                .addSender(operatorId, 0)
                .build(client)
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
        protected Channel getChannel(Client client) {
            return QueryBuilder.this.getChannel(client);
        }

        @Override
        protected Duration getDefaultTimeout() {
            return QueryBuilder.this.getDefaultTimeout();
        }

        @Override
        protected Long mapResponse(Response raw) throws HederaException {
            return getResponseHeader(raw).getCost();
        }

        @Override
        public void localValidate() {
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
