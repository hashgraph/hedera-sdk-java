package com.hedera.hashgraph.sdk.contract;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.QueryBuilder;
import com.hedera.hashgraph.proto.ContractGetRecordsQuery;
import com.hedera.hashgraph.proto.ContractGetRecordsResponse;
import com.hedera.hashgraph.proto.Query;
import com.hedera.hashgraph.proto.QueryHeader;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.proto.SmartContractServiceGrpc;

import io.grpc.MethodDescriptor;

// `ContractGetRecordsQuery`
public class ContractRecordsQuery extends QueryBuilder<ContractGetRecordsResponse, ContractRecordsQuery> {
    private final ContractGetRecordsQuery.Builder builder = inner.getContractGetRecordsBuilder();

    /**
     * @deprecated {@link Client} should now be provided to {@link #execute(Client)}
     */
    @Deprecated
    public ContractRecordsQuery(Client client) {
        super(client);
    }

    public ContractRecordsQuery() {
        super(null);
    }

    public ContractRecordsQuery setContractId(ContractId contractId) {
        builder.setContractID(contractId.toProto());
        return this;
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return SmartContractServiceGrpc.getGetTxRecordByContractIDMethod();
    }

    @Override
    protected ContractGetRecordsResponse fromResponse(Response raw) {
        return raw.getContractGetRecordsResponse();
    }

    @Override
    protected void doValidate() {
        require(builder.hasContractID(), ".setContractId() required");
    }
}
