package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.AccountAmount;
import com.hedera.hashgraph.sdk.proto.CryptoTransferTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransferList;

import javax.annotation.Nonnegative;

public final class CryptoTransferTransaction extends TransactionBuilder<CryptoTransferTransaction> {
    private final CryptoTransferTransactionBody.Builder builder;
    private final TransferList.Builder transfersBuilder;

    public CryptoTransferTransaction() {
        builder = CryptoTransferTransactionBody.newBuilder();
        transfersBuilder = TransferList.newBuilder();
    }

    public CryptoTransferTransaction addSender(AccountId senderId, @Nonnegative long value) {
        return addTransfer(senderId, value * -1L);
    }

    public CryptoTransferTransaction addRecipient(AccountId recipientId, @Nonnegative long value) {
        return addTransfer(recipientId, value);
    }

    public CryptoTransferTransaction addTransfer(AccountId accountId, long value) {
        transfersBuilder.addAccountAmounts(
            AccountAmount.newBuilder()
                .setAccountID(accountId.toProtobuf())
                .setAmount(value)
                .build()
        );

        return this;
    }

    @Override
    protected void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setCryptoTransfer(builder.setTransfers(transfersBuilder));
    }
}
