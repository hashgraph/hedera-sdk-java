package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.QueryBuilder;
import com.hederahashgraph.api.proto.java.CryptoGetAccountRecordsQuery;
import com.hederahashgraph.api.proto.java.CryptoGetAccountRecordsResponse;
import com.hederahashgraph.api.proto.java.Query;
import com.hederahashgraph.api.proto.java.QueryHeader;
import com.hederahashgraph.api.proto.java.Response;
import com.hederahashgraph.service.proto.java.CryptoServiceGrpc;

import io.grpc.MethodDescriptor;

// `CryptoGetAccountRecordsQuery`
public class AccountRecordsQuery extends QueryBuilder<CryptoGetAccountRecordsResponse, AccountRecordsQuery> {
    private final CryptoGetAccountRecordsQuery.Builder builder = inner.getCryptoGetAccountRecordsBuilder();

    public AccountRecordsQuery(Client client) {
        super(client);
    }

    AccountRecordsQuery() {
        super(null);
    }

    public AccountRecordsQuery setAccountId(AccountId accountId) {
        builder.setAccountID(accountId.toProto());
        return this;
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    @Override
    protected void doValidate() {
        require(builder.hasAccountID(), ".setAccountId() required");
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return CryptoServiceGrpc.getGetAccountRecordsMethod();
    }

    @Override
    protected CryptoGetAccountRecordsResponse fromResponse(Response raw) {
        return raw.getCryptoGetAccountRecords();
    }
}
