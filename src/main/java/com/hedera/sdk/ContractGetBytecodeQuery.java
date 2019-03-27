package com.hedera.sdk;

import com.hedera.sdk.proto.QueryHeader;

public final class ContractGetBytecodeQuery extends QueryBuilder {
    private final com.hedera.sdk.proto.ContractGetBytecodeQuery.Builder builder;

    public ContractGetBytecodeQuery() {
        builder = inner.getContractGetBytecodeBuilder();
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public ContractGetBytecodeQuery setContract(ContractId contract) {
        builder.setContractID(contract.inner);
        return this;
    }
}
