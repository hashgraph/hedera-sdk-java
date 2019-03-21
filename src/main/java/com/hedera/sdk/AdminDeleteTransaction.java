package com.hedera.sdk;

import java.time.Instant;

public class AdminDeleteTransaction extends TransactionBuilder<AdminDeleteTransaction> {
    public final AdminDeleteTransaction setID(FileId fileId) {
        inner.getBodyBuilder().getAdminDeleteBuilder().setFileID(fileId.inner);
        return this;
    }

    public final AdminDeleteTransaction setID(ContractId contractId) {
        inner.getBodyBuilder().getAdminDeleteBuilder().setContractID(contractId.inner);
        return this;
    }

    public final AdminDeleteTransaction setExpirationTime(Instant timestamp) {
        inner.getBodyBuilder()
                .getAdminDeleteBuilder()
                .setExpirationTime(TimestampHelper.timestampSecondsFrom(timestamp));
        return this;
    }
}
