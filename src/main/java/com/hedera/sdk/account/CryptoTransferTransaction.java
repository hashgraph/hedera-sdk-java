package com.hedera.sdk.account;

import com.hedera.sdk.AccountId;
import com.hedera.sdk.TransactionBuilder;
import com.hedera.sdk.proto.*;
import com.hedera.sdk.proto.Transaction;
import io.grpc.MethodDescriptor;
import javax.annotation.Nonnegative;

public final class CryptoTransferTransaction extends TransactionBuilder<CryptoTransferTransaction> {
    private CryptoTransferTransactionBody.Builder builder;
    private TransferList.Builder transferList;

    public CryptoTransferTransaction() {
        builder = inner.getBodyBuilder()
            .getCryptoTransferBuilder();
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
            AccountAmount.newBuilder()
                .setAccountID(accountId.toProto())
                .setAmount(value)
                .build()
        );

        return this;
    }

    @Override
    protected void doValidate() {
        require(transferList.getAccountAmountsOrBuilderList(), "at least one transfer required");

        long sum = 0;

        for (var acctAmt : transferList.getAccountAmountsOrBuilderList()) {
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
