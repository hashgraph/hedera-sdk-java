// SPDX-License-Identifier: Apache-2.0
package com.hiero.sdk;

import com.hiero.sdk.proto.NetworkGetVersionInfoQuery;
import com.hiero.sdk.proto.NetworkServiceGrpc;
import com.hiero.sdk.proto.QueryHeader;
import com.hiero.sdk.proto.Response;
import com.hiero.sdk.proto.ResponseHeader;
import io.grpc.MethodDescriptor;

/**
 * Information about the versions of protobuf and hedera.
 */
public class NetworkVersionInfoQuery extends Query<NetworkVersionInfo, NetworkVersionInfoQuery> {
    /**
     * Constructor.
     */
    public NetworkVersionInfoQuery() {}

    @Override
    void onMakeRequest(com.hiero.sdk.proto.Query.Builder queryBuilder, QueryHeader header) {
        queryBuilder.setNetworkGetVersionInfo(
                NetworkGetVersionInfoQuery.newBuilder().setHeader(header));
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        return response.getNetworkGetVersionInfo().getHeader();
    }

    @Override
    QueryHeader mapRequestHeader(com.hiero.sdk.proto.Query request) {
        return request.getNetworkGetVersionInfo().getHeader();
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        // do nothing
    }

    @Override
    NetworkVersionInfo mapResponse(Response response, AccountId nodeId, com.hiero.sdk.proto.Query request) {
        return NetworkVersionInfo.fromProtobuf(response.getNetworkGetVersionInfo());
    }

    @Override
    MethodDescriptor<com.hiero.sdk.proto.Query, Response> getMethodDescriptor() {
        return NetworkServiceGrpc.getGetVersionInfoMethod();
    }
}
