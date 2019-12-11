package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.QueryBuilder;
import com.hedera.hashgraph.proto.CryptoGetAccountRecordsQuery;
import com.hedera.hashgraph.proto.CryptoGetAccountRecordsResponse;
import com.hedera.hashgraph.proto.Query;
import com.hedera.hashgraph.proto.QueryHeader;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.proto.CryptoServiceGrpc;

import io.grpc.MethodDescriptor;

// `CryptoGetAccountRecordsQuery`
public class AccountRecordsQuery extends QueryBuilder<CryptoGetAccountRecordsResponse, AccountRecordsQuery> {
    private final CryptoGetAccountRecordsQuery.Builder builder = inner.getCryptoGetAccountRecordsBuilder();

    /**
     * @deprecated {@link Client} should now be provided to {@link #execute(Client)}
     */
    @Deprecated
    public AccountRecordsQuery(Client client) {
        super(client);
    }

    public AccountRecordsQuery() {
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
