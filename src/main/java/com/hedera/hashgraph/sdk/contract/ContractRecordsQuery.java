package com.hedera.hashgraph.sdk.contract;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.QueryBuilder;
import com.hedera.hashgraph.sdk.proto.*;
import io.grpc.MethodDescriptor;

// `ContractGetRecordsQuery`
public class ContractRecordsQuery extends QueryBuilder<ContractGetRecordsResponse> {
    private final ContractGetRecordsQuery.Builder builder = inner.getContractGetRecordsBuilder();

    public ContractRecordsQuery(Client client) {
        super(client);
    }

    ContractRecordsQuery() {
        super(null);
    }

    public ContractRecordsQuery setContract(ContractId contractId) {
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
        require(builder.hasContractID(), ".setContract() required");
    }
}
