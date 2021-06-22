package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.CryptoGetAccountBalanceQuery;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import io.grpc.MethodDescriptor;

import java.util.Objects;
import javax.annotation.Nullable;

/**
 * Get the balance of a Hedera™ crypto-currency account. This returns only the balance, so it is a
 * smaller and faster reply than {@link AccountInfoQuery}.
 *
 * <p>This query is free.
 */
public final class AccountBalanceQuery extends Query<AccountBalance, AccountBalanceQuery> {
    private final CryptoGetAccountBalanceQuery.Builder builder;

    AccountId accountId;
    ContractId contractId;

    public AccountBalanceQuery() {
        builder = CryptoGetAccountBalanceQuery.newBuilder();
    }

    public AccountId getAccountId() {
        if (accountId == null) {
            return new AccountId(0);
        }

        return accountId;
    }

    /**
     * The account ID for which the balance is being requested.
     * <p>
     * This is mutually exclusive with {@link #setContractId(ContractId)}.
     *
     * @param accountId The AccountId to set
     * @return {@code this}
     */
    public AccountBalanceQuery setAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId);
        this.accountId = accountId;
        return this;
    }

    public ContractId getContractId() {
        if (contractId == null) {
            return new ContractId(0);
        }

        return contractId;
    }

    /**
     * The contract ID for which the balance is being requested.
     * <p>
     * This is mutually exclusive with {@link #setAccountId(AccountId)}.
     *
     * @param contractId The ContractId to set
     * @return {@code this}
     */
    public AccountBalanceQuery setContractId(ContractId contractId) {
        Objects.requireNonNull(contractId);
        this.contractId = contractId;
        return this;
    }

    @Override
    void validateNetworkOnIds(@Nullable AccountId accountId) {
        EntityIdHelper.validateNetworkOnIds(this.accountId, accountId);
        EntityIdHelper.validateNetworkOnIds(this.contractId, accountId);
    }

    @Override
    boolean isPaymentRequired() {
        return false;
    }

    @Override
    void onMakeRequest(com.hedera.hashgraph.sdk.proto.Query.Builder queryBuilder, QueryHeader header) {
        if (accountId != null) {
            builder.setAccountID(accountId.toProtobuf());
        }

        if (contractId != null) {
            builder.setContractID(contractId.toProtobuf());
        }

        queryBuilder.setCryptogetAccountBalance(builder.setHeader(header));
    }

    @Override
    AccountBalance mapResponse(Response response, AccountId nodeId, com.hedera.hashgraph.sdk.proto.Query request) {
        return AccountBalance.fromProtobuf(response.getCryptogetAccountBalance());
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
