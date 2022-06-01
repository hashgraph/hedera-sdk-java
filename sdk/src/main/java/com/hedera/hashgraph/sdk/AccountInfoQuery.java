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

import com.hedera.hashgraph.sdk.proto.CryptoGetInfoQuery;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import io.grpc.MethodDescriptor;

import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Get all the information about an account, including the balance.
 * This does not get the list of account records.
 */
public final class AccountInfoQuery extends Query<AccountInfo, AccountInfoQuery> {

    @Nullable
    private AccountId accountId = null;

    /**
     * Constructor.
     */
    public AccountInfoQuery() {
    }

    /**
     * Extract the account id.
     *
     * @return                          the account id
     */
    @Nullable
    public AccountId getAccountId() {
        return accountId;
    }

    /**
     * Sets the account ID for which information is requested.
     *
     * @param accountId The AccountId to be set
     * @return {@code this}
     */
    public AccountInfoQuery setAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId);
        this.accountId = accountId;
        return this;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (accountId != null) {
            accountId.validateChecksum(client);
        }
    }

    @Override
    void onMakeRequest(com.hedera.hashgraph.sdk.proto.Query.Builder queryBuilder, QueryHeader header) {
        var builder = CryptoGetInfoQuery.newBuilder();

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
