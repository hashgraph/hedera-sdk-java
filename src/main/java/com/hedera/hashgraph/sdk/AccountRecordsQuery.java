package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import io.grpc.MethodDescriptor;

import java.util.List;

// TODO: TransactionRecord
public final class AccountRecordsQuery extends QueryBuilder<List<Void>, AccountRecordsQuery> {
    @Override
    protected void onMakeRequest(Query.Builder queryBuilder, QueryHeader header) {
    }

    @Override
    protected ResponseHeader mapResponseHeader(Response response) {
        return response.getCryptoGetAccountRecords().getHeader();
    }

    @Override
    protected QueryHeader mapRequestHeader(Query request) {
        return request.getCryptoGetAccountRecords().getHeader();
    }

    @Override
    protected List<Void> mapResponse(Response response) {
        return null;
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethodDescriptor() {
        return null;
    }
}
