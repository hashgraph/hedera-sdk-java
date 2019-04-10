package com.hedera.sdk;

import com.hedera.sdk.proto.*;
import io.grpc.Channel;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;

public abstract class QueryBuilder<Resp> extends ValidatingHederaCall<Query, Response, Resp> {
    protected Query.Builder inner = Query.newBuilder();

    @Nullable
    private final Client client;

    protected QueryBuilder(@Nullable Client client, Function<Response, Resp> mapResponse) {
        super(mapResponse);
        this.client = client;
    }

    protected abstract QueryHeader.Builder getHeaderBuilder();

    @Override
    protected Channel getChannel() {
        Objects.requireNonNull(client, "QueryBuilder.client must be non-null in regular use");
        return client.getChannel()
            .getChannel();
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

    /** Check that the query was built properly, throwing an exception on any errors. */
    @Override
    public final void validate() {
        require(getHeaderBuilder().hasPayment(), ".setPayment() required");
        doValidate();
        checkValidationErrors("query builder failed validation");
    }
}
