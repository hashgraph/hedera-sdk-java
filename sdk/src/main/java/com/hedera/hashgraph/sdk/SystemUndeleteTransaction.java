package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.SystemUndeleteTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;

/**
 * Undelete a file or smart contract that was deleted by AdminDelete - can only be done with a Hedera admin.
 */
public final class SystemUndeleteTransaction extends TransactionBuilder<SystemUndeleteTransaction> {
    private final SystemUndeleteTransactionBody.Builder builder;

    public SystemUndeleteTransaction() {
        builder = SystemUndeleteTransactionBody.newBuilder();
    }

    /**
     * Sets the file ID to undelete.
     *
     * Mutually exclusive with {@link #setContractId(ContractId)}.
     *
     * @return {@code this}
     * @param fileId The FileId to be set
     */
    public SystemUndeleteTransaction setFileId(FileId fileId) {
        builder.setFileID(fileId.toProtobuf());
        return this;
    }

    /**
     * Sets the contract ID to undelete.
     *
     * Mutually exclusive with {@link #setFileId(FileId)}.
     *
     * @return {@code this}
     * @param contractId The ContractId to be set
     */
    public SystemUndeleteTransaction setContractId(ContractId contractId) {
        builder.setContractID(contractId.toProtobuf());
        return this;
    }

    @Override
    void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setSystemUndelete(builder);
    }
}
