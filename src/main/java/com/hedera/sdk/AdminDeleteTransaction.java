package com.hedera.sdk;

import com.hedera.sdk.proto.ContractID;
import javax.annotation.Nonnull;

public class AdminDeleteTransaction extends TransactionBodyBuilder<AdminDeleteTransaction> {
    // Transaction to delete a File
    public AdminDeleteTransaction(@Nonnull FileId fileID) {
        inner.getAdminDeleteBuilder().setFileID(fileID.inner);
    }

    // Transaction to delete a contract
    public AdminDeleteTransaction(@Nonnull ContractID contractID) {
        inner.getAdminDeleteBuilder().setContractID(contractID);
    }

    public final AdminDeleteTransaction setExpirationTime(@Nonnull TimestampSeconds timestamp) {
        inner.getAdminDeleteBuilder().setExpirationTime(timestamp.inner);
        return this;
    }
}
