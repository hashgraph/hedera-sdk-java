package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.proto.Query;
import com.hedera.hashgraph.proto.QueryHeader;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.HederaThrowable;
import com.hedera.hashgraph.sdk.QueryBuilder;

import java.util.function.Consumer;

import io.grpc.MethodDescriptor;

// `CryptoGetInfoQuery`
public final class AccountInfoQuery extends QueryBuilder<AccountInfo, AccountInfoQuery> {
    private final com.hedera.hashgraph.proto.CryptoGetInfoQuery.Builder builder;

    /**
     * @deprecated {@link Client} should now be provided to {@link #execute(Client)}
     */
    @Deprecated
    public AccountInfoQuery(Client client) {
        super(client);
        builder = inner.getCryptoGetInfoBuilder();
    }

    public AccountInfoQuery() {
        super();
        builder = inner.getCryptoGetInfoBuilder();
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public AccountInfoQuery setAccountId(AccountId account) {
        builder.setAccountID(account.toProto());
        return this;
    }

    @Override
    protected void doValidate() {
        require(builder.hasAccountID(), ".setAccountId() required");
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return CryptoServiceGrpc.getGetAccountInfoMethod();
    }

    @Override
    protected AccountInfo fromResponse(Response raw) {
        return AccountInfo.fromResponse(raw);
    }

    @Override
    public long getCost(Client client) throws HederaException, HederaNetworkException {
        // deleted accounts return a COST_ANSWER of zero which triggers `INSUFFICIENT_TX_FEE`
        // if you set that as the query payment; 25 tinybar seems to be enough to get
        // `ACCOUNT_DELETED` back instead.
        return Math.max(super.getCost(client), 25);
    }

    @Override
    public void getCostAsync(Client client, Consumer<Long> withCost, Consumer<HederaThrowable> onError) {
        // see above
        super.getCostAsync(client, (cost) -> withCost.accept(Math.min(cost, 25)), onError);
    }
}
