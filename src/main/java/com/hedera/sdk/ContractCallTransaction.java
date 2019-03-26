package com.hedera.sdk;

import com.google.protobuf.ByteString;
import com.hedera.sdk.proto.ContractCallTransactionBody;

public final class ContractCallTransaction extends TransactionBuilder<ContractCallTransaction> {
    private final ContractCallTransactionBody.Builder builder;

    public ContractCallTransaction() {
        builder = inner.getBodyBuilder().getContractCallBuilder();
    }

    public ContractCallTransaction setContractId(ContractId contractId) {
        builder.setContractID(contractId.inner);
        return this;
    }

    public ContractCallTransaction setGas(long gas) {
        builder.setGas(gas);
        return this;
    }

    public ContractCallTransaction setAmount(long amount) {
        builder.setAmount(amount);
        return this;
    }

    public ContractCallTransaction setFunctionParameters(byte[] functionParameters) {
        builder.setFunctionParameters(ByteString.copyFrom(functionParameters));
        return this;
    }
}
