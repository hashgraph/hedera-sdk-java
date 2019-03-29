package com.hedera.sdk.account;

import com.hedera.sdk.AccountId;
import com.hedera.sdk.TransactionBuilder;
import com.hedera.sdk.proto.*;
import com.hedera.sdk.proto.Transaction;
import io.grpc.MethodDescriptor;
import javax.annotation.Nonnegative;

// `CryptoTransferTransaction`
public final class AccountBalanceTransferTransaction
        extends TransactionBuilder<AccountBalanceTransferTransaction> {
    private CryptoTransferTransactionBody.Builder builder;
    private TransferList.Builder transferList;

    public AccountBalanceTransferTransaction() {
        builder = inner.getBodyBuilder().getCryptoTransferBuilder();
        transferList = builder.getTransfersBuilder();
    }

    public AccountBalanceTransferTransaction addSender(
            AccountId senderId, @Nonnegative long value) {
        return this.addTransfer(senderId, value * -1L);
    }

    public AccountBalanceTransferTransaction addRecipient(
            AccountId recipientId, @Nonnegative long value) {
        return this.addTransfer(recipientId, value);
    }

    public AccountBalanceTransferTransaction addTransfer(AccountId accountId, long value) {
        transferList.addAccountAmounts(
                AccountAmount.newBuilder()
                        .setAccountID(accountId.toProto())
                        .setAmount(value)
                        .build());

        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return CryptoServiceGrpc.getCryptoTransferMethod();
    }
}
