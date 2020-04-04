package com.hedera.hashgraph.sdk;

import java8.util.Optional;

public final class TransactionReceipt {
    public final Status status;

    public final ExchangeRate currentExchangeRate;

    public final ExchangeRate nextExchangeRate;

    public final Optional<AccountId> accountId;

    public final Optional<FileId> fileId;

    public final Optional<ContractId> contractId;

    public final Optional<TopicId> topicId;

    public final Optional<Long> topicSequenceNumber;

    public final Optional<byte[]> topicRunningHash;

    TransactionReceipt(
            Status status,
            ExchangeRate currentExchangeRate,
            ExchangeRate nextExchangeRate,
            Optional<AccountId> accountId,
            Optional<FileId> fileId,
            Optional<ContractId> contractId,
            Optional<TopicId> topicId,
            Optional<Long> topicSequenceNumber,
            Optional<byte[]> topicRunningHash) {
        this.status = status;
        this.currentExchangeRate = currentExchangeRate;
        this.nextExchangeRate = nextExchangeRate;
        this.accountId = accountId;
        this.fileId = fileId;
        this.contractId = contractId;
        this.topicId = topicId;
        this.topicSequenceNumber = topicSequenceNumber;
        this.topicRunningHash = topicRunningHash;
    }

    static TransactionReceipt fromProtobuf(com.hedera.hashgraph.sdk.proto.TransactionReceipt pb) {
        var status = Status.valueOf(pb.getStatus());

        var rate = pb.getExchangeRate();
        var currentExchangeRate = ExchangeRate.fromProtobuf(rate.getCurrentRate());
        var nextExchangeRate = ExchangeRate.fromProtobuf(rate.getNextRate());

        var accountId = Optional.ofNullable(pb.getAccountID()).map(AccountId::fromProtobuf);
        var fileId = Optional.ofNullable(pb.getFileID()).map(FileId::fromProtobuf);
        var contractId = Optional.ofNullable(pb.getContractID()).map(ContractId::fromProtobuf);
        var topicId = Optional.ofNullable(pb.getTopicID()).map(TopicId::fromProtobuf);

        var topicSequenceNumber =
                pb.getTopicSequenceNumber() == 0
                        ? Optional.<Long>empty()
                        : Optional.of(pb.getTopicSequenceNumber());

        var topicRunningHash =
                pb.getTopicRunningHash().isEmpty()
                        ? Optional.<byte[]>empty()
                        : Optional.of(pb.getTopicRunningHash().toByteArray());

        return new TransactionReceipt(
                status,
                currentExchangeRate,
                nextExchangeRate,
                accountId,
                fileId,
                contractId,
                topicId,
                topicSequenceNumber,
                topicRunningHash);
    }

    @Override
    public String toString() {
        return "TransactionReceipt{"
                + "status="
                + status
                + ", currentExchangeRate="
                + currentExchangeRate
                + ", nextExchangeRate="
                + nextExchangeRate
                + ", accountId="
                + accountId
                + ", fileId="
                + fileId
                + ", contractId="
                + contractId
                + ", topicId="
                + topicId
                + ", topicSequenceNumber="
                + topicSequenceNumber
                + ", topicRunningHash="
                + topicRunningHash
                + '}';
    }
}
