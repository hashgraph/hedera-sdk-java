package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransferTransaction extends Transaction<TransferTransaction> {
    private final CryptoTransferTransactionBody.Builder builder;
    private final HashMap<TokenId, TokenTransferList.Builder> tokenTransferLists = new HashMap<>();
    private final TransferList.Builder transfersBuilder;

    public TransferTransaction() {
        builder = CryptoTransferTransactionBody.newBuilder();
        transfersBuilder = builder.getTransfers().toBuilder();
    }

    TransferTransaction(TransactionBody body) {
        super(body);

        builder = body.getCryptoTransfer().toBuilder();
        transfersBuilder = builder.getTransfers().toBuilder();

        for (var list : builder.getTokenTransfersList()) {
            var token = TokenId.fromProtobuf(list.getToken());
            tokenTransferLists.put(token, list.toBuilder());
        }
    }

    public HashMap<TokenId, Map<AccountId, Long>> getTokenTransfers() {
        var tokenTransferMap = new HashMap<TokenId, Map<AccountId, Long>>();

        for (var entry : tokenTransferLists.entrySet()) {
            var accountMap = new HashMap<AccountId, Long>();
            tokenTransferMap.put(entry.getKey(), accountMap);
            for (var transfer : entry.getValue().getTransfersList()) {
                accountMap.put(AccountId.fromProtobuf(transfer.getAccountID()), transfer.getAmount());
            }
        }

        return tokenTransferMap;
    }

    public TransferTransaction addTokenTransfer(TokenId tokenId, AccountId accountId, long value) {
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

    public HashMap<AccountId, Hbar> getHbarTransfers() {
        var numTransfers = transfersBuilder.getAccountAmountsCount();
        var transfers = new HashMap<AccountId, Hbar>(numTransfers);

        for (var aa : transfersBuilder.getAccountAmountsList()) {
            transfers.put(AccountId.fromProtobuf(aa.getAccountID()), Hbar.fromTinybars(aa.getAmount()));
        }

        return transfers;
    }

    public TransferTransaction addHbarTransfer(AccountId accountId, Hbar value) {
        requireNotFrozen();

        transfersBuilder.addAccountAmounts(
            AccountAmount.newBuilder()
                .setAccountID(accountId.toProtobuf())
                .setAmount(value.toTinybars())
                .build()
        );

        return this;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return CryptoServiceGrpc.getCryptoTransferMethod();
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

        bodyBuilder.setCryptoTransfer(builder);
        return true;
    }
}
