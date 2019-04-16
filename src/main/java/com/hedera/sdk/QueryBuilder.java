package com.hedera.sdk;

import com.hedera.sdk.account.CryptoTransferTransaction;
import com.hedera.sdk.proto.*;
import io.grpc.Channel;

import javax.annotation.Nullable;
import java.util.Objects;

public abstract class QueryBuilder<Resp> extends Builder<Query, Response, Resp> {
    protected Query.Builder inner = Query.newBuilder();

    @Nullable
    private final Client client;

    protected QueryBuilder(@Nullable Client client) {
        this.client = client;
    }

    protected abstract QueryHeader.Builder getHeaderBuilder();

    @Override
    protected Channel getChannel() {
        Objects.requireNonNull(client, "QueryBuilder.client must be non-null in regular use");
        return client.pickNode()
            .getChannel();
    }

    @Override
    public final Query toProto() {
        if (client != null && isPaymentRequired() && !getHeaderBuilder().hasPayment() && client.getOperatorId() != null) {
            // FIXME: Require setAutoPayment to be set ?

            var cost = getCost();
            var operatorId = client.getOperatorId();
            var nodeId = client.pickNode().accountId;
            var txPayment = new CryptoTransferTransaction(client).setNodeAccountId(nodeId)
                .setTransactionId(new TransactionId(operatorId))
                .addSender(operatorId, cost)
                .addRecipient(nodeId, cost)
                .build();

            setPayment(txPayment);
        }

        validate();

        return inner.build();
    }

    public QueryBuilder setPayment(Transaction transaction) {
        getHeaderBuilder().setPayment(transaction.toProto());
        return this;
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
    protected final void validate() {
        if (isPaymentRequired()) {
            require(getHeaderBuilder().hasPayment(), ".setPayment() required");
        }

        doValidate();
        checkValidationErrors("query builder failed validation");
    }
}
