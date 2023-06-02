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

import com.hedera.hashgraph.sdk.proto.CryptoGetStakersQuery;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Get all the accounts that are proxy staking to this account.
 * For each of them, give the amount currently staked.
 * <p>
 * This is not yet implemented, but will be in a future version of the API.
 */
public final class AccountStakersQuery extends Query<List<ProxyStaker>, AccountStakersQuery> {
    @Nullable
    private AccountId accountId;

    /**
     * Constructor.
     */
    public AccountStakersQuery() {
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
     * Sets the Account ID for which the records should be retrieved.
     *
     * @param accountId The AccountId to be set
     * @return {@code this}
     */
    public AccountStakersQuery setAccountId(AccountId accountId) {
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
        var builder = CryptoGetStakersQuery.newBuilder();

        if (accountId != null) {
            builder.setAccountID(accountId.toProtobuf());
        }

        queryBuilder.setCryptoGetProxyStakers(builder.setHeader(header));
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        return response.getCryptoGetProxyStakers().getHeader();
    }

    @Override
    QueryHeader mapRequestHeader(com.hedera.hashgraph.sdk.proto.Query request) {
        return request.getCryptoGetProxyStakers().getHeader();
    }

    @Override
    List<ProxyStaker> mapResponse(Response response, AccountId nodeId, com.hedera.hashgraph.sdk.proto.Query request) {
        var rawStakers = response.getCryptoGetProxyStakers().getStakers();
        var stakers = new ArrayList<ProxyStaker>(rawStakers.getProxyStakerCount());

        for (var i = 0; i < rawStakers.getProxyStakerCount(); ++i) {
            stakers.add(ProxyStaker.fromProtobuf(rawStakers.getProxyStaker(i)));
        }

        return stakers;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Query, Response> getMethodDescriptor() {
        return CryptoServiceGrpc.getGetStakersByAccountIDMethod();
    }
}
