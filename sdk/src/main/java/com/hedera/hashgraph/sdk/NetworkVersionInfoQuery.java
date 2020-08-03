package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.NetworkGetVersionInfoQuery;
import com.hedera.hashgraph.sdk.proto.NetworkServiceGrpc;
import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import io.grpc.MethodDescriptor;

public class NetworkVersionInfoQuery extends QueryBuilder<NetworkVersionInfo, NetworkVersionInfoQuery> {
    private final NetworkGetVersionInfoQuery.Builder builder;

    public NetworkVersionInfoQuery() {
        builder = NetworkGetVersionInfoQuery.newBuilder();
    }

    @Override
    void onMakeRequest(Query.Builder queryBuilder, QueryHeader header) {
        queryBuilder.setNetworkGetVersionInfo(builder.setHeader(header));
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        return response.getNetworkGetVersionInfo().getHeader();
    }

    @Override
    QueryHeader mapRequestHeader(Query request) {
        return request.getNetworkGetVersionInfo().getHeader();
    }

    @Override
    NetworkVersionInfo mapResponse(Response response, AccountId nodeId) {
        return NetworkVersionInfo.fromProtobuf(response.getNetworkGetVersionInfo());
    }

    @Override
    MethodDescriptor<Query, Response> getMethodDescriptor() {
        return NetworkServiceGrpc.getGetVersionInfoMethod();
    }
}
