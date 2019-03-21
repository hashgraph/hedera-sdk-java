package com.hedera.sdk;

public final class AdminUndeleteTransaction extends TransactionBuilder<AdminUndeleteTransaction> {
    public AdminUndeleteTransaction setID(FileId fileId) {
        inner.getBodyBuilder().getAdminUndeleteBuilder().setFileID(fileId.inner);
        return this;
    }

    public AdminUndeleteTransaction setID(ContractId contractId) {
        inner.getBodyBuilder().getAdminUndeleteBuilder().setContractID(contractId.inner);
        return this;
    }
}
