package com.hedera.sdk.contract;

import com.hedera.sdk.Client;
import com.hedera.sdk.HederaException;
import com.hedera.sdk.QueryBuilder;
import com.hedera.sdk.proto.*;
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
    protected ContractGetRecordsResponse mapResponse(Response raw) throws HederaException {
        return raw.getContractGetRecordsResponse();
    }

    @Override
    protected void doValidate() {
        require(builder.hasContractID(), ".setContract() required");
    }
}
