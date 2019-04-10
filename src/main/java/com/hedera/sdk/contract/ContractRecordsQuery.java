package com.hedera.sdk.contract;

import com.hedera.sdk.Client;
import com.hedera.sdk.ContractId;
import com.hedera.sdk.QueryBuilder;
import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

// `ContractGetRecordsQuery`
public class ContractRecordsQuery extends QueryBuilder<ContractGetRecordsResponse> {
    private final ContractGetRecordsQuery.Builder builder = inner.getContractGetRecordsBuilder();

    public ContractRecordsQuery(Client client) {
        super(client, Response::getContractGetRecordsResponse);
    }

    ContractRecordsQuery() {
        super(null, Response::getContractGetRecordsResponse);
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
    protected void doValidate() {
        require(builder.hasContractID(), ".setContract() required");
    }
}
