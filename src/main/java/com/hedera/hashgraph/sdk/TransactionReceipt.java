package com.hedera.hashgraph.sdk;

import java8.util.Optional;

public final class TransactionReceipt {
    public final Status status;

    public final ExchangeRate exchangeRate;

    public final Optional<AccountId> accountId;

    public final Optional<FileId> fileId;

    public final Optional<ContractId> contractId;

    public final Optional<TopicId> topicId;

    public final Optional<Long> topicSequenceNumber;

    public final Optional<byte[]> topicRunningHash;

    TransactionReceipt(
            Status status,
            ExchangeRate exchangeRate,
            Optional<AccountId> accountId,
            Optional<FileId> fileId,
            Optional<ContractId> contractId,
            Optional<TopicId> topicId,
            Optional<Long> topicSequenceNumber,
            Optional<byte[]> topicRunningHash) {
        this.status = status;
        this.exchangeRate = exchangeRate;
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
        var exchangeRate = ExchangeRate.fromProtobuf(rate.getCurrentRate());

        var accountId =
                pb.hasAccountID()
                        ? Optional.of(AccountId.fromProtobuf(pb.getAccountID()))
                        : Optional.<AccountId>empty();

        var fileId =
                pb.hasFileID()
                        ? Optional.of(FileId.fromProtobuf(pb.getFileID()))
                        : Optional.<FileId>empty();

        var contractId =
                pb.hasContractID()
                        ? Optional.of(ContractId.fromProtobuf(pb.getContractID()))
                        : Optional.<ContractId>empty();

        var topicId =
                pb.hasTopicID()
                        ? Optional.of(TopicId.fromProtobuf(pb.getTopicID()))
                        : Optional.<TopicId>empty();

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
                exchangeRate,
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
                + ", exchangeRate="
                + exchangeRate
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
