package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.CryptoGetInfoQuery;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import io.grpc.MethodDescriptor;
import java8.util.concurrent.CompletableFuture;

import java.util.Objects;
import javax.annotation.Nullable;

/**
 * Get all the information about an account, including the balance.
 * This does not get the list of account records.
 */
public final class AccountInfoQuery extends Query<AccountInfo, AccountInfoQuery> {
    private final CryptoGetInfoQuery.Builder builder;

    AccountId accountId;

    public AccountInfoQuery() {
        builder = CryptoGetInfoQuery.newBuilder();
    }

    public AccountId getAccountId() {
        if (accountId == null) {
            return new AccountId(0);
        }

        return accountId;
    }

    /**
     * Sets the account ID for which information is requested.
     *
     * @return {@code this}
     * @param accountId  The AccountId to be set
     */
    public AccountInfoQuery setAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId);
        this.accountId = accountId;
        return this;
    }

    @Override
    void validateNetworkOnIds(@Nullable AccountId accountId) {
        EntityIdHelper.validateNetworkOnIds(this.accountId, accountId);
    }

    @Override
    void onMakeRequest(com.hedera.hashgraph.sdk.proto.Query.Builder queryBuilder, QueryHeader header) {
        if (accountId != null) {
            builder.setAccountID(accountId.toProtobuf());
        }

        queryBuilder.setCryptoGetInfo(builder.setHeader(header));
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        return response.getCryptoGetInfo().getHeader();
    }

    @Override
    QueryHeader mapRequestHeader(com.hedera.hashgraph.sdk.proto.Query request) {
        return request.getCryptoGetInfo().getHeader();
    }

    @Override
    AccountInfo mapResponse(Response response, AccountId nodeId, com.hedera.hashgraph.sdk.proto.Query request) {
        return AccountInfo.fromProtobuf(response.getCryptoGetInfo().getAccountInfo());
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Query, Response> getMethodDescriptor() {
        return CryptoServiceGrpc.getGetAccountInfoMethod();
    }

    @Override
    public CompletableFuture<Hbar> getCostAsync(Client client) {
        // deleted accounts return a COST_ANSWER of zero which triggers `INSUFFICIENT_TX_FEE`
        // if you set that as the query payment; 25 tinybar seems to be enough to get
        // `ACCOUNT_DELETED` back instead.
        return super.getCostAsync(client).thenApply((cost) -> Hbar.fromTinybars(Math.max(cost.toTinybars(), 25)));
    }
}
