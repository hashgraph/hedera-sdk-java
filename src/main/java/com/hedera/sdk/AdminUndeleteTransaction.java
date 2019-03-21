package com.hedera.sdk;

import javax.annotation.Nonnull;

public class AdminUndeleteTransaction extends TransactionBuilder<AdminUndeleteTransaction> {
    public final AdminUndeleteTransaction setID(@Nonnull FileId fileId) {
        inner.getBodyBuilder().getAdminUndeleteBuilder().setFileID(fileId.inner);
        return this;
    }

    public final AdminUndeleteTransaction setID(@Nonnull ContractId contractId) {
        inner.getBodyBuilder().getAdminUndeleteBuilder().setContractID(contractId.inner);
        return this;
    }
}
