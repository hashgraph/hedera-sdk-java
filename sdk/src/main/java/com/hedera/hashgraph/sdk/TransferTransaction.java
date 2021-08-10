package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.CryptoTransferTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenTransferList;
import com.hedera.hashgraph.sdk.proto.AccountAmount;
import com.hedera.hashgraph.sdk.proto.TransferList;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;

public class TransferTransaction extends Transaction<TransferTransaction> {
    private final Map<TokenId, Map<AccountId, Long>> tokenTransfers = new HashMap<>();
    private final Map<TokenId, List<TokenNftTransfer>> nftTransfers = new HashMap<>();
    private final Map<AccountId, Hbar> hbarTransfers = new HashMap<>();

    public TransferTransaction() {
        setMaxTransactionFee(new Hbar(1));
    }

    TransferTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    TransferTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    public Map<TokenId, Map<AccountId, Long>> getTokenTransfers() {
        return tokenTransfers;
    }

    public TransferTransaction addTokenTransfer(TokenId tokenId, AccountId accountId, long value) {
        requireNotFrozen();
        doAddTokenTransfer(getTokenTransferMap(tokenId), accountId, value);
        return this;
    }

    public Map<TokenId, List<TokenNftTransfer>> getTokenNftTransfers() {
        return nftTransfers;
    }

    public TransferTransaction addNftTransfer(NftId nftId, AccountId sender, AccountId receiver) {
        requireNotFrozen();
        getNftTransferList(nftId.tokenId).add(new TokenNftTransfer(sender, receiver, nftId.serial));
        return this;
    }

    public Map<AccountId, Hbar> getHbarTransfers() {
        return hbarTransfers;
    }

    public TransferTransaction addHbarTransfer(AccountId accountId, Hbar value) {
        requireNotFrozen();
        doAddHbarTransfer(accountId, value.toTinybars());
        return this;
    }

    CryptoTransferTransactionBody.Builder build() {
        var builder = CryptoTransferTransactionBody.newBuilder();
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

        for(var entry : nftTransfers.entrySet()) {
            var list = TokenTransferList.newBuilder()
                .setToken(entry.getKey().toProtobuf());

            for(var nftTransfer : entry.getValue()) {
                list.addNftTransfers(nftTransfer.toProtobuf());
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
    void validateChecksums(Client client) throws BadEntityIdException {
        for (var a : hbarTransfers.keySet()) {
            a.validateChecksum(client);
        }

        for(var entry : nftTransfers.entrySet()) {
            entry.getKey().validateChecksum(client);

            for(var nftTransfer : entry.getValue()) {
                nftTransfer.sender.validateChecksum(client);
                nftTransfer.receiver.validateChecksum(client);
            }
        }

        for (var entry : tokenTransfers.entrySet()) {
            entry.getKey().validateChecksum(client);

            for (var a : entry.getValue().keySet()) {
                a.validateChecksum(client);
            }
        }
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return CryptoServiceGrpc.getCryptoTransferMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setCryptoTransfer(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setCryptoTransfer(build());
    }

    private Map<AccountId, Long> getTokenTransferMap(TokenId tokenId) {
        // Cannot use `Map.merge()` as it uses `BiFunction`
        var map = tokenTransfers.containsKey(tokenId) ?
            Objects.requireNonNull(tokenTransfers.get(tokenId)) :
            new HashMap<AccountId, Long>();
        tokenTransfers.put(tokenId, map);
        return map;
    }

    private List<TokenNftTransfer> getNftTransferList(TokenId tokenId) {
        // Cannot use `Map.merge()` as it uses `BiFunction`
        var list = nftTransfers.containsKey(tokenId) ?
            Objects.requireNonNull(nftTransfers.get(tokenId)) :
            new ArrayList<TokenNftTransfer>();
        nftTransfers.put(tokenId, list);
        return list;
    }

    private static void doAddTokenTransfer(Map<AccountId, Long> tokenTransferMap, AccountId accountId, long amount) {
        Objects.requireNonNull(tokenTransferMap);
        Objects.requireNonNull(accountId);
        var current = tokenTransferMap.containsKey(accountId) ?
            Objects.requireNonNull(tokenTransferMap.get(accountId)) :
            0L;
        tokenTransferMap.put(accountId, current + amount);
    }

    private void doAddHbarTransfer(AccountId accountId, long amount) {
        Objects.requireNonNull(accountId);
        var current = hbarTransfers.containsKey(accountId) ?
            Objects.requireNonNull(hbarTransfers.get(accountId)).toTinybars() :
            0L;

        hbarTransfers.put(accountId, Hbar.fromTinybars(current + amount));
    }

    void initFromTransactionBody() {
        var body = sourceTransactionBody.getCryptoTransfer();
        if(body.hasTransfers()) {
            for (var transfer : body.getTransfers().getAccountAmountsList()) {
                doAddHbarTransfer(AccountId.fromProtobuf(transfer.getAccountID()), transfer.getAmount());
            }
        }

        for (var tokenTransferList : body.getTokenTransfersList()) {
            var token = TokenId.fromProtobuf(tokenTransferList.getToken());

            if(tokenTransferList.getTransfersCount() > 0) {
                var map = getTokenTransferMap(token);

                for (var aa : tokenTransferList.getTransfersList()) {
                    doAddTokenTransfer(map, AccountId.fromProtobuf(aa.getAccountID()), aa.getAmount());
                }
            }

            if(tokenTransferList.getNftTransfersCount() > 0) {
                var list = getNftTransferList(token);
                for(var nftTransfer : tokenTransferList.getNftTransfersList()) {
                    list.add(TokenNftTransfer.fromProtobuf(nftTransfer));
                }
            }
        }
    }
}
