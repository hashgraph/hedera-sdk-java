package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.ContractGetBytecodeQuery;
import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import com.hedera.hashgraph.sdk.proto.SmartContractServiceGrpc;
import io.grpc.MethodDescriptor;

public final class ContractByteCodeQuery extends QueryBuilder<ByteString, ContractByteCodeQuery> {
    private final ContractGetBytecodeQuery.Builder builder;

    public ContractByteCodeQuery() {
        this.builder = ContractGetBytecodeQuery.newBuilder();
    }

    public ContractByteCodeQuery setContractId(ContractId contractId) {
        builder.setContractID(contractId.toProtobuf());
        return this;
    }

    @Override
    protected void onMakeRequest(Query.Builder queryBuilder, QueryHeader header) {
        queryBuilder.setContractGetBytecode(builder.setHeader(header));
    }

    @Override
    protected ResponseHeader mapResponseHeader(Response response) {
        return response.getContractGetBytecodeResponse().getHeader();
    }

    @Override
    protected QueryHeader mapRequestHeader(Query request) {
        return request.getContractGetBytecode().getHeader();
    }

    @Override
    protected ByteString mapResponse(Response response) {
        return response.getContractGetBytecodeResponse().getBytecode();
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethodDescriptor() {
        return SmartContractServiceGrpc.getContractGetBytecodeMethod();
    }
}
