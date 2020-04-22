package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.ByteString;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

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

    private TransactionRecord(
        TransactionReceipt transactionReceipt,
        ByteString transactionHash,
        Instant consensusTimestamp,
        TransactionId transactionId,
        String transactionMemo,
        long transactionFee,
        @Nullable ContractFunctionResult contractFunctionResult,
        List<Transfer> transfers
    ) {
        this.receipt = transactionReceipt;
        this.transactionHash = transactionHash;
        this.consensusTimestamp = consensusTimestamp;
        this.transactionMemo = transactionMemo;
        this.transactionId = transactionId;
        this.transfers = transfers;
        this.contractFunctionResult = contractFunctionResult;
        this.transactionFee = Hbar.fromTinybar(transactionFee);
    }

    static TransactionRecord fromProtobuf(com.hedera.hashgraph.sdk.proto.TransactionRecord transactionRecord) {
        var transfers = new ArrayList<Transfer>(transactionRecord.getTransferList().getAccountAmountsCount());
        for (var accountAmount : transactionRecord.getTransferList().getAccountAmountsList()) {
            transfers.add(Transfer.fromProtobuf(accountAmount));
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
            TransactionId.fromProtobuf(transactionRecord.getTransactionID()),
            transactionRecord.getMemo(),
            transactionRecord.getTransactionFee(),
            contractFunctionResult,
            transfers
        );
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("receipt", receipt)
            .add("transactionHash", transactionHash)
            .add("consensusTimestamp", consensusTimestamp)
            .add("transactionId", transactionId)
            .add("transactionMemo", transactionMemo)
            .add("transactionFee", transactionFee)
            .add("contractFunctionResult", contractFunctionResult)
            .add("transfers", transfers)
            .toString();
    }
}
