// SPDX-License-Identifier: Apache-2.0
package org.hiero.sdk.java;

import io.grpc.MethodDescriptor;
import org.hiero.sdk.java.proto.NetworkGetVersionInfoQuery;
import org.hiero.sdk.java.proto.NetworkServiceGrpc;
import org.hiero.sdk.java.proto.QueryHeader;
import org.hiero.sdk.java.proto.Response;
import org.hiero.sdk.java.proto.ResponseHeader;

/**
 * Information about the versions of protobuf and hedera.
 */
public class NetworkVersionInfoQuery extends Query<NetworkVersionInfo, NetworkVersionInfoQuery> {
    /**
     * Constructor.
     */
    public NetworkVersionInfoQuery() {}

    @Override
    void onMakeRequest(org.hiero.sdk.java.proto.Query.Builder queryBuilder, QueryHeader header) {
        queryBuilder.setNetworkGetVersionInfo(
                NetworkGetVersionInfoQuery.newBuilder().setHeader(header));
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        return response.getNetworkGetVersionInfo().getHeader();
    }

    @Override
    QueryHeader mapRequestHeader(org.hiero.sdk.java.proto.Query request) {
        return request.getNetworkGetVersionInfo().getHeader();
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        // do nothing
    }

    @Override
    NetworkVersionInfo mapResponse(Response response, AccountId nodeId, org.hiero.sdk.java.proto.Query request) {
        return NetworkVersionInfo.fromProtobuf(response.getNetworkGetVersionInfo());
    }

    @Override
    MethodDescriptor<org.hiero.sdk.java.proto.Query, Response> getMethodDescriptor() {
        return NetworkServiceGrpc.getGetVersionInfoMethod();
    }
}
