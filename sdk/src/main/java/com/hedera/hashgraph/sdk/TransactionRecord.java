package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.AccountAmount;
import com.hedera.hashgraph.sdk.proto.NftTransfer;
import com.hedera.hashgraph.sdk.proto.TokenTransferList;
import com.hedera.hashgraph.sdk.proto.TransferList;
import org.bouncycastle.util.encoders.Hex;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;
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

    public final List<TokenTransfer> tokenTransferList;

    public final Map<TokenId, List<TokenNftTransfer>> tokenNftTransfers;

    @Nullable
    public final ScheduleId scheduleRef;

    public final List<AssessedCustomFee> assessedCustomFees;

    /**
     * All token associations implicitly created while handling this transaction
     */
    public final List<TokenAssociation> automaticTokenAssociations;

    @Nullable
    public final PublicKey aliasKey;

    public final List<TransactionRecord> children;

    public final List<TransactionRecord> duplicates;

    @Nullable
    public final Instant parentConsensusTimestamp;

    public final List<HbarAllowance> hbarAllowanceAdjustments;

    public final List<TokenAllowance> tokenAllowanceAdjustments;

    public final List<TokenNftAllowance> tokenNftAllowanceAdjustments;

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
        List<TokenTransfer> tokenTransferList,
        Map<TokenId, List<TokenNftTransfer>> tokenNftTransfers,
        @Nullable ScheduleId scheduleRef,
        List<AssessedCustomFee> assessedCustomFees,
        List<TokenAssociation> automaticTokenAssociations,
        @Nullable PublicKey aliasKey,
        List<TransactionRecord> children,
        List<TransactionRecord> duplicates,
        @Nullable Instant parentConsensusTimestamp,
        List<HbarAllowance> hbarAllowanceAdjustments,
        List<TokenAllowance> tokenAllowanceAdjustments,
        List<TokenNftAllowance> tokenNftAllowanceAdjustments
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
        this.tokenTransferList = tokenTransferList;
        this.tokenNftTransfers = tokenNftTransfers;
        this.scheduleRef = scheduleRef;
        this.assessedCustomFees = assessedCustomFees;
        this.automaticTokenAssociations = automaticTokenAssociations;
        this.aliasKey = aliasKey;
        this.children = children;
        this.duplicates = duplicates;
        this.parentConsensusTimestamp = parentConsensusTimestamp;
        this.hbarAllowanceAdjustments = hbarAllowanceAdjustments;
        this.tokenAllowanceAdjustments = tokenAllowanceAdjustments;
        this.tokenNftAllowanceAdjustments = tokenNftAllowanceAdjustments;
    }

    static TransactionRecord fromProtobuf(
        com.hedera.hashgraph.sdk.proto.TransactionRecord transactionRecord,
        List<TransactionRecord> children,
        List<TransactionRecord> duplicates
    ) {
        var transfers = new ArrayList<Transfer>(transactionRecord.getTransferList().getAccountAmountsCount());
        for (var accountAmount : transactionRecord.getTransferList().getAccountAmountsList()) {
            transfers.add(Transfer.fromProtobuf(accountAmount));
        }

        var tokenTransfers = new HashMap<TokenId, Map<AccountId, Long>>();
        var tokenNftTransfers = new HashMap<TokenId, List<TokenNftTransfer>>();

        var tokenTransfersList = TokenTransfer.fromProtobuf(transactionRecord.getTokenTransferListsList());
        var nftTransfersList = TokenNftTransfer.fromProtobuf(transactionRecord.getTokenTransferListsList());

        for (var transfer : tokenTransfersList) {
            var current = tokenTransfers.containsKey(transfer.tokenId) ? tokenTransfers.get(transfer.tokenId) : new HashMap<AccountId, Long>();
            current.put(transfer.accountId, transfer.amount);
            tokenTransfers.put(transfer.tokenId, current);
        }

        for (var transfer : nftTransfersList) {
            var current = tokenNftTransfers.containsKey(transfer.tokenId) ? tokenNftTransfers.get(transfer.tokenId) : new ArrayList<TokenNftTransfer>();
            current.add(transfer);
            tokenNftTransfers.put(transfer.tokenId, current);
        }

        var fees = new ArrayList<AssessedCustomFee>(transactionRecord.getAssessedCustomFeesCount());
        for (var fee : transactionRecord.getAssessedCustomFeesList()) {
            fees.add(AssessedCustomFee.fromProtobuf(fee));
        }

        // HACK: This is a bit bad, any takers to clean this up
        var contractFunctionResult = transactionRecord.hasContractCallResult() ?
            new ContractFunctionResult(transactionRecord.getContractCallResult()) :
            transactionRecord.hasContractCreateResult() ?
                new ContractFunctionResult(transactionRecord.getContractCreateResult()) :
                null;

        var automaticTokenAssociations = new ArrayList<TokenAssociation>(transactionRecord.getAutomaticTokenAssociationsCount());
        for (var tokenAssociation : transactionRecord.getAutomaticTokenAssociationsList()) {
            automaticTokenAssociations.add(TokenAssociation.fromProtobuf(tokenAssociation));
        }

        var aliasKey = PublicKey.fromAliasBytes(transactionRecord.getAlias());

        var hbarAllowanceAdjustments = new ArrayList<HbarAllowance>(transactionRecord.getCryptoAdjustmentsCount());
        for (var hbarAdjustmentProto : transactionRecord.getCryptoAdjustmentsList()) {
            hbarAllowanceAdjustments.add(HbarAllowance.fromProtobuf(hbarAdjustmentProto));
        }

        var tokenAllowanceAdjustments = new ArrayList<TokenAllowance>(transactionRecord.getTokenAdjustmentsCount());
        for (var tokenAdjustmentProto : transactionRecord.getTokenAdjustmentsList()) {
            tokenAllowanceAdjustments.add(TokenAllowance.fromProtobuf(tokenAdjustmentProto));
        }

        var tokenNftAllowanceAdjustments = new ArrayList<TokenNftAllowance>(transactionRecord.getNftAdjustmentsCount());
        for (var nftAdjustmentProto : transactionRecord.getNftAdjustmentsList()) {
            tokenNftAllowanceAdjustments.add(TokenNftAllowance.fromProtobuf(nftAdjustmentProto));
        }

        return new TransactionRecord(
            TransactionReceipt.fromProtobuf(transactionRecord.getReceipt()),
            transactionRecord.getTransactionHash(),
            InstantConverter.fromProtobuf(transactionRecord.getConsensusTimestamp()),
            TransactionId.fromProtobuf(transactionRecord.getTransactionID()),
            transactionRecord.getMemo(),
            transactionRecord.getTransactionFee(),
            contractFunctionResult,
            transfers,
            tokenTransfers,
            tokenTransfersList,
            tokenNftTransfers,
            transactionRecord.hasScheduleRef() ? ScheduleId.fromProtobuf(transactionRecord.getScheduleRef()) : null,
            fees,
            automaticTokenAssociations,
            aliasKey,
            children,
            duplicates,
            transactionRecord.hasParentConsensusTimestamp() ?
                InstantConverter.fromProtobuf(transactionRecord.getParentConsensusTimestamp()) : null,
            hbarAllowanceAdjustments,
            tokenAllowanceAdjustments,
            tokenNftAllowanceAdjustments
        );
    }

    static TransactionRecord fromProtobuf(com.hedera.hashgraph.sdk.proto.TransactionRecord transactionRecord) {
        return fromProtobuf(transactionRecord, new ArrayList<>(), new ArrayList<>());
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

        for (var nftEntry : tokenNftTransfers.entrySet()) {
            var nftTransferList = TokenTransferList.newBuilder()
                .setToken(nftEntry.getKey().toProtobuf());
            for (var aaEntry : nftEntry.getValue()) {
                nftTransferList.addNftTransfers(NftTransfer.newBuilder()
                    .setSenderAccountID(aaEntry.sender.toProtobuf())
                    .setReceiverAccountID(aaEntry.receiver.toProtobuf())
                    .setSerialNumber(aaEntry.serial).build());
            }

            transactionRecord.addTokenTransferLists(nftTransferList);
        }

        if (contractFunctionResult != null) {
            transactionRecord.setContractCallResult(contractFunctionResult.toProtobuf());
        }

        if (scheduleRef != null) {
            transactionRecord.setScheduleRef(scheduleRef.toProtobuf());
        }

        for (var fee : assessedCustomFees) {
            transactionRecord.addAssessedCustomFees(fee.toProtobuf());
        }

        for (var tokenAssociation : automaticTokenAssociations) {
            transactionRecord.addAutomaticTokenAssociations(tokenAssociation.toProtobuf());
        }

        if (aliasKey != null) {
            transactionRecord.setAlias(aliasKey.toProtobufKey().toByteString());
        }

        if (parentConsensusTimestamp != null) {
            transactionRecord.setParentConsensusTimestamp(InstantConverter.toProtobuf(parentConsensusTimestamp));
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
            .add("tokenNftTransfers", tokenNftTransfers)
            .add("scheduleRef", scheduleRef)
            .add("assessedCustomFees", assessedCustomFees)
            .add("automaticTokenAssociations", automaticTokenAssociations)
            .add("aliasKey", aliasKey)
            .add("children", children)
            .add("duplicates", duplicates)
            .add("parentConsensusTimestamp", parentConsensusTimestamp)
            .add("hbarAllowanceAdjustments", hbarAllowanceAdjustments)
            .add("tokenAllowanceAdjustments", tokenAllowanceAdjustments)
            .add("tokenNftAllowanceAdjustments", tokenNftAllowanceAdjustments)
            .toString();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
