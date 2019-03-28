package com.hedera.sdk;

import com.hedera.sdk.proto.QueryHeader;

public final class ContractGetInfoQuery extends QueryBuilder {
    private final com.hedera.sdk.proto.ContractGetInfoQuery.Builder builder;

    public ContractGetInfoQuery() {
        builder = inner.getContractGetInfoBuilder();
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public ContractGetInfoQuery setContract(ContractId contract) {
        builder.setContractID(contract.inner);
        return this;
    }
}
