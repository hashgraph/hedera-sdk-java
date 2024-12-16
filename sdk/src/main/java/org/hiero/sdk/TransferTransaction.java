// SPDX-License-Identifier: Apache-2.0
package org.hiero.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.MethodDescriptor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hiero.sdk.proto.AccountAmount;
import org.hiero.sdk.proto.CryptoServiceGrpc;
import org.hiero.sdk.proto.CryptoTransferTransactionBody;
import org.hiero.sdk.proto.SchedulableTransactionBody;
import org.hiero.sdk.proto.TransactionBody;
import org.hiero.sdk.proto.TransactionResponse;
import org.hiero.sdk.proto.TransferList;

/**
 * A transaction that transfers hbars and tokens between Hedera accounts. You can enter multiple transfers in a single
 * transaction. The net value of hbars between the sending accounts and receiving accounts must equal zero.
 * <p>
 * See <a href="https://docs.hedera.com/guides/docs/sdks/cryptocurrency/transfer-cryptocurrency">Hedera
 * Documentation</a>
 */
public class TransferTransaction extends AbstractTokenTransferTransaction<TransferTransaction> {
    private final ArrayList<HbarTransfer> hbarTransfers = new ArrayList<>();

    private static class HbarTransfer {
        final AccountId accountId;
        Hbar amount;
        boolean isApproved;

        HbarTransfer(AccountId accountId, Hbar amount, boolean isApproved) {
            this.accountId = accountId;
            this.amount = amount;
            this.isApproved = isApproved;
        }

        AccountAmount toProtobuf() {
            return AccountAmount.newBuilder()
                    .setAccountID(accountId.toProtobuf())
                    .setAmount(amount.toTinybars())
                    .setIsApproval(isApproved)
                    .build();
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("accountId", accountId)
                    .add("amount", amount)
                    .add("isApproved", isApproved)
                    .toString();
        }
    }

    /**
     * Constructor.
     */
    public TransferTransaction() {
        defaultMaxTransactionFee = new Hbar(1);
    }

    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction) records
     * @throws InvalidProtocolBufferException when there is an issue with the protobuf
     */
    TransferTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, org.hiero.sdk.proto.Transaction>> txs)
            throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * Constructor.
     *
     * @param txBody protobuf TransactionBody
     */
    TransferTransaction(org.hiero.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    /**
     * Extract the of hbar transfers.
     *
     * @return list of hbar transfers
     */
    public Map<AccountId, Hbar> getHbarTransfers() {
        Map<AccountId, Hbar> transfers = new HashMap<>();

        for (var transfer : hbarTransfers) {
            transfers.put(transfer.accountId, transfer.amount);
        }

        return transfers;
    }

    private TransferTransaction doAddHbarTransfer(AccountId accountId, Hbar value, boolean isApproved) {
        requireNotFrozen();

        for (var transfer : hbarTransfers) {
            if (transfer.accountId.equals(accountId) && transfer.isApproved == isApproved) {
                transfer.amount = Hbar.fromTinybars(transfer.amount.toTinybars() + value.toTinybars());
                return this;
            }
        }

        hbarTransfers.add(new HbarTransfer(accountId, value, isApproved));
        return this;
    }

    /**
     * Add a non approved hbar transfer to an EVM address.
     *
     * @param evmAddress the EVM address
     * @param value      the value
     * @return the updated transaction
     */
    public TransferTransaction addHbarTransfer(EvmAddress evmAddress, Hbar value) {
        AccountId accountId = AccountId.fromEvmAddress(evmAddress);
        return doAddHbarTransfer(accountId, value, false);
    }

    /**
     * Add a non approved hbar transfer.
     *
     * @param accountId the account id
     * @param value     the value
     * @return the updated transaction
     */
    public TransferTransaction addHbarTransfer(AccountId accountId, Hbar value) {
        return doAddHbarTransfer(accountId, value, false);
    }

    /**
     * Add an approved hbar transfer.
     *
     * @param accountId the account id
     * @param value     the value
     * @return the updated transaction
     */
    public TransferTransaction addApprovedHbarTransfer(AccountId accountId, Hbar value) {
        return doAddHbarTransfer(accountId, value, true);
    }

    /**
     * @param accountId  the account id
     * @param isApproved whether the transfer is approved
     * @return {@code this}
     * @deprecated - Use {@link #addApprovedHbarTransfer(AccountId, Hbar)} instead
     */
    @Deprecated
    public TransferTransaction setHbarTransferApproval(AccountId accountId, boolean isApproved) {
        requireNotFrozen();

        for (var transfer : hbarTransfers) {
            if (transfer.accountId.equals(accountId)) {
                transfer.isApproved = isApproved;
                return this;
            }
        }

        return this;
    }

    /**
     * Build the transaction body.
     *
     * @return {@link org.hiero.sdk.proto.CryptoTransferTransactionBody}
     */
    CryptoTransferTransactionBody.Builder build() {
        var transfers = sortTransfersAndBuild();

        var builder = CryptoTransferTransactionBody.newBuilder();

        this.hbarTransfers.sort(
                Comparator.comparing((HbarTransfer a) -> a.accountId).thenComparing(a -> a.isApproved));
        var hbarTransfersList = TransferList.newBuilder();
        for (var transfer : hbarTransfers) {
            hbarTransfersList.addAccountAmounts(transfer.toProtobuf());
        }
        builder.setTransfers(hbarTransfersList);

        for (var transfer : transfers) {
            builder.addTokenTransfers(transfer.toProtobuf());
        }

        return builder;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        super.validateChecksums(client);
        for (var transfer : hbarTransfers) {
            transfer.accountId.validateChecksum(client);
        }
    }

    @Override
    MethodDescriptor<org.hiero.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return CryptoServiceGrpc.getCryptoTransferMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setCryptoTransfer(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setCryptoTransfer(build());
    }

    /**
     * Initialize from the transaction body.
     */
    void initFromTransactionBody() {
        var body = sourceTransactionBody.getCryptoTransfer();

        for (var transfer : body.getTransfers().getAccountAmountsList()) {
            hbarTransfers.add(new HbarTransfer(
                    AccountId.fromProtobuf(transfer.getAccountID()),
                    Hbar.fromTinybars(transfer.getAmount()),
                    transfer.getIsApproval()));
        }

        for (var tokenTransferList : body.getTokenTransfersList()) {
            var token = TokenId.fromProtobuf(tokenTransferList.getToken());

            for (var transfer : tokenTransferList.getTransfersList()) {
                tokenTransfers.add(new TokenTransfer(
                        token,
                        AccountId.fromProtobuf(transfer.getAccountID()),
                        transfer.getAmount(),
                        tokenTransferList.hasExpectedDecimals()
                                ? tokenTransferList.getExpectedDecimals().getValue()
                                : null,
                        transfer.getIsApproval()));
            }

            for (var transfer : tokenTransferList.getNftTransfersList()) {
                nftTransfers.add(new TokenNftTransfer(
                        token,
                        AccountId.fromProtobuf(transfer.getSenderAccountID()),
                        AccountId.fromProtobuf(transfer.getReceiverAccountID()),
                        transfer.getSerialNumber(),
                        transfer.getIsApproval()));
            }
        }
    }
}
