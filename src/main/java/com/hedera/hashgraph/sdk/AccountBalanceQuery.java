package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.CryptoGetAccountBalanceQuery;
import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;

/**
 * Get the balance of a Hedera crypto-currency account. This returns only the balance, so it is a
 * smaller and faster reply than {@link AccountInfoQuery}.
 */
public final class AccountBalanceQuery extends QueryBuilder<Long, AccountBalanceQuery> {
    private final CryptoGetAccountBalanceQuery.Builder builder;

    public AccountBalanceQuery() {
        builder = CryptoGetAccountBalanceQuery.newBuilder();
    }

    /** The account ID for which the balance is being requested. */
    public AccountBalanceQuery setAccountId(AccountId accountId) {
        builder.setAccountID(accountId.toProtobuf());
        return this;
    }

    /** The contract ID for which the balance is being requested. */
    public AccountBalanceQuery setContractId(ContractId contractId) {
        builder.setContractID(contractId.toProtobuf());
        return this;
    }

    @Override
    protected void onBuild(Query.Builder queryBuilder, QueryHeader header) {
        queryBuilder.setCryptogetAccountBalance(builder.setHeader(header));
    }

    @Override
    protected Long mapResponse(Response response) {
        return response.getCryptogetAccountBalance().getBalance();
    }
}
