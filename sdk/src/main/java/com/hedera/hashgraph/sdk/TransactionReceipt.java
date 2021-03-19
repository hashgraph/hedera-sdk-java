package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.ExchangeRateSet;
import com.hedera.hashgraph.sdk.proto.ScheduleID;
import com.hedera.hashgraph.sdk.proto.TimestampSeconds;

import javax.annotation.Nullable;

/**
 * The consensus result for a transaction, which might not be currently
 * known, or may succeed or fail.
 */
public final class TransactionReceipt {
    /**
     * Whether the transaction succeeded or failed (or is unknown).
     */
    public final Status status;

    /**
     * The exchange rate of Hbars to cents (USD).
     */
    public final ExchangeRate exchangeRate;

    /**
     * The account ID, if a new account was created.
     */
    @Nullable
    public final AccountId accountId;

    /**
     * The file ID, if a new file was created.
     */
    @Nullable
    public final FileId fileId;

    /**
     * The contract ID, if a new contract was created.
     */
    @Nullable
    public final ContractId contractId;

    /**
     * The topic ID, if a new topic was created.
     */
    @Nullable
    public final TopicId topicId;

    /**
     * The token ID, if a new token was created.
     */
    @Nullable
    public final TokenId tokenId;

    /**
     * Updated sequence number for a consensus service topic.
     * Set for {@link TopicMessageSubmitTransaction}.
     */
    @Nullable
    public final Long topicSequenceNumber;

    /**
     * Updated running hash for a consensus service topic.
     * Set for {@link TopicMessageSubmitTransaction}.
     */
    @Nullable
    public final ByteString topicRunningHash;

    public final Long totalSupply;

    @Nullable
    public final ScheduleId scheduleId;

    @Nullable
    public final TransactionId scheduledTransactionId;

    private TransactionReceipt(
        Status status,
        ExchangeRate exchangeRate,
        @Nullable AccountId accountId,
        @Nullable FileId fileId,
        @Nullable ContractId contractId,
        @Nullable TopicId topicId,
        @Nullable TokenId tokenId,
        @Nullable Long topicSequenceNumber,
        @Nullable ByteString topicRunningHash,
        Long totalSupply,
        @Nullable ScheduleId scheduleId,
        @Nullable TransactionId scheduledTransactionId
    ) {
        this.status = status;
        this.exchangeRate = exchangeRate;
        this.accountId = accountId;
        this.fileId = fileId;
        this.contractId = contractId;
        this.topicId = topicId;
        this.tokenId = tokenId;
        this.topicSequenceNumber = topicSequenceNumber;
        this.topicRunningHash = topicRunningHash;
        this.totalSupply = totalSupply;
        this.scheduleId = scheduleId;
        this.scheduledTransactionId = scheduledTransactionId;
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

        var tokenId =
            transactionReceipt.hasTokenID()
                ? TokenId.fromProtobuf(transactionReceipt.getTokenID())
                : null;

        var topicSequenceNumber =
            transactionReceipt.getTopicSequenceNumber() == 0
                ? null
                : transactionReceipt.getTopicSequenceNumber();

        var topicRunningHash =
            transactionReceipt.getTopicRunningHash().isEmpty()
                ? null
                : transactionReceipt.getTopicRunningHash();

        var totalSupply = transactionReceipt.getNewTotalSupply();

        var scheduleId =
            transactionReceipt.hasScheduleID()
                ? ScheduleId.fromProtobuf(transactionReceipt.getScheduleID())
                : null;

        var scheduledTransactionId =
            transactionReceipt.hasScheduledTransactionID()
                ? TransactionId.fromProtobuf(transactionReceipt.getScheduledTransactionID())
                : null;

        return new TransactionReceipt(
            status,
            exchangeRate,
            accountId,
            fileId,
            contractId,
            topicId,
            tokenId,
            topicSequenceNumber,
            topicRunningHash,
            totalSupply,
            scheduleId,
            scheduledTransactionId
        );
    }

    public static TransactionReceipt fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.TransactionReceipt.parseFrom(bytes).toBuilder().build());
    }

    com.hedera.hashgraph.sdk.proto.TransactionReceipt toProtobuf() {
        var transactionReceiptBuilder = com.hedera.hashgraph.sdk.proto.TransactionReceipt.newBuilder()
            .setStatus(status.code)
            .setExchangeRate(ExchangeRateSet.newBuilder()
                .setCurrentRate(com.hedera.hashgraph.sdk.proto.ExchangeRate.newBuilder()
                    .setHbarEquiv(exchangeRate.hbars)
                    .setCentEquiv(exchangeRate.cents)
                    .setExpirationTime(TimestampSeconds.newBuilder()
                        .setSeconds(exchangeRate.expirationTime.getEpochSecond())
                    )
                )
            )
            .setNewTotalSupply(totalSupply);

        if (accountId != null) {
            transactionReceiptBuilder.setAccountID(accountId.toProtobuf());
        }

        if (fileId != null) {
            transactionReceiptBuilder.setFileID(fileId.toProtobuf());
        }

        if (contractId != null) {
            transactionReceiptBuilder.setContractID(contractId.toProtobuf());
        }

        if (topicId != null) {
            transactionReceiptBuilder.setTopicID(topicId.toProtobuf());
        }

        if (tokenId != null) {
            transactionReceiptBuilder.setTokenID(tokenId.toProtobuf());
        }

        if (topicSequenceNumber != null) {
            transactionReceiptBuilder.setTopicSequenceNumber(topicSequenceNumber);
        }

        if (topicRunningHash != null) {
            transactionReceiptBuilder.setTopicRunningHash(topicRunningHash);
        }

        if (scheduleId != null) {
            transactionReceiptBuilder.setScheduleID(scheduleId.toProtobuf());
        }

        if (scheduledTransactionId != null) {
            transactionReceiptBuilder.setScheduledTransactionID(scheduledTransactionId.toProtobuf());
        }

        return transactionReceiptBuilder.build();
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
            .add("tokenId", tokenId)
            .add("topicSequenceNumber", topicSequenceNumber)
            .add("topicRunningHash", topicRunningHash)
            .add("totalSupply", totalSupply)
            .add("scheduleId", scheduleId)
            .toString();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
