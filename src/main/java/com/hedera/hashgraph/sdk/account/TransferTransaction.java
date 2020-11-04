package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.proto.*;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.SingleTransactionBuilder;

import javax.annotation.Nonnegative;

import com.hedera.hashgraph.sdk.token.TokenId;
import com.hedera.hashgraph.sdk.token.TokenTransferTransaction;
import io.grpc.MethodDescriptor;

import java.util.HashMap;

public final class TransferTransaction extends SingleTransactionBuilder<CryptoTransferTransaction> {
    private final CryptoTransferTransactionBody.Builder builder = bodyBuilder.getCryptoTransferBuilder();
    private final TransferList.Builder transferList = builder.getTransfersBuilder();
    private HashMap<TokenId, Integer> tokenIndexes = new HashMap<>();

    public TransferTransaction() { super(); }

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
    public TransferTransaction addHbarSender(AccountId senderId, Hbar value) {
        return this.addHbarTransfer(senderId, value.asTinybar() * -1L);
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
    public TransferTransaction addHbarSender(AccountId senderId, @Nonnegative long value) {
        return this.addHbarTransfer(senderId, value * -1L);
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
    public TransferTransaction addHbarRecipient(AccountId recipientId, Hbar value) {
        return this.addHbarTransfer(recipientId, value.asTinybar());
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
    public TransferTransaction addHbarRecipient(AccountId recipientId, @Nonnegative long value) {
        return this.addHbarTransfer(recipientId, value);
    }

    public TransferTransaction addHbarTransfer(AccountId accountId, long value) {
        transferList.addAccountAmounts(
            AccountAmount.newBuilder()
                .setAccountID(accountId.toProto())
                .setAmount(value)
                .build());

        return this;
    }

    public TransferTransaction addTokenSender(TokenId tokenId, AccountId accountId, @Nonnegative long amount) {
        return addTokenTransfer(tokenId, accountId, -amount);
    }

    public TransferTransaction addTokenRecipient(TokenId tokenId, AccountId accountId, @Nonnegative long amount) {
        return addTokenTransfer(tokenId, accountId, amount);
    }

    public TransferTransaction addTokenTransfer(TokenId tokenId, AccountId accountId, long amount) {
        Integer index = tokenIndexes.get(tokenId);
        int size = builder.getTokenTransfersCount();

        TokenTransferList.Builder transfers;

        if (index != null) {
            transfers = builder.getTokenTransfersBuilder(index);
        } else {
            builder.addTokenTransfers(TokenTransferList.newBuilder());
            transfers = builder.getTokenTransfersBuilder(size);
            tokenIndexes.put(tokenId, size);
        }

        transfers.setToken(tokenId.toProto());
        transfers.addTransfers(AccountAmount.newBuilder()
            .setAccountID(accountId.toProto())
            .setAmount(amount)
        );

        return this;
    }

    @Override
    protected void doValidate() {
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return CryptoServiceGrpc.getCryptoTransferMethod();
    }
}
