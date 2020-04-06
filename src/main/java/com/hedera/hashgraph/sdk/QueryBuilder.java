package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import com.hedera.hashgraph.sdk.proto.ResponseType;

public abstract class QueryBuilder<O, T extends QueryBuilder<O, T>>
        extends HederaExecutable<Query, Response, O> {
    private final Query.Builder builder;

    private final QueryHeader.Builder headerBuilder;

    QueryBuilder() {
        builder = Query.newBuilder();
        headerBuilder = QueryHeader.newBuilder();

        headerBuilder.setResponseType(ResponseType.ANSWER_ONLY);
    }

    /**
     * Called in {@link #makeRequest} just before the query is built. The intent is for the derived
     * class to assign their data variant to the query.
     */
    protected abstract void onMakeRequest(Query.Builder queryBuilder, QueryHeader header);

    /** The derived class should access its response header and return. */
    protected abstract ResponseHeader mapResponseHeader(Response response);

    @Override
    protected final Query makeRequest() {
        onMakeRequest(builder, headerBuilder.build());

        // TODO: Generate a payment transaction if one was
        //       not set and payment is required

        return builder.build();
    }

    @Override
    protected final Status mapResponseStatus(Response response) {
        var preCheckCode = mapResponseHeader(response).getNodeTransactionPrecheckCode();

        return Status.valueOf(preCheckCode);
    }

    @Override
    protected final AccountId getNodeId(Client client) {
        // TODO: If this query needs a payment transaction we need to pick the node ID from the next
        //       payment transaction

        return client.getNextNodeId();
    }

    @Override
    protected TransactionId getTransactionId() {
        // TODO: Return the payment transaction ID
        throw new NullPointerException();
    }
}
