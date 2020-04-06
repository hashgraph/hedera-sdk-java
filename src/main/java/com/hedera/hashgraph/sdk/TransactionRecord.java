package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import org.threeten.bp.Instant;

import java.util.ArrayList;
import java.util.List;

public final class TransactionRecord {
    public final TransactionReceipt receipt;

    public final byte[] transactionHash;

    public final Instant consensusTimestamp;

    public final TransactionId transactionId;

    public final String memo;

    public final long transactionFee;

    public final Void contractFunctionResult;

    public final List<Transfer> transfers;

    private TransactionRecord(
        TransactionReceipt transactionReceipt,
        byte[] transactionHash,
        Instant consensusTimestamp,
        TransactionId transactionId,
        String memo,
        long transactionFee,
        Void contractFunctionResult,
        List<Transfer> transfers
    ) {
        this.receipt = transactionReceipt;
        this.transactionHash = transactionHash;
        this.consensusTimestamp = consensusTimestamp;
        this.memo = memo;
        this.transactionId = transactionId;
        this.transfers = transfers;
        this.contractFunctionResult = contractFunctionResult;
        this.transactionFee = transactionFee;
    }

    static TransactionRecord fromProtobuf(com.hedera.hashgraph.sdk.proto.TransactionRecord transactionRecord) {
        var transfers = new ArrayList<Transfer>(transactionRecord.getTransferList().getAccountAmountsCount());
        for (var accountAmount : transactionRecord.getTransferList().getAccountAmountsList()) {
            transfers.add(Transfer.fromProtobuf(accountAmount));
        }

        return new TransactionRecord(
            TransactionReceipt.fromProtobuf(transactionRecord.getReceipt()),
            transactionRecord.getTransactionHash().toByteArray(),
            InstantConverter.fromProtobuf(transactionRecord.getConsensusTimestamp()),
            TransactionId.fromProtobuf(transactionRecord.getTransactionID()),
            transactionRecord.getMemo(),
            transactionRecord.getTransactionFee(),
            null,
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
            .add("memo", memo)
            .add("transactionFee", transactionFee)
            .add("contractFunctionResult", contractFunctionResult)
            .add("transfers", transfers)
            .toString();
    }
}
