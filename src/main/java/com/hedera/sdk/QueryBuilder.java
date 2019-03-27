package com.hedera.sdk;

import com.hedera.sdk.proto.QueryHeader;
import com.hedera.sdk.proto.ResponseType;

abstract class QueryBuilder {
    com.hedera.sdk.proto.Query.Builder inner = com.hedera.sdk.proto.Query.newBuilder();

    protected abstract QueryHeader.Builder getHeaderBuilder();

    QueryBuilder() {
        getHeaderBuilder().setResponseType(ResponseType.ANSWER_ONLY);
    }

    public QueryBuilder setPayment(Transaction transaction) {
        getHeaderBuilder().setPayment(transaction.build());
        return this;
    }
}
