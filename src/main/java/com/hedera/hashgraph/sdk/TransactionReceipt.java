package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.consensus.TopicId;
import com.hedera.hashgraph.sdk.contract.ContractId;
import com.hedera.hashgraph.sdk.file.FileId;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseCodeEnum;

public final class TransactionReceipt {
    private final com.hedera.hashgraph.sdk.proto.TransactionReceipt inner;

    TransactionReceipt(Response response) {
        if (!response.hasTransactionGetReceipt()) {
            throw new IllegalArgumentException("response was not `TransactionGetReceipt`");
        }

        inner = response.getTransactionGetReceipt()
            .getReceipt();
    }

    TransactionReceipt(com.hedera.hashgraph.sdk.proto.TransactionReceipt inner) {
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

    public TopicId getTopicId() {
        if (!inner.hasTopicID()) {
            throw new IllegalStateException("receipt does not contain an topic ID");
        }

        return new TopicId(inner.getTopicIDOrBuilder());
    }

    public com.hedera.hashgraph.sdk.proto.TransactionReceipt toProto() {
        return inner;
    }
}
