package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.account.CryptoTransferTransaction;
import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseCodeEnum;

import java.util.Objects;

import javax.annotation.Nullable;

import io.grpc.Channel;

public abstract class QueryBuilder<Resp, T extends QueryBuilder<Resp, T>> extends HederaCall<Query, Response, Resp> {
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

    @Override
    protected final Resp mapResponse(Response raw) throws HederaException {
        final ResponseCodeEnum precheckCode;
        var unknownIsExceptional = true;
        switch (raw.getResponseCase()) {
        case GETBYKEY:
            precheckCode = raw.getGetByKey()
                .getHeader()
                .getNodeTransactionPrecheckCode();
            break;
        case GETBYSOLIDITYID:
            precheckCode = raw.getGetBySolidityID()
                .getHeader()
                .getNodeTransactionPrecheckCode();
            break;
        case CONTRACTCALLLOCAL:
            precheckCode = raw.getContractCallLocal()
                .getHeader()
                .getNodeTransactionPrecheckCode();
            break;
        case CONTRACTGETBYTECODERESPONSE:
            precheckCode = raw.getContractGetBytecodeResponse()
                .getHeader()
                .getNodeTransactionPrecheckCode();
            break;
        case CONTRACTGETINFO:
            precheckCode = raw.getContractGetInfo()
                .getHeader()
                .getNodeTransactionPrecheckCode();
            break;
        case CONTRACTGETRECORDSRESPONSE:
            precheckCode = raw.getContractGetRecordsResponse()
                .getHeader()
                .getNodeTransactionPrecheckCode();
            break;
        case CRYPTOGETACCOUNTBALANCE:
            precheckCode = raw.getCryptogetAccountBalance()
                .getHeader()
                .getNodeTransactionPrecheckCode();
            break;
        case CRYPTOGETACCOUNTRECORDS:
            precheckCode = raw.getCryptoGetAccountRecords()
                .getHeader()
                .getNodeTransactionPrecheckCode();
            break;
        case CRYPTOGETINFO:
            precheckCode = raw.getCryptoGetInfo()
                .getHeader()
                .getNodeTransactionPrecheckCode();
            break;
        case CRYPTOGETCLAIM:
            precheckCode = raw.getCryptoGetClaim()
                .getHeader()
                .getNodeTransactionPrecheckCode();
            break;
        case CRYPTOGETPROXYSTAKERS:
            precheckCode = raw.getCryptoGetProxyStakers()
                .getHeader()
                .getNodeTransactionPrecheckCode();
            break;
        case FILEGETCONTENTS:
            precheckCode = raw.getFileGetContents()
                .getHeader()
                .getNodeTransactionPrecheckCode();
            break;
        case FILEGETINFO:
            precheckCode = raw.getFileGetInfo()
                .getHeader()
                .getNodeTransactionPrecheckCode();
            break;
        case TRANSACTIONGETRECEIPT:
            precheckCode = raw.getTransactionGetReceipt()
                .getHeader()
                .getNodeTransactionPrecheckCode();
            unknownIsExceptional = false;
            break;
        case TRANSACTIONGETRECORD:
            precheckCode = raw.getTransactionGetRecord()
                .getHeader()
                .getNodeTransactionPrecheckCode();
            unknownIsExceptional = false;
            break;
        case RESPONSE_NOT_SET:
            throw new IllegalStateException("Response not set");
        default:
            // NOTE: TRANSACTIONGETFASTRECORD shouldn't be handled as we don't expose that query
            throw new RuntimeException("Unhandled response case");
        }

        HederaException.throwIfExceptional(precheckCode, unknownIsExceptional);
        return fromResponse(raw);
    }

    protected abstract Resp fromResponse(Response raw);
}
