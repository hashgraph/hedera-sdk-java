/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.CryptoGetAccountBalanceQuery;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Get the balance of a Hedera™ crypto-currency account. This returns only the balance, so it is a
 * smaller and faster reply than {@link AccountInfoQuery}.
 *
 * <p>This query is free.
 */
public final class AccountBalanceQuery extends Query<AccountBalance, AccountBalanceQuery> {
    @Nullable
    private AccountId accountId = null;
    @Nullable
    private ContractId contractId = null;

    public AccountBalanceQuery() {
    }

    @Nullable
    public AccountId getAccountId() {
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

    @Nullable
    public ContractId getContractId() {
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
    void validateChecksums(Client client) throws BadEntityIdException {
        if (accountId != null) {
            accountId.validateChecksum(client);
        }

        if (contractId != null) {
            contractId.validateChecksum(client);
        }
    }

    @Override
    boolean isPaymentRequired() {
        return false;
    }

    @Override
    void onMakeRequest(com.hedera.hashgraph.sdk.proto.Query.Builder queryBuilder, QueryHeader header) {
        var builder = CryptoGetAccountBalanceQuery.newBuilder();
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
