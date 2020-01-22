package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.proto.CryptoGetAccountBalanceQuery;
import com.hedera.hashgraph.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.proto.Query;
import com.hedera.hashgraph.proto.QueryHeader;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.QueryBuilder;

import com.hedera.hashgraph.sdk.contract.ContractId;
import io.grpc.MethodDescriptor;

// `CryptoGetAccountBalanceQuery`
public final class AccountBalanceQuery extends QueryBuilder<Hbar, AccountBalanceQuery> {
    private final CryptoGetAccountBalanceQuery.Builder builder = inner.getCryptogetAccountBalanceBuilder();

    public AccountBalanceQuery() {
        super();

        // a payment transaction is required but is not processed so it can have zero value
        setQueryPayment(0);
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public AccountBalanceQuery setAccountId(AccountId accountId) {
        builder.setAccountID(accountId.toProto());
        return this;
    }

    public AccountBalanceQuery setContractId(ContractId contractId) {
        builder.setContractID(contractId.toProto());
        return this;
    }

    @Override
    protected void doValidate() {
        require(builder.hasAccountID() || builder.hasContractID(),
            ".setAccountId() or .setContractId() required");

        require(getHeaderBuilder().hasPayment(),
            "AccountBalanceQuery requires a payment for validation but it is not processed; "
                + "one would have been created automatically but the given Client did not have "
                + "an operator ID or key set. You must instead manually create, sign and then set "
                + "a payment transaction with .setPayment().");
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return CryptoServiceGrpc.getCryptoGetBalanceMethod();
    }

    @Override
    protected Hbar extractResponse(Response raw) {
        return Hbar.fromTinybar(raw.getCryptogetAccountBalance().getBalance());
    }
}
