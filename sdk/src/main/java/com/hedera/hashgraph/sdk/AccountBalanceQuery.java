package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.CryptoGetAccountBalanceQuery;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import io.grpc.MethodDescriptor;

/**
 * Get the balance of a Hedera™ crypto-currency account. This returns only the balance, so it is a
 * smaller and faster reply than {@link AccountInfoQuery}.
 *
 * <p>This query is free.
 */
public final class AccountBalanceQuery extends Query<Hbar, AccountBalanceQuery> {
    private final CryptoGetAccountBalanceQuery.Builder builder;

    public AccountBalanceQuery() {
        builder = CryptoGetAccountBalanceQuery.newBuilder();
    }

    /**
     * The account ID for which the balance is being requested.
     *
     * This is mutually exclusive with {@link #setContractId(ContractId)}.
     * @return {@code this}
     * @param accountId The AccountId to set
     */
    public AccountBalanceQuery setAccountId(AccountId accountId) {
        builder.setAccountID(accountId.toProtobuf());
        return this;
    }

    /**
     * The contract ID for which the balance is being requested.
     *
     * This is mutually exclusive with {@link #setAccountId(AccountId)}.
     * @return {@code this}
     * @param contractId The ContractId to set
     */
    public AccountBalanceQuery setContractId(ContractId contractId) {
        builder.setContractID(contractId.toProtobuf());
        return this;
    }

    @Override
    boolean isPaymentRequired() {
        return false;
    }

    @Override
    void onMakeRequest(com.hedera.hashgraph.sdk.proto.Query.Builder queryBuilder, QueryHeader header) {
        queryBuilder.setCryptogetAccountBalance(builder.setHeader(header));
    }

    @Override
    Hbar mapResponse(Response response, AccountId nodeId, com.hedera.hashgraph.sdk.proto.Query request) {
        return Hbar.fromTinybars(response.getCryptogetAccountBalance().getBalance());
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        return response.getCryptogetAccountBalance().getHeader();
    }

    @Override
    QueryHeader mapRequestHeader(com.hedera.hashgraph.sdk.proto.Query request) {
        return request.getCryptogetAccountBalance().getHeader();
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Query, Response> getMethodDescriptor() {
        return CryptoServiceGrpc.getCryptoGetBalanceMethod();
    }
}
