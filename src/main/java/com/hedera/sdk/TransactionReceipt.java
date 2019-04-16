package com.hedera.sdk;

import com.hedera.sdk.account.AccountId;
import com.hedera.sdk.contract.ContractId;
import com.hedera.sdk.file.FileId;
import com.hedera.sdk.proto.Response;
import com.hedera.sdk.proto.ResponseCodeEnum;

import javax.annotation.Nullable;

public final class TransactionReceipt {
    private final com.hedera.sdk.proto.TransactionReceipt inner;

    TransactionReceipt(Response response) {
        if (!response.hasTransactionGetReceipt()) {
            throw new IllegalArgumentException("response was not `TransactionGetReceipt`");
        }

        inner = response.getTransactionGetReceipt()
            .getReceipt();
    }

    TransactionReceipt(com.hedera.sdk.proto.TransactionReceipt inner) {
        this.inner = inner;
    }

    public ResponseCodeEnum getStatus() {
        return inner.getStatus();
    }

    @Nullable
    public AccountId getAccountId() {
        return inner.hasAccountID() ? new AccountId(inner.getAccountIDOrBuilder()) : null;
    }

    @Nullable
    public FileId getFileId() {
        return inner.hasFileID() ? new FileId(inner.getFileIDOrBuilder()) : null;
    }

    @Nullable
    public ContractId getContractId() {
        return inner.hasContractID() ? new ContractId(inner.getContractIDOrBuilder()) : null;
    }

    public com.hedera.sdk.proto.TransactionReceipt toProto() {
        return inner;
    }
}
