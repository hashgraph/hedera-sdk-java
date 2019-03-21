package com.hedera.sdk;

import java.time.Instant;
import javax.annotation.Nonnull;

public class AdminDeleteTransaction extends TransactionBuilder<AdminDeleteTransaction> {
    public AdminDeleteTransaction() {}

    public final AdminDeleteTransaction setID(@Nonnull FileId fileId) {
        inner.getBodyBuilder().getAdminDeleteBuilder().setFileID(fileId.inner);
        return this;
    }

    public final AdminDeleteTransaction setID(@Nonnull ContractId contractId) {
        inner.getBodyBuilder().getAdminDeleteBuilder().setContractID(contractId.inner);
        return this;
    }

    public final AdminDeleteTransaction setExpirationTime(@Nonnull Instant timestamp) {
        inner.getBodyBuilder()
                .getAdminDeleteBuilder()
                .setExpirationTime(TimestampHelper.timestampSecondsFrom(timestamp));
        return this;
    }
}
