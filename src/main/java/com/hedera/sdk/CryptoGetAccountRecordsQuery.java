package com.hedera.sdk;

import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

public class CryptoGetAccountRecordsQuery extends QueryBuilder<CryptoGetAccountRecordsResponse> {
    private final com.hedera.sdk.proto.CryptoGetAccountRecordsQuery.Builder builder;

    public CryptoGetAccountRecordsQuery() {
        super(Response::getCryptoGetAccountRecords);
        builder = inner.getCryptoGetAccountRecordsBuilder();
    }

    public CryptoGetAccountRecordsQuery setAccount(AccountId accountId) {
        builder.setAccountID(accountId.toProto());
        return this;
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    @Override
    MethodDescriptor<Query, Response> getMethod() {
        return CryptoServiceGrpc.getGetAccountRecordsMethod();
    }
}
