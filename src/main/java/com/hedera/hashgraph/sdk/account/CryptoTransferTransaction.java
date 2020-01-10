package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.proto.AccountAmount;
import com.hedera.hashgraph.proto.AccountAmountOrBuilder;
import com.hedera.hashgraph.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.proto.CryptoTransferTransactionBody;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.proto.TransferList;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.TransactionBuilder;

import javax.annotation.Nonnegative;

import io.grpc.MethodDescriptor;

public final class CryptoTransferTransaction extends TransactionBuilder<CryptoTransferTransaction> {
    private final CryptoTransferTransactionBody.Builder builder = bodyBuilder.getCryptoTransferBuilder();
    private final TransferList.Builder transferList = builder.getTransfersBuilder();

    public CryptoTransferTransaction() { super(); }

    /**
     * Add a party to the transfer who will have currency withdrawn from their account
     * to add to the transfer.
     *
     * This party will need to sign the transaction.
     *
     * Parties may not appear on the same transaction more than once and the net value of the
     * transfer must be zero (total sent hbar must equal total received hbar).
     *
     * @param senderId the account ID of the party.
     * @param value the amount to transfer from the account.
     * @return {@code this} for fluent usage.
     */
    public CryptoTransferTransaction addSender(AccountId senderId, Hbar value) {
        return this.addTransfer(senderId, value.asTinybar() * -1L);
    }

    /**
     * Add a party to the transfer who will have currency withdrawn from their account
     * to add to the transfer.
     *
     * This party will need to sign the transaction.
     *
     * @param senderId the account ID of the party.
     * @param value the amount to transfer from the account, in tinybar.
     * @return {@code this} for fluent usage.
     */
    public CryptoTransferTransaction addSender(AccountId senderId, @Nonnegative long value) {
        return this.addTransfer(senderId, value * -1L);
    }

    /**
     * Add a party to the transfer who will have currency credited to their account
     * by this transfer.
     *
     * Parties may not appear on the same transaction more than once.
     *
     * @param recipientId the account ID of the party.
     * @param value the amount to transfer from the account.
     * @return {@code this} for fluent usage.
     */
    public CryptoTransferTransaction addRecipient(AccountId recipientId, Hbar value) {
        return this.addTransfer(recipientId, value.asTinybar());
    }

    /**
     * Add a party to the transfer who will have currency credited to their account
     * by this transfer.
     *
     * Parties may not appear on the same transaction more than once.
     *
     * @param recipientId the account ID of the party.
     * @param value the amount to transfer from the account, in tinybar.
     * @return {@code this} for fluent usage.
     */
    public CryptoTransferTransaction addRecipient(AccountId recipientId, @Nonnegative long value) {
        return this.addTransfer(recipientId, value);
    }

    public CryptoTransferTransaction addTransfer(AccountId accountId, long value) {
        transferList.addAccountAmounts(
            AccountAmount.newBuilder()
                .setAccountID(accountId.toProto())
                .setAmount(value)
                .build());

        return this;
    }

    @Override
    protected void doValidate() {
        require(transferList.getAccountAmountsOrBuilderList(), "at least one transfer required");

        long sum = 0;

        for (AccountAmountOrBuilder acctAmt : transferList.getAccountAmountsOrBuilderList()) {
            sum += acctAmt.getAmount();
        }

        if (sum != 0) {
            addValidationError(String.format("transfer transaction must have zero sum; transfer balance: %d tinybar", sum));
        }
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return CryptoServiceGrpc.getCryptoTransferMethod();
    }
}
