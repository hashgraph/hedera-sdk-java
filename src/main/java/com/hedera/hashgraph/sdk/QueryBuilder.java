package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.proto.AccountAmount;
import com.hedera.hashgraph.proto.Query;
import com.hedera.hashgraph.proto.QueryHeader;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.proto.ResponseCodeEnum;
import com.hedera.hashgraph.proto.ResponseHeader;
import com.hedera.hashgraph.proto.ResponseType;
import com.hedera.hashgraph.proto.TransactionBody;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.account.CryptoTransferTransaction;
import com.hedera.hashgraph.sdk.crypto.PublicKey;
import com.hedera.hashgraph.sdk.crypto.TransactionSigner;

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
    private AccountId nodeId;

    @Nullable
    private TransactionId paymentTransactionId;

    private long paymentAmount;
    private long maxPayment = 0;

    protected QueryBuilder() { }

    protected abstract QueryHeader.Builder getHeaderBuilder();

    @Override
    protected Channel getChannel(Client client) {
        return getNode(client).getChannel();
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

    public T setMaxQueryPayment(Hbar maxPayment) {
        this.maxPayment = maxPayment.asTinybar();

        //noinspection unchecked
        return (T) this;
    }

    public T setMaxQueryPayment(long maxPayment) {
        this.maxPayment = maxPayment;

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
    public T setQueryPayment(Hbar paymentAmount) {
        return setQueryPayment(paymentAmount.asTinybar());
    }

    /**
     * Explicitly specify that the operator account is paying for the query with an amount in
     * tinybar; when the query is executed a payment transaction will be constructed with a transfer
     * of this amount from the operator account to the node which will handle the query.
     *
     * @return {@code this} for fluent usage.
     */
    public T setQueryPayment(long paymentAmount) {
        this.paymentAmount = paymentAmount;

        //noinspection unchecked
        return (T) this;
    }

    /**
     * Explicitly set a payment for this query.
     *
     * The payment must only be a single payer and a single payee.
     *
     * @param transaction
     * @return {@code this} for fluent usage.
     */
    public T setPaymentTransaction(Transaction transaction) {
        getHeaderBuilder().setPayment(transaction.toProto());
        paymentTransactionId = transaction.id;
        // noinspection unchecked
        return (T) this;
    }

    public long getCost(Client client) throws HederaStatusException, HederaNetworkException {
        // set which node we're going to be working with
        return new CostQuery(client).execute(client);
    }

    public void getCostAsync(Client client, Consumer<Long> withCost, Consumer<HederaThrowable> onError) {
        new CostQuery(client).executeAsync(client, withCost, onError);
    }

    private void generatePayment(Client client) {
        if (isPaymentRequired() && !getHeaderBuilder().hasPayment()
            && client.getOperatorId() != null && client.getOperatorSigner() != null
            && client.getOperatorPublicKey() != null)
        {
            AccountId operatorId = client.getOperatorId();
            AccountId nodeId = getNode(client).accountId;
            Transaction txPayment = new CryptoTransferTransaction()
                .setNodeAccountId(nodeId)
                .setTransactionId(new TransactionId(operatorId))
                .addSender(operatorId, paymentAmount)
                .addRecipient(nodeId, paymentAmount)
                .build(client)
                .signWith(client.getOperatorPublicKey(), client.getOperatorSigner());

            setPaymentTransaction(txPayment);
        }
    }

    @Override
    public final Resp execute(Client client, Duration timeout) throws HederaStatusException, HederaNetworkException, LocalValidationException {
        final long maxQueryPayment = client.getMaxQueryPayment();

        if (!getHeaderBuilder().hasPayment() && isPaymentRequired() && maxQueryPayment > 0) {
            if (paymentAmount == 0) {
                final long cost = getCost(client);
                if (cost > maxQueryPayment) {
                    throw new MaxQueryPaymentExceededException(this, cost, maxQueryPayment);
                }

                this.paymentAmount = cost;
            }

            generatePayment(client);
        }

        return super.execute(client, timeout);
    }

    @Override
    public final void executeAsync(Client client, Duration timeout, Consumer<Resp> onSuccess, Consumer<HederaThrowable> onError) throws LocalValidationException {
        final long maxQueryPayment = client.getMaxQueryPayment();

        if (!getHeaderBuilder().hasPayment() && isPaymentRequired() && maxQueryPayment > 0) {
            getCostAsync(client, cost -> {
                if (cost > maxQueryPayment) {
                    onError.accept(new MaxQueryPaymentExceededException(this, cost, maxQueryPayment));
                    return;
                }
                paymentAmount = cost;

                super.executeAsync(client, timeout, onSuccess, onError);
            }, onError);
        } else {
            super.executeAsync(client, timeout, onSuccess, onError);
        }
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
    protected final Resp mapResponse(Response raw) throws HederaStatusException {
        if (paymentTransactionId != null) {
            // precheck code for transaction only matters if we have a payment attached
            final ResponseCodeEnum precheckCode = getResponseHeader(raw).getNodeTransactionPrecheckCode();
            HederaPrecheckStatusException.throwIfExceptional(precheckCode, paymentTransactionId);
        }

        switch (raw.getResponseCase()) {
            case TRANSACTIONGETRECEIPT:
                HederaReceiptStatusException.throwIfExceptional(
                    // look at the query for the transaction ID
                    inner.getTransactionGetReceipt(),
                    raw.getTransactionGetReceipt());
                break;
            case TRANSACTIONGETRECORD:
                // record response has everything we need
                HederaRecordStatusException.throwIfExceptional(raw.getTransactionGetRecord());
                break;
            default:
        }

        return extractResponse(raw);
    }

    protected abstract Resp extractResponse(Response raw);

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

            final PublicKey operatorPublicKey = Objects.requireNonNull(client.getOperatorPublicKey());

            final TransactionSigner operatorSigner = Objects.requireNonNull(client.getOperatorSigner());

            // COST_ANSWER requires a payment to pass validation but doesn't actually process it
            final com.hedera.hashgraph.proto.Transaction fakePayment = new CryptoTransferTransaction()
                .addRecipient(Objects.requireNonNull(nodeId), 0)
                .addSender(operatorId, 0)
                .build(client)
                .signWith(operatorPublicKey, operatorSigner)
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
        protected Channel getChannel(Client client) {
            return QueryBuilder.this.getChannel(client);
        }

        @Override
        protected Duration getDefaultTimeout() {
            return QueryBuilder.this.getDefaultTimeout();
        }

        @Override
        protected Long mapResponse(Response raw) throws HederaStatusException {
            return getResponseHeader(raw).getCost();
        }

        @Override
        public void localValidate() {
            // Skip validation for COST_ANSWER
        }
    }

}
