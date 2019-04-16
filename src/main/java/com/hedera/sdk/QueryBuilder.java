package com.hedera.sdk;

import com.hedera.sdk.proto.*;
import io.grpc.Channel;

import javax.annotation.Nullable;
import java.util.Objects;

public abstract class QueryBuilder<Resp> extends HederaCall<Query, Response, Resp> {
    protected Query.Builder inner = Query.newBuilder();

    @Nullable
    private final ChannelHolder channel;

    protected QueryBuilder(@Nullable Client client) {
        this(client != null ? client.getChannel() : null);

    }

    QueryBuilder(@Nullable ChannelHolder channel) {
        this.channel = channel;
    }

    protected abstract QueryHeader.Builder getHeaderBuilder();

    @Override
    protected Channel getChannel() {
        Objects.requireNonNull(channel, "QueryBuilder.channel must be non-null in regular use");
        return channel.getChannel();
    }

    @Override
    public final Query toProto() {
        validate();
        return inner.build();
    }

    public QueryBuilder setPayment(Transaction transaction) {
        getHeaderBuilder().setPayment(transaction.toProto());
        return this;
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
