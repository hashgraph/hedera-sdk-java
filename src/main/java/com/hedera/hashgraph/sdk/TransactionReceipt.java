package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.proto.ResponseCodeEnum;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.contract.ContractId;
import com.hedera.hashgraph.sdk.file.FileId;

public final class TransactionReceipt {
    public final ResponseCodeEnum status;

    private final com.hedera.hashgraph.proto.TransactionReceipt inner;

    TransactionReceipt(Response response) {
        if (!response.hasTransactionGetReceipt()) {
            throw new IllegalArgumentException("response was not `TransactionGetReceipt`");
        }

        inner = response.getTransactionGetReceipt()
            .getReceipt();

        status = inner.getStatus();
    }

    TransactionReceipt(com.hedera.hashgraph.proto.TransactionReceipt inner) {
        this.inner = inner;

        status = inner.getStatus();
    }

    /**
     * @deprecated use {@link #status} instead.
     */
    @Deprecated
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

    public com.hedera.hashgraph.proto.TransactionReceipt toProto() {
        return inner;
    }
}
