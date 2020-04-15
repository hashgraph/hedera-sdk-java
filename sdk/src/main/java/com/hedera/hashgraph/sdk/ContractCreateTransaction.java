package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.ContractCreateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import org.threeten.bp.Duration;

public final class ContractCreateTransaction extends TransactionBuilder<ContractCreateTransaction> {
    private final ContractCreateTransactionBody.Builder builder;

    public ContractCreateTransaction() {
        builder = ContractCreateTransactionBody.newBuilder();

        setAutoRenewPeriod(DEFAULT_AUTO_RENEW_PERIOD);
    }

    public ContractCreateTransaction setByteCodeFileId(FileId byteCodeFileId) {
        builder.setFileID(byteCodeFileId.toProtobuf());
        return this;
    }

    public ContractCreateTransaction setAdminKey(Key adminKey) {
        builder.setAdminKey(adminKey.toKeyProtobuf());
        return this;
    }

    public ContractCreateTransaction setGas(long gas) {
        builder.setGas(gas);
        return this;
    }

    public ContractCreateTransaction setInitialBalance(Hbar initialBalance) {
        builder.setInitialBalance(initialBalance.asTinybar());
        return this;
    }

    public ContractCreateTransaction setProxyAccountId(AccountId proxyAccountId) {
        builder.setProxyAccountID(proxyAccountId.toProtobuf());
        return this;
    }

    public ContractCreateTransaction setAutoRenewPeriod(Duration autoRenewPeriod) {
        builder.setAutoRenewPeriod(DurationConverter.toProtobuf(autoRenewPeriod));
        return this;
    }

    public ContractCreateTransaction setConstructorParameters(byte[] constructorParameters) {
        builder.setConstructorParameters(ByteString.copyFrom(constructorParameters));
        return this;
    }

    public ContractCreateTransaction setConstructorParameters(ContractFunctionParameters constructorParameters) {
        builder.setConstructorParameters(constructorParameters.toBytes(null));
        return this;
    }

    public ContractCreateTransaction setContractMemo(String memo) {
        builder.setMemo(memo);
        return this;
    }

    @Override
    void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setContractCreateInstance(builder);
    }
}
