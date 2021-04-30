package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.proto.*;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.SingleTransactionBuilder;
import com.hedera.hashgraph.sdk.token.TokenId;
import io.grpc.MethodDescriptor;

import java.util.HashMap;
import java.util.Map;

public final class TransferTransaction extends SingleTransactionBuilder<TransferTransaction> {
    private final CryptoTransferTransactionBody.Builder builder = bodyBuilder.getCryptoTransferBuilder();
    private final TransferList.Builder transferList = builder.getTransfersBuilder();
    private HashMap<TokenId, Integer> tokenIndexes = new HashMap<>();

    public TransferTransaction() { super(); }

    public TransferTransaction addHbarTransfer(AccountId accountId, Hbar value) {
        return addHbarTransfer(accountId, value.asTinybar());
    }
    public TransferTransaction addHbarTransfer(AccountId accountId, long value) {
        transferList.addAccountAmounts(
            AccountAmount.newBuilder()
                .setAccountID(accountId.toProto())
                .setAmount(value)
                .build());

        return this;
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
