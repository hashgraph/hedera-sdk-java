package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

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
            hbarTransfers.merge(
                AccountId.fromProtobuf(transfer.getAccountID()),
                Hbar.fromTinybars(transfer.getAmount()),
                (a, b) -> Hbar.fromTinybars(a.toTinybars() + b.toTinybars())
            );
        }

        for (var tokenTransferList : bodyBuilder.getCryptoTransfer().getTokenTransfersList()) {
            var list = tokenTransfers.computeIfAbsent(TokenId.fromProtobuf(tokenTransferList.getToken()), k -> new HashMap<>());

            for (var aa : tokenTransferList.getTransfersList()) {
                list.merge(AccountId.fromProtobuf(aa.getAccountID()), aa.getAmount(), Long::sum);
            }
        }
    }

    TransferTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);

        builder = bodyBuilder.getCryptoTransfer().toBuilder();

        for (var transfer : bodyBuilder.getCryptoTransfer().getTransfers().getAccountAmountsList()) {
            hbarTransfers.merge(
                AccountId.fromProtobuf(transfer.getAccountID()),
                Hbar.fromTinybars(transfer.getAmount()),
                (a, b) -> Hbar.fromTinybars(a.toTinybars() + b.toTinybars())
            );
        }

        for (var tokenTransferList : bodyBuilder.getCryptoTransfer().getTokenTransfersList()) {
            var list = tokenTransfers.computeIfAbsent(TokenId.fromProtobuf(tokenTransferList.getToken()), k -> new HashMap<>());

            for (var aa : tokenTransferList.getTransfersList()) {
                list.merge(AccountId.fromProtobuf(aa.getAccountID()), aa.getAmount(), Long::sum);
            }
        }
    }

    public Map<TokenId, Map<AccountId, Long>> getTokenTransfers() {
        return tokenTransfers;
    }

    public TransferTransaction addTokenTransfer(TokenId tokenId, AccountId accountId, long value) {
        requireNotFrozen();

        // Cannot use `Map.merge()` as it uses `BiFunction`
        var tokenTransfer = Objects.requireNonNull(tokenTransfers.computeIfAbsent(tokenId, k -> new HashMap<>()));
        var current = tokenTransfer.computeIfAbsent(accountId, k -> 0L);
        tokenTransfer.put(accountId, current + value);

        return this;
    }

    public Map<AccountId, Hbar> getHbarTransfers() {
        return hbarTransfers;
    }

    public TransferTransaction addHbarTransfer(AccountId accountId, Hbar value) {
        requireNotFrozen();

        // Cannot use `Map.merge()` as it uses `BiFunction`
        var current = hbarTransfers.computeIfAbsent(accountId, k -> new Hbar(0));
        hbarTransfers.put(accountId, Hbar.fromTinybars(current.toTinybars() + value.toTinybars()));

        return this;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return CryptoServiceGrpc.getCryptoTransferMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
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

        bodyBuilder.setCryptoTransfer(builder);
        return true;
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setCryptoTransfer(builder);
    }
}
