package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.QueryBuilder;
import com.hedera.hashgraph.sdk.proto.*;
import io.grpc.MethodDescriptor;

// `CryptoGetAccountRecordsQuery`
public class AccountRecordsQuery extends QueryBuilder<CryptoGetAccountRecordsResponse> {
    private final CryptoGetAccountRecordsQuery.Builder builder = inner.getCryptoGetAccountRecordsBuilder();

    public AccountRecordsQuery(Client client) {
        super(client);
    }

    AccountRecordsQuery() {
        super(null);
    }

    public AccountRecordsQuery setAccount(AccountId accountId) {
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
