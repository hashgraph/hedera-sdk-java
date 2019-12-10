package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hashgraph.sdk.contract.ContractId;
import com.hedera.hashgraph.sdk.file.FileId;
import com.hederahashgraph.api.proto.java.Response;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

public final class TransactionReceipt {
    public final ResponseCodeEnum status;

    private final com.hederahashgraph.api.proto.java.TransactionReceipt inner;

    TransactionReceipt(Response response) {
        if (!response.hasTransactionGetReceipt()) {
            throw new IllegalArgumentException("response was not `TransactionGetReceipt`");
        }

        inner = response.getTransactionGetReceipt()
            .getReceipt();

        status = inner.getStatus();
    }

    TransactionReceipt(com.hederahashgraph.api.proto.java.TransactionReceipt inner) {
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

    public ConsensusTopicId getConsensusTopicId() {
        // Should be present for [ConsensusTopicCreateTransaction]
        if (!inner.hasTopicID()) {
            throw new IllegalStateException("receipt does not contain a topic ID");
        }

        return new ConsensusTopicId(inner.getTopicIDOrBuilder());
    }

    public long getConsensusTopicSequenceNumber() {
        // Should be present for [ConsensusTopicCreateTransaction]
        // FIXME[@mike-burrage-hedera]: Should this bail if there is no Topic ID

        return inner.getTopicSequenceNumber();
    }

    public byte[] getConsensusTopicRunningHash() {
        // Should be present for [ConsensusTopicCreateTransaction]
        // FIXME[@mike-burrage-hedera]: Should this bail if there is no Topic ID

        return inner.getTopicRunningHash().toByteArray();
    }

    public com.hederahashgraph.api.proto.java.TransactionReceipt toProto() {
        return inner;
    }
}
