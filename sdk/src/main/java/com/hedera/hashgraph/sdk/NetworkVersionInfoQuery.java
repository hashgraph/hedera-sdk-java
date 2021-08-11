package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.NetworkGetVersionInfoQuery;
import com.hedera.hashgraph.sdk.proto.NetworkServiceGrpc;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;

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
