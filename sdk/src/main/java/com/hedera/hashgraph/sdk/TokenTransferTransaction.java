package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.AccountAmount;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TokenTransferList;
import com.hedera.hashgraph.sdk.proto.TokenTransfersTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.util.ArrayList;
import java.util.List;

public class TokenTransferTransaction extends Transaction<TokenTransferTransaction> {
    private final TokenTransfersTransactionBody.Builder builder;
    private final TokenTransferList.Builder transfersBuilder;

    public TokenTransferTransaction() {
        builder = TokenTransfersTransactionBody.newBuilder();
        transfersBuilder = TokenTransferList.newBuilder();
    }

    TokenTransferTransaction(TransactionBody body) {
        super(body);

        builder = body.getTokenTransfers().toBuilder();

        transfersBuilder = TokenTransferList.newBuilder();

        // TODO: remove nested loop when fixed
        for (int i = 0; i <= builder.getTokenTransfersCount(); i++) {
            for (int j = 0; j <= builder.getTokenTransfersCount(); j++)
                transfersBuilder.addTransfers(builder.getTokenTransfers(i).getTransfers(j).toBuilder());
        }
    }

    public List<Transfer> getTransfers() {
        var numTransfers = transfersBuilder.getTransfersCount();
        var transfers = new ArrayList<Transfer>(numTransfers);

        for (var i = 0; i < numTransfers; i++) {
            transfers.add(Transfer.fromProtobuf(transfersBuilder.getTransfers(i)));
        }

        return transfers;
    }

    /**
     * Adds hbar to the transfer from the given account.
     *
     * @param value    The Hbar value to be transferred
     * @param senderId The AccountId of the sender
     * @return {@code this}
     */
    public TokenTransferTransaction addSender(AccountId senderId, Hbar value) {
        return addTransfer(senderId, value.negated());
    }

    /**
     * Removes hbar from the transfer to the given account.
     *
     * @param value       The Hbar value to be received
     * @param recipientId The AccountId of the recipient
     * @return {@code this}
     */
    public TokenTransferTransaction addRecipient(AccountId recipientId, Hbar value) {
        return addTransfer(recipientId, value);
    }

    public TokenTransferTransaction addTransfer(AccountId accountId, Hbar value) {
        requireNotFrozen();

        transfersBuilder.addTransfers(
            AccountAmount.newBuilder()
                .setAccountID(accountId.toProtobuf())
                .setAmount(value.toTinybars())
                .build()
        );

        return this;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getTransferTokensMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenTransfers(builder.setTokenTransfers(0, transfersBuilder));
        return true;
    }
}
