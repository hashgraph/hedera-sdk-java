package com.hedera.sdk;

import java.time.Instant;

public final class AdminDeleteTransaction extends TransactionBuilder<AdminDeleteTransaction> {
    public AdminDeleteTransaction setID(FileId fileId) {
        inner.getBodyBuilder().getAdminDeleteBuilder().setFileID(fileId.inner);
        return this;
    }

    public AdminDeleteTransaction setID(ContractId contractId) {
        inner.getBodyBuilder().getAdminDeleteBuilder().setContractID(contractId.inner);
        return this;
    }

    public AdminDeleteTransaction setExpirationTime(Instant timestamp) {
        inner.getBodyBuilder()
                .getAdminDeleteBuilder()
                .setExpirationTime(TimestampHelper.timestampSecondsFrom(timestamp));
        return this;
    }
}
