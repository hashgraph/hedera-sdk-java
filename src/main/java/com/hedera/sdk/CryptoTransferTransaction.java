package com.hedera.sdk;

import com.hedera.sdk.proto.AccountAmount;
import com.hedera.sdk.proto.CryptoTransferTransactionBody;
import com.hedera.sdk.proto.TransferList;
import javax.annotation.Nonnegative;

public final class CryptoTransferTransaction extends TransactionBuilder<CryptoTransferTransaction> {
    private CryptoTransferTransactionBody.Builder builder;
    private TransferList.Builder transferList;

    public CryptoTransferTransaction() {
        builder = inner.getBodyBuilder().getCryptoTransferBuilder();
        transferList = builder.getTransfersBuilder();
    }

    public CryptoTransferTransaction addSender(AccountId senderId, @Nonnegative long value) {
        return this.addTransfer(senderId, value * -1L);
    }

    public CryptoTransferTransaction addRecipient(AccountId recipientId, @Nonnegative long value) {
        return this.addTransfer(recipientId, value);
    }

    public CryptoTransferTransaction addTransfer(AccountId accountId, long value) {
        transferList.addAccountAmounts(
                AccountAmount.newBuilder().setAccountID(accountId.inner).setAmount(value).build());

        return this;
    }
}
