package com.hedera.sdk;

import com.hedera.sdk.account.AccountId;
import com.hedera.sdk.contract.ContractId;
import com.hedera.sdk.file.FileId;
import com.hedera.sdk.proto.Response;
import com.hedera.sdk.proto.ResponseCodeEnum;

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

    public AccountId getAccountId() {
        if (!inner.hasAccountID()) {
            throw new IllegalStateException("receipt does not contain an account ID");
        }

        return new AccountId(inner.getAccountIDOrBuilder());
    }

    public FileId getFileId() {
        if (!inner.hasFileID()) {
            throw new IllegalStateException("receipt does not contain a file ID");
        }

        return new FileId(inner.getFileIDOrBuilder());
    }

    public ContractId getContractId() {
        if (!inner.hasContractID()) {
            throw new IllegalStateException("receipt does not contain a contract ID");
        }

        return new ContractId(inner.getContractIDOrBuilder());
    }

    public com.hedera.sdk.proto.TransactionReceipt toProto() {
        return inner;
    }
}
