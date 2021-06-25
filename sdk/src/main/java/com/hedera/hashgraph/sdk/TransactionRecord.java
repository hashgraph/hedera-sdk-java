package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.AccountAmount;
import com.hedera.hashgraph.sdk.proto.TokenTransferList;
import com.hedera.hashgraph.sdk.proto.TransferList;
import org.bouncycastle.util.encoders.Hex;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The complete record for a transaction on Hedera that has reached consensus.
 * <p>
 * This is not-free to request and is available for 1 hour after a transaction reaches consensus.
 * <p>
 * A {@link TransactionReceipt} can be thought of as a light-weight record which is free to ask for if you just
 * need what it contains. A receipt however lasts for only 180 seconds.
 */
public final class TransactionRecord {
    /**
     * The status (reach consensus, or failed, or is unknown) and the ID of
     * any new account/file/instance created.
     */
    public final TransactionReceipt receipt;

    /**
     * The hash of the Transaction that executed (not the hash of any Transaction that failed for
     * having a duplicate TransactionID).
     */
    public final ByteString transactionHash;

    /**
     * The consensus timestamp (or null if didn't reach consensus yet).
     */
    public final Instant consensusTimestamp;

    /**
     * The ID of the transaction this record represents.
     */
    public final TransactionId transactionId;

    /**
     * The memo that was submitted as part of the transaction (max 100 bytes).
     */
    public final String transactionMemo;

    /**
     * The actual transaction fee charged, not the original
     * transactionFee value from TransactionBody.
     */
    public final Hbar transactionFee;

    /**
     * Record of the value returned by the smart contract
     * function or constructor.
     */
    @Nullable
    public final ContractFunctionResult contractFunctionResult;

    /**
     * All hbar transfers as a result of this transaction, such as fees, or
     * transfers performed by the transaction, or by a smart contract it calls,
     * or by the creation of threshold records that it triggers.
     */
    public final List<Transfer> transfers;

    public final Map<TokenId, Map<AccountId, Long>> tokenTransfers;

    @Nullable
    public final ScheduleId scheduleRef;

    private TransactionRecord(
        TransactionReceipt transactionReceipt,
        ByteString transactionHash,
        Instant consensusTimestamp,
        TransactionId transactionId,
        String transactionMemo,
        long transactionFee,
        @Nullable ContractFunctionResult contractFunctionResult,
        List<Transfer> transfers,
        Map<TokenId, Map<AccountId, Long>> tokenTransfers,
        @Nullable ScheduleId scheduleRef
    ) {
        this.receipt = transactionReceipt;
        this.transactionHash = transactionHash;
        this.consensusTimestamp = consensusTimestamp;
        this.transactionMemo = transactionMemo;
        this.transactionId = transactionId;
        this.transfers = transfers;
        this.contractFunctionResult = contractFunctionResult;
        this.transactionFee = Hbar.fromTinybars(transactionFee);
        this.tokenTransfers = tokenTransfers;
        this.scheduleRef = scheduleRef;
    }

    static TransactionRecord fromProtobuf(com.hedera.hashgraph.sdk.proto.TransactionRecord transactionRecord) {
        return TransactionRecord.fromProtobuf(transactionRecord, null);
    }

    static TransactionRecord fromProtobuf(com.hedera.hashgraph.sdk.proto.TransactionRecord transactionRecord, @Nullable NetworkName networkName) {
        var transfers = new ArrayList<Transfer>(transactionRecord.getTransferList().getAccountAmountsCount());
        for (var accountAmount : transactionRecord.getTransferList().getAccountAmountsList()) {
            transfers.add(Transfer.fromProtobuf(accountAmount, networkName));
        }

        var tokenTransfers = new HashMap<TokenId, Map<AccountId, Long>>(transactionRecord.getTokenTransferListsCount());
        for (var tokenTransfersList : transactionRecord.getTokenTransferListsList()) {
            var accountAmounts = new HashMap<AccountId, Long>();
            for (var accountAmount : tokenTransfersList.getTransfersList()) {
                accountAmounts.put(AccountId.fromProtobuf(accountAmount.getAccountID(), networkName), accountAmount.getAmount());
            }

            tokenTransfers.put(TokenId.fromProtobuf(tokenTransfersList.getToken(), networkName), accountAmounts);
        }

        // HACK: This is a bit bad, any takers to clean this up
        var contractFunctionResult = transactionRecord.hasContractCallResult() ?
            new ContractFunctionResult(transactionRecord.getContractCallResult()) :
            transactionRecord.hasContractCreateResult() ?
                new ContractFunctionResult(transactionRecord.getContractCreateResult()) :
                null;

        return new TransactionRecord(
            TransactionReceipt.fromProtobuf(transactionRecord.getReceipt()),
            transactionRecord.getTransactionHash(),
            InstantConverter.fromProtobuf(transactionRecord.getConsensusTimestamp()),
            TransactionId.fromProtobuf(transactionRecord.getTransactionID(), networkName),
            transactionRecord.getMemo(),
            transactionRecord.getTransactionFee(),
            contractFunctionResult,
            transfers,
            tokenTransfers,
            transactionRecord.hasScheduleRef() ? ScheduleId.fromProtobuf(transactionRecord.getScheduleRef(), networkName) : null
        );
    }

    public static TransactionRecord fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.TransactionRecord.parseFrom(bytes).toBuilder().build());
    }

    com.hedera.hashgraph.sdk.proto.TransactionRecord toProtobuf() {
        var transferList = TransferList.newBuilder();
        for (Transfer transfer : transfers) {
            transferList.addAccountAmounts(transfer.toProtobuf());
        }

        var transactionRecord = com.hedera.hashgraph.sdk.proto.TransactionRecord.newBuilder()
            .setReceipt(receipt.toProtobuf())
            .setTransactionHash(transactionHash)
            .setConsensusTimestamp(InstantConverter.toProtobuf(consensusTimestamp))
            .setTransactionID(transactionId.toProtobuf())
            .setMemo(transactionMemo)
            .setTransactionFee(transactionFee.toTinybars())
            .setTransferList(transferList);

        for (var tokenEntry : tokenTransfers.entrySet()) {
            var tokenTransfersList = TokenTransferList.newBuilder()
                .setToken(tokenEntry.getKey().toProtobuf());
            for (var aaEntry : tokenEntry.getValue().entrySet()) {
                tokenTransfersList.addTransfers(AccountAmount.newBuilder()
                    .setAccountID(aaEntry.getKey().toProtobuf())
                    .setAmount(aaEntry.getValue()).build()
                );
            }

            transactionRecord.addTokenTransferLists(tokenTransfersList);
        }

        if (contractFunctionResult != null) {
            transactionRecord.setContractCallResult(contractFunctionResult.toProtobuf());
        }

        if (scheduleRef != null) {
            transactionRecord.setScheduleRef(scheduleRef.toProtobuf());
        }

        return transactionRecord.build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("receipt", receipt)
            .add("transactionHash", Hex.toHexString(transactionHash.toByteArray()))
            .add("consensusTimestamp", consensusTimestamp)
            .add("transactionId", transactionId)
            .add("transactionMemo", transactionMemo)
            .add("transactionFee", transactionFee)
            .add("contractFunctionResult", contractFunctionResult)
            .add("transfers", transfers)
            .add("tokenTransfers", tokenTransfers)
            .add("scheduleRef", scheduleRef)
            .toString();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
