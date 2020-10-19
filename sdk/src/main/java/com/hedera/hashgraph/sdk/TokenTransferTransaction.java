package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TokenTransferTransaction extends Transaction<TokenTransferTransaction> {
    private final TokenTransfersTransactionBody.Builder builder;
    private HashMap<TokenId, TokenTransferList.Builder> tokenTransferLists;
    public TokenTransferTransaction() {
        builder = TokenTransfersTransactionBody.newBuilder();
        tokenTransferLists = new HashMap<>();
    }

    TokenTransferTransaction(TransactionBody body) {
        super(body);

        builder = body.getTokenTransfers().toBuilder();

        for (var list : builder.getTokenTransfersList()) {
            var token = TokenId.fromProtobuf(list.getToken());
            tokenTransferLists.put(token, list.toBuilder());
        }
    }

    public HashMap<TokenId, List<TokenTransfer>> getTransfers() {
        var tokenTransferListMap = new HashMap<TokenId, List<TokenTransfer>>();

        for (var entry : tokenTransferLists.entrySet()) {
            var list = new ArrayList<TokenTransfer>(entry.getValue().getTransfersCount());
            for (var transfer : entry.getValue().getTransfersList()) {
                list.add(TokenTransfer.fromProtobuf(transfer));
            }
            tokenTransferListMap.put(entry.getKey(), list);
        }

        return tokenTransferListMap;
    }

    /**
     * Adds token value to the transfer from the given account.
     *
     * @param tokenId  The Id of the token to be transferred
     * @param value    The token value to be transferred
     * @param senderId The AccountId of the sender
     * @return {@code this}
     */
    public TokenTransferTransaction addSender(TokenId tokenId, AccountId senderId, long value) {
        return addTransfer(tokenId, senderId, -value);
    }

    /**
     * Removes token value from the transfer to the given account.
     *
     * @param tokenId     The Id of the token to be received
     * @param value       The token value to be received
     * @param recipientId The AccountId of the recipient
     * @return {@code this}
     */
    public TokenTransferTransaction addRecipient(TokenId tokenId, AccountId recipientId, long value) {
        return addTransfer(tokenId, recipientId, value);
    }

    public TokenTransferTransaction addTransfer(TokenId tokenId, AccountId accountId, long value) {
        requireNotFrozen();

        var list = tokenTransferLists.get(tokenId);
        if (list == null) {
            list = TokenTransferList.newBuilder().setToken(tokenId.toProtobuf());
            tokenTransferLists.put(tokenId, list);
        }

        list.addTransfers(
            AccountAmount.newBuilder()
                .setAccountID(accountId.toProtobuf())
                .setAmount(value)
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
        for (var entry : tokenTransferLists.entrySet()) {
            var listBuilder = TokenTransferList.newBuilder()
                .setToken(entry.getKey().toProtobuf());

            for (var transfer : entry.getValue().getTransfersList()) {
                listBuilder.addTransfers(transfer);
            }

            builder.addTokenTransfers(listBuilder);
        }

        bodyBuilder.setTokenTransfers(builder);
        return true;
    }
}
