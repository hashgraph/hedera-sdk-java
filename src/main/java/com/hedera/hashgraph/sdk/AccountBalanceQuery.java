package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.CryptoGetAccountBalanceQuery;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import io.grpc.MethodDescriptor;

/**
 * Get the balance of a Hedera crypto-currency account. This returns only the balance, so it is a
 * smaller and faster reply than {@link AccountInfoQuery}.
 *
 * <p>This query is free.
 */
public final class AccountBalanceQuery extends QueryBuilder<Long, AccountBalanceQuery> {
    private final CryptoGetAccountBalanceQuery.Builder builder;

    public AccountBalanceQuery() {
        builder = CryptoGetAccountBalanceQuery.newBuilder();
    }

    /**
     * The account ID for which the balance is being requested.
     */
    public AccountBalanceQuery setAccountId(AccountId accountId) {
        builder.setAccountID(accountId.toProtobuf());
        return this;
    }

    /**
     * The contract ID for which the balance is being requested.
     */
    public AccountBalanceQuery setContractId(ContractId contractId) {
        builder.setContractID(contractId.toProtobuf());
        return this;
    }

    @Override
    protected boolean isPaymentRequired() {
        return false;
    }

    @Override
    protected void onMakeRequest(Query.Builder queryBuilder, QueryHeader header) {
        queryBuilder.setCryptogetAccountBalance(builder.setHeader(header));
    }

    @Override
    protected Long mapResponse(Response response) {
        return response.getCryptogetAccountBalance().getBalance();
    }

    @Override
    protected ResponseHeader mapResponseHeader(Response response) {
        return response.getCryptogetAccountBalance().getHeader();
    }

    @Override
    protected QueryHeader mapRequestHeader(Query request) {
        return request.getCryptogetAccountBalance().getHeader();
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethodDescriptor() {
        return CryptoServiceGrpc.getCryptoGetBalanceMethod();
    }
}
