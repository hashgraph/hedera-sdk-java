package com.hedera.hashgraph.sdk.contract;

import com.hedera.hashgraph.proto.ContractGetBytecodeQuery;
import com.hedera.hashgraph.proto.Query;
import com.hedera.hashgraph.proto.QueryHeader;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.proto.SmartContractServiceGrpc;
import com.hedera.hashgraph.sdk.QueryBuilder;

import io.grpc.MethodDescriptor;

/**
 * Get the bytecode of a contract.
 */
// `ContractGetBytecodeQuery`
public final class ContractBytecodeQuery extends QueryBuilder<byte[], ContractBytecodeQuery> {
    private final ContractGetBytecodeQuery.Builder builder = inner.getContractGetBytecodeBuilder();

    public ContractBytecodeQuery() {
        super();
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public ContractBytecodeQuery setContractId(ContractId contract) {
        builder.setContractID(contract.toProto());
        return this;
    }

    @Override
    protected void doValidate() {
        require(builder.hasContractID(), ".setContractId() required");
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return SmartContractServiceGrpc.getContractGetBytecodeMethod();
    }

    @Override
    protected byte[] extractResponse(Response raw) {
        return raw.getContractGetBytecodeResponse().getBytecode().toByteArray();
    }
}
