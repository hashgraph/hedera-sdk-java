package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class TransferTransaction extends Transaction<TransferTransaction> {
    private final CryptoTransferTransactionBody.Builder builder;
    private final Map<TokenId, Map<AccountId, Long>> tokenTransfers = new HashMap<>();
    private final Map<AccountId, Hbar> hbarTransfers = new HashMap<>();

    public TransferTransaction() {
        builder = CryptoTransferTransactionBody.newBuilder();

        setMaxTransactionFee(new Hbar(1));
    }

    TransferTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getCryptoTransfer().toBuilder();

        for (var transfer : bodyBuilder.getCryptoTransfer().getTransfers().getAccountAmountsList()) {
            var account = AccountId.fromProtobuf(transfer.getAccountID());
            var current = hbarTransfers.containsKey(account) ?
                Objects.requireNonNull(hbarTransfers.get(account)).toTinybars() :
                0L;

            hbarTransfers.put(account, Hbar.fromTinybars(current + transfer.getAmount()));
        }

        for (var tokenTransferList : bodyBuilder.getCryptoTransfer().getTokenTransfersList()) {
            var token = TokenId.fromProtobuf(tokenTransferList.getToken());
            var list = tokenTransfers.containsKey(token) ?
                Objects.requireNonNull(tokenTransfers.get(token)) :
                new HashMap<AccountId, Long>();
            tokenTransfers.put(token, list);

            for (var aa : tokenTransferList.getTransfersList()) {
                var account = AccountId.fromProtobuf(aa.getAccountID());
                var current = list.containsKey(account) ?
                    Objects.requireNonNull(list.get(account)) :
                    0L;

                list.put(account, current + aa.getAmount());
            }
        }
    }

    TransferTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);

        builder = bodyBuilder.getCryptoTransfer().toBuilder();

        for (var transfer : bodyBuilder.getCryptoTransfer().getTransfers().getAccountAmountsList()) {
            var account = AccountId.fromProtobuf(transfer.getAccountID());
            var current = hbarTransfers.containsKey(account) ?
                Objects.requireNonNull(hbarTransfers.get(account)).toTinybars() :
                0L;

            hbarTransfers.put(account, Hbar.fromTinybars(current + transfer.getAmount()));
        }

        for (var tokenTransferList : bodyBuilder.getCryptoTransfer().getTokenTransfersList()) {
            var token = TokenId.fromProtobuf(tokenTransferList.getToken());
            var list = tokenTransfers.containsKey(token) ?
                Objects.requireNonNull(tokenTransfers.get(token)) :
                new HashMap<AccountId, Long>();
            tokenTransfers.put(token, list);

            for (var aa : tokenTransferList.getTransfersList()) {
                var account = AccountId.fromProtobuf(aa.getAccountID());
                var current = list.containsKey(account) ?
                    Objects.requireNonNull(list.get(account)) :
                    0L;

                list.put(account, current + aa.getAmount());
            }
        }
    }

    public Map<TokenId, Map<AccountId, Long>> getTokenTransfers() {
        return tokenTransfers;
    }

    public TransferTransaction addTokenTransfer(TokenId tokenId, AccountId accountId, long value) {
        requireNotFrozen();

        // Cannot use `Map.merge()` as it uses `BiFunction`
        var tokenTransfer = tokenTransfers.containsKey(tokenId) ?
            Objects.requireNonNull(tokenTransfers.get(tokenId)) :
            new HashMap<AccountId, Long>();
        tokenTransfers.put(tokenId, tokenTransfer);

        var current = tokenTransfer.containsKey(accountId) ?
            Objects.requireNonNull(tokenTransfer.get(accountId)) :
            0L;

        tokenTransfer.put(accountId, current + value);

        return this;
    }

    public Map<AccountId, Hbar> getHbarTransfers() {
        return hbarTransfers;
    }

    public TransferTransaction addHbarTransfer(AccountId accountId, Hbar value) {
        requireNotFrozen();

        // Cannot use `Map.merge()` as it uses `BiFunction`
        var current = hbarTransfers.containsKey(accountId) ?
            Objects.requireNonNull(hbarTransfers.get(accountId)).toTinybars() :
            0L;

        hbarTransfers.put(accountId, Hbar.fromTinybars(current + value.toTinybars()));

        return this;
    }

    CryptoTransferTransactionBody.Builder build() {
        for (var entry : tokenTransfers.entrySet()) {
            var list = TokenTransferList.newBuilder()
                .setToken(entry.getKey().toProtobuf());

            for (var aa : entry.getValue().entrySet()) {
                list.addTransfers(AccountAmount.newBuilder()
                    .setAccountID(aa.getKey().toProtobuf())
                    .setAmount(aa.getValue())
                );
            }

            builder.addTokenTransfers(list);
        }

        var list = TransferList.newBuilder();
        for (var entry : hbarTransfers.entrySet()) {
            list.addAccountAmounts(AccountAmount.newBuilder()
                .setAccountID(entry.getKey().toProtobuf())
                .setAmount(entry.getValue().toTinybars())
            );
        }
        builder.setTransfers(list);

        return builder;
    }

    @Override
    void validateNetworkOnIds(@Nullable AccountId accountId) {
        for (var a : hbarTransfers.keySet()) {
            EntityIdHelper.validateNetworkOnIds(a, accountId);
        }

        for (var entry : tokenTransfers.entrySet()) {
            EntityIdHelper.validateNetworkOnIds(entry.getKey(), accountId);

            for (var a : entry.getValue().keySet()) {
                EntityIdHelper.validateNetworkOnIds(a, accountId);
            }
        }
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return CryptoServiceGrpc.getCryptoTransferMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setCryptoTransfer(build());
        return true;
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setCryptoTransfer(build());
    }
}
