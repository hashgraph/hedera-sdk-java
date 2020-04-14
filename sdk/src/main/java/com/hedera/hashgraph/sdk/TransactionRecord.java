package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class TransactionRecord {
    public final TransactionReceipt receipt;

    public final byte[] transactionHash;

    public final Instant consensusTimestamp;

    public final TransactionId transactionId;

    public final String transactionMemo;

    public final Hbar transactionFee;

    @Nullable
    public final ContractFunctionResult contractFunctionResult;

    public final List<Transfer> transfers;

    private TransactionRecord(
        TransactionReceipt transactionReceipt,
        byte[] transactionHash,
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
            transactionRecord.getTransactionHash().toByteArray(),
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
