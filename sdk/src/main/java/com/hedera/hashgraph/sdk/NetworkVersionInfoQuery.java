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

import com.hedera.hashgraph.sdk.proto.NetworkGetVersionInfoQuery;
import com.hedera.hashgraph.sdk.proto.NetworkServiceGrpc;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import io.grpc.MethodDescriptor;

public class NetworkVersionInfoQuery extends Query<NetworkVersionInfo, NetworkVersionInfoQuery> {
    public NetworkVersionInfoQuery() {
    }

    @Override
    void onMakeRequest(com.hedera.hashgraph.sdk.proto.Query.Builder queryBuilder, QueryHeader header) {
        queryBuilder.setNetworkGetVersionInfo(NetworkGetVersionInfoQuery.newBuilder().setHeader(header));
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        return response.getNetworkGetVersionInfo().getHeader();
    }

    @Override
    QueryHeader mapRequestHeader(com.hedera.hashgraph.sdk.proto.Query request) {
        return request.getNetworkGetVersionInfo().getHeader();
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        // do nothing
    }

    @Override
    NetworkVersionInfo mapResponse(Response response, AccountId nodeId, com.hedera.hashgraph.sdk.proto.Query request) {
        return NetworkVersionInfo.fromProtobuf(response.getNetworkGetVersionInfo());
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Query, Response> getMethodDescriptor() {
        return NetworkServiceGrpc.getGetVersionInfoMethod();
    }
}
