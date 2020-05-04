package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.AccountAmount;
import com.hedera.hashgraph.sdk.proto.CryptoTransferTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransferList;

/**
 * Transfer cryptocurrency from some accounts to other accounts.
 * <p>
 * The accounts list can contain up to 10 accounts.
 * <p>
 * The amounts list must be the same length as the accounts list.
 * <p>
 * Each negative amount is withdrawn from the corresponding account (a sender), and each
 * positive one is added to the corresponding account (a receiver).
 * <p>
 * The amounts list must sum to zero. Each amount is a number of hbars
 * If any sender account fails to have sufficient hbars to do the withdrawal,
 * then the entire transaction fails, and none of those transfers occur, though the
 * transaction fee is still charged.
 * <p>
 * This transaction must be signed by the keys for all the sending accounts, and
 * for any receiving accounts that have `receiverSigRequired == true`.
 * <p>
 * The signatures are in the same order as the accounts,
 * skipping those accounts that don't need a signature.
 */
public final class CryptoTransferTransaction extends TransactionBuilder<CryptoTransferTransaction> {
    private final CryptoTransferTransactionBody.Builder builder;
    private final TransferList.Builder transfersBuilder;

    public CryptoTransferTransaction() {
        builder = CryptoTransferTransactionBody.newBuilder();
        transfersBuilder = TransferList.newBuilder();
    }

    /**
     * Adds hbar to the transfer from the given account.
     *
     * @return {@code this}
     */
    public CryptoTransferTransaction addSender(AccountId senderId, Hbar value) {
        return addTransfer(senderId, value.negate());
    }

    /**
     * Removes hbar from the transfer to the given account.
     *
     * @return {@code this}
     */
    public CryptoTransferTransaction addRecipient(AccountId recipientId, Hbar value) {
        return addTransfer(recipientId, value);
    }

    public CryptoTransferTransaction addTransfer(AccountId accountId, Hbar value) {
        transfersBuilder.addAccountAmounts(
            AccountAmount.newBuilder()
                .setAccountID(accountId.toProtobuf())
                .setAmount(value.toTinybars())
                .build()
        );

        return this;
    }

    @Override
    void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setCryptoTransfer(builder.setTransfers(transfersBuilder));
    }
}
