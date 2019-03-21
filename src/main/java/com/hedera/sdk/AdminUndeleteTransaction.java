package com.hedera.sdk;

import javax.annotation.Nonnull;

public class AdminUndeleteTransaction extends TransactionBuilder<AdminUndeleteTransaction> {
    // Undelete a File
    public AdminUndeleteTransaction(@Nonnull FileId fileId) {
        inner.getBodyBuilder().getAdminUndeleteBuilder().setFileID(fileId.inner);
    }

    // Undelete a Contract
    public AdminUndeleteTransaction(@Nonnull ContractId contractId) {
        inner.getBodyBuilder().getAdminUndeleteBuilder().setContractID(contractId.inner);
    }
}
