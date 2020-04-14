package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;

import javax.annotation.Nullable;

public final class TransactionReceipt {
    public final Status status;

    public final ExchangeRate exchangeRate;

    @Nullable
    public final AccountId accountId;

    @Nullable
    public final FileId fileId;

    @Nullable
    public final ContractId contractId;

    @Nullable
    public final TopicId topicId;

    @Nullable
    public final Long topicSequenceNumber;

    @Nullable
    public final byte[] topicRunningHash;

    private TransactionReceipt(
        Status status,
        ExchangeRate exchangeRate,
        @Nullable AccountId accountId,
        @Nullable FileId fileId,
        @Nullable ContractId contractId,
        @Nullable TopicId topicId,
        @Nullable Long topicSequenceNumber,
        @Nullable byte[] topicRunningHash
    ) {
        this.status = status;
        this.exchangeRate = exchangeRate;
        this.accountId = accountId;
        this.fileId = fileId;
        this.contractId = contractId;
        this.topicId = topicId;
        this.topicSequenceNumber = topicSequenceNumber;
        this.topicRunningHash = topicRunningHash;
    }

    static TransactionReceipt fromProtobuf(com.hedera.hashgraph.sdk.proto.TransactionReceipt transactionReceipt) {
        var status = Status.valueOf(transactionReceipt.getStatus());

        var rate = transactionReceipt.getExchangeRate();
        var exchangeRate = ExchangeRate.fromProtobuf(rate.getCurrentRate());

        var accountId =
            transactionReceipt.hasAccountID()
                ? AccountId.fromProtobuf(transactionReceipt.getAccountID())
                : null;

        var fileId =
            transactionReceipt.hasFileID()
                ? FileId.fromProtobuf(transactionReceipt.getFileID())
                : null;

        var contractId =
            transactionReceipt.hasContractID()
                ? ContractId.fromProtobuf(transactionReceipt.getContractID())
                : null;

        var topicId =
            transactionReceipt.hasTopicID()
                ? TopicId.fromProtobuf(transactionReceipt.getTopicID())
                : null;

        var topicSequenceNumber =
            transactionReceipt.getTopicSequenceNumber() == 0
                ? null
                : transactionReceipt.getTopicSequenceNumber();

        var topicRunningHash =
            transactionReceipt.getTopicRunningHash().isEmpty()
                ? null
                : transactionReceipt.getTopicRunningHash().toByteArray();

        return new TransactionReceipt(
            status,
            exchangeRate,
            accountId,
            fileId,
            contractId,
            topicId,
            topicSequenceNumber,
            topicRunningHash
        );
    }

    public AccountId getAccountId() {
        if (this.accountId == null) {
            throw new IllegalStateException("receipt does not contain an account ID");
        }

        return this.accountId;
    }

    public FileId getFileId() {
        if (this.fileId == null) {
            throw new IllegalStateException("receipt does not contain an file ID");
        }

        return this.fileId;
    }

    public ContractId getContractId() {
        if (this.contractId == null) {
            throw new IllegalStateException("receipt does not contain an contract ID");
        }

        return this.contractId;
    }

    public TopicId getTopicId() {
        if (this.topicId == null) {
            throw new IllegalStateException("receipt does not contain an topic ID");
        }

        return this.topicId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("status", status)
            .add("exchangeRate", exchangeRate)
            .add("accountId", accountId)
            .add("fileId", fileId)
            .add("contractId", contractId)
            .add("topicId", topicId)
            .add("topicSequenceNumber", topicSequenceNumber)
            .toString();
    }
}
