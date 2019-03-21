package com.hedera.sdk;

public class AdminUndeleteTransaction extends TransactionBuilder<AdminUndeleteTransaction> {
    public final AdminUndeleteTransaction setID(FileId fileId) {
        inner.getBodyBuilder().getAdminUndeleteBuilder().setFileID(fileId.inner);
        return this;
    }

    public final AdminUndeleteTransaction setID(ContractId contractId) {
        inner.getBodyBuilder().getAdminUndeleteBuilder().setContractID(contractId.inner);
        return this;
    }
}
