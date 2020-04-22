package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.SystemDeleteTransactionBody;
import com.hedera.hashgraph.sdk.proto.TimestampSeconds;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import org.threeten.bp.Instant;

/**
 * Delete a file or smart contract - can only be done with a Hedera admin.
 *
 * When it is deleted, it immediately disappears from the system as seen by the user,
 * but is still stored internally until the expiration time, at which time it
 * is truly and permanently deleted.
 *
 * Until that time, it can be undeleted by the Hedera admin.
 * When a smart contract is deleted, the cryptocurrency account within it continues
 * to exist, and is not affected by the expiration time here.
 */
public final class SystemDeleteTransaction extends TransactionBuilder<SystemDeleteTransaction> {
    private final SystemDeleteTransactionBody.Builder builder;

    public SystemDeleteTransaction() {
        builder = SystemDeleteTransactionBody.newBuilder();
    }

    /**
     * Sets the file ID to delete.
     *
     * Mutually exclusive with {@link #setContractId(ContractId)}.
     *
     * @return {@code this}
     */
    public SystemDeleteTransaction setFileId(FileId fileId) {
        builder.setFileID(fileId.toProtobuf());
        return this;
    }

    /**
     * Sets the contract ID to delete.
     *
     * Mutually exclusive with {@link #setFileId(FileId)}.
     *
     * @return {@code this}
     */
    public SystemDeleteTransaction setContractId(ContractId contractId) {
        builder.setContractID(contractId.toProtobuf());
        return this;
    }

    /**
     * Sets the timestamp at which the "deleted" file should
     * truly be permanently deleted.
     *
     * @return {@code this}
     */
    public SystemDeleteTransaction setExpirationTime(Instant expirationTime) {
        builder.setExpirationTime(TimestampSeconds.newBuilder()
            .setSeconds(expirationTime.getEpochSecond())
            .build());

        return this;
    }

    @Override
    void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setSystemDelete(builder);
    }
}
