package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hederahashgraph.api.proto.java.AccountAmount;
import com.hederahashgraph.api.proto.java.AccountAmountOrBuilder;
import com.hederahashgraph.api.proto.java.CryptoTransferTransactionBody;
import com.hederahashgraph.api.proto.java.Transaction;
import com.hederahashgraph.api.proto.java.TransactionResponse;
import com.hederahashgraph.api.proto.java.TransferList;
import com.hederahashgraph.service.proto.java.CryptoServiceGrpc;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;

import io.grpc.MethodDescriptor;

public final class CryptoTransferTransaction extends TransactionBuilder<CryptoTransferTransaction> {
    private final CryptoTransferTransactionBody.Builder builder = bodyBuilder.getCryptoTransferBuilder();
    private final TransferList.Builder transferList = builder.getTransfersBuilder();

    /**
     * @deprecated use the no-arg constructor and pass the client to {@link #build(Client)} instead.
     */
    @Deprecated
    public CryptoTransferTransaction(@Nullable Client client) {
        super(client);
    }

    public CryptoTransferTransaction() { super(); }

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
