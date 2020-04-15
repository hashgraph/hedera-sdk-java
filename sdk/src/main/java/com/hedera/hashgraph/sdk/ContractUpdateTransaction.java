package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ContractUpdateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

public final class ContractUpdateTransaction extends TransactionBuilder<ContractUpdateTransaction> {
    private final ContractUpdateTransactionBody.Builder builder;

    public ContractUpdateTransaction() {
        builder = ContractUpdateTransactionBody.newBuilder();
    }

    public ContractUpdateTransaction setContractId(ContractId contractId) {
        builder.setContractID(contractId.toProtobuf());
        return this;
    }

    public ContractUpdateTransaction setExpirationTime(Instant expirationTime) {
        builder.setExpirationTime(InstantConverter.toProtobuf(expirationTime));
        return this;
    }

    public ContractUpdateTransaction setAdminKey(Key adminKey) {
        builder.setAdminKey(adminKey.toKeyProtobuf());
        return this;
    }

    public ContractUpdateTransaction setProxyAccountId(AccountId proxyAccountId) {
        builder.setProxyAccountID(proxyAccountId.toProtobuf());
        return this;
    }

    public ContractUpdateTransaction setAutoRenewPeriod(Duration autoRenewPeriod) {
        builder.setAutoRenewPeriod(DurationConverter.toProtobuf(autoRenewPeriod));
        return this;
    }

    public ContractUpdateTransaction setByteCodeFileId(FileId byteCodeFileId) {
        builder.setFileID(byteCodeFileId.toProtobuf());
        return this;
    }

    public ContractUpdateTransaction setContractMemo(String memo) {
        builder.setMemo(memo);
        return this;
    }

    @Override
    void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setContractUpdateInstance(builder);
    }
}
