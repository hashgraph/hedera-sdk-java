package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.UInt32Value;
import com.hedera.hashgraph.sdk.proto.AccountAmount;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.CryptoTransferTransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenBalance;
import com.hedera.hashgraph.sdk.proto.TokenTransferList;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.proto.TransferList;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TransferTransaction extends Transaction<TransferTransaction> {
    private final Map<TokenId, TokenTransferMap> tokenTransfers = new HashMap<>();
    private final Map<TokenId, List<TokenNftTransfer>> nftTransfers = new HashMap<>();
    private final Map<AccountId, Hbar> hbarTransfers = new HashMap<>();

    private static class TokenTransferMap {
        public final Map<AccountId, Long> transfers = new HashMap<>();
        @Nullable
        private UInt32Value expectedDecimals = null;

        @Nullable
        public UInt32Value getExpectedDecimals() {
            return expectedDecimals;
        }

        public TokenTransferMap setExpectedDecimals(UInt32Value amount) {
            if (expectedDecimals != null && !amount.equals(expectedDecimals)) {
                throw new IllegalArgumentException("expected decimals for a token in a token transfer cannot be changed after being set");
            }
            expectedDecimals = amount;
            return this;
        }
    }

    public TransferTransaction() {
        defaultMaxTransactionFee = new Hbar(1);
    }

    TransferTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    TransferTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    @Nullable
    public Integer getTokenIdDecimals(TokenId tokenId) {
        var transferMap = tokenTransfers.get(tokenId);
        if (transferMap == null) {
            return null;
        }
        var decimals = transferMap.getExpectedDecimals();
        return decimals == null ? null : decimals.getValue();
    }

    private static void doAddTokenTransfer(Map<AccountId, Long> tokenTransferMap, AccountId accountId, long amount) {
        Objects.requireNonNull(tokenTransferMap);
        Objects.requireNonNull(accountId);
        var current = tokenTransferMap.containsKey(accountId) ?
            Objects.requireNonNull(tokenTransferMap.get(accountId)) :
            0L;
        tokenTransferMap.put(accountId, current + amount);
    }

    public Map<TokenId, Map<AccountId, Long>> getTokenTransfers() {
        Map<TokenId, Map<AccountId, Long>> retval = new HashMap<>();
        for (var entry : tokenTransfers.entrySet()) {
            retval.put(entry.getKey(), entry.getValue().transfers);
        }
        return retval;
    }

    public TransferTransaction addTokenTransfer(TokenId tokenId, AccountId accountId, long value) {
        requireNotFrozen();
        doAddTokenTransfer(getTokenTransferMap(tokenId).transfers, accountId, value);
        return this;
    }

    public TransferTransaction addTokenTransferWithDecimals(TokenId tokenId, AccountId accountId, long value, int decimals) {
        requireNotFrozen();
        doAddTokenTransfer(getTokenTransferMap(tokenId).setExpectedDecimals(UInt32Value.of(decimals)).transfers, accountId, value);
        return this;
    }

    public Map<TokenId, List<TokenNftTransfer>> getTokenNftTransfers() {
        return new HashMap<>(nftTransfers);
    }

    public TransferTransaction addNftTransfer(NftId nftId, AccountId sender, AccountId receiver) {
        requireNotFrozen();
        getNftTransferList(nftId.tokenId).add(new TokenNftTransfer(sender, receiver, nftId.serial));
        return this;
    }

    public Map<AccountId, Hbar> getHbarTransfers() {
        return new HashMap<>(hbarTransfers);
    }

    public TransferTransaction addHbarTransfer(AccountId accountId, Hbar value) {
        requireNotFrozen();
        doAddHbarTransfer(accountId, value.toTinybars());
        return this;
    }

    private static class SortableTokenTransferList implements Comparable<SortableTokenTransferList> {
        public final TokenId tokenId;
        @Nullable
        public final UInt32Value expectedDecimals;
        public final List<Map.Entry<AccountId, Long>> transfers;
        public final List<TokenNftTransfer> nftTransfers;

        private SortableTokenTransferList(
            TokenId tokenId,
            @Nullable UInt32Value expectedDecimals,
            List<Map.Entry<AccountId, Long>> transfers,
            List<TokenNftTransfer> nftTransfers
        ) {
            this.tokenId = tokenId;
            this.expectedDecimals = expectedDecimals;
            this.transfers = transfers;
            this.nftTransfers = nftTransfers;
        }

        public static SortableTokenTransferList forTransfers(Map.Entry<TokenId, TokenTransferMap> transfersEntry) {
            var retval = new SortableTokenTransferList(
                transfersEntry.getKey(),
                transfersEntry.getValue().getExpectedDecimals(),
                new ArrayList<>(),
                Collections.emptyList()
            );
            retval.transfers.addAll(transfersEntry.getValue().transfers.entrySet());
            Collections.sort(retval.transfers, (o1, o2) -> o1.getKey().compareTo(o2.getKey()));
            return retval;
        }

        public static SortableTokenTransferList forNftTransfers(Map.Entry<TokenId, List<TokenNftTransfer>> nftTransfersEntry) {
            var retval = new SortableTokenTransferList(
                nftTransfersEntry.getKey(),
                null,
                Collections.emptyList(),
                nftTransfersEntry.getValue()
            );
            Collections.sort(retval.nftTransfers);
            return retval;
        }

        @Override
        public int compareTo(SortableTokenTransferList o) {
            return tokenId.compareTo(o.tokenId);
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof SortableTokenTransferList)) {
                return false;
            }

            SortableTokenTransferList otherTransferList = (SortableTokenTransferList) o;
            return tokenId.equals(otherTransferList.tokenId);
        }

        @Override
        public int hashCode() {
            return tokenId.hashCode();
        }
    }

    CryptoTransferTransactionBody.Builder build() {
        List<SortableTokenTransferList> transferLists = new ArrayList<>();

        for (var entry : tokenTransfers.entrySet()) {
            transferLists.add(SortableTokenTransferList.forTransfers(entry));
        }

        for (var entry : nftTransfers.entrySet()) {
            transferLists.add(SortableTokenTransferList.forNftTransfers(entry));
        }

        Collections.sort(transferLists);

        List<Map.Entry<AccountId, Hbar>> hbarTransferList = new ArrayList<>();
        hbarTransferList.addAll(hbarTransfers.entrySet());
        Collections.sort(hbarTransferList, (o1, o2) -> o1.getKey().compareTo(o2.getKey()));

        var txBuilder = CryptoTransferTransactionBody.newBuilder();

        for (var list : transferLists) {
            var listBuilder = TokenTransferList.newBuilder()
                .setToken(list.tokenId.toProtobuf());

            if (list.expectedDecimals != null) {
                listBuilder.setExpectedDecimals(list.expectedDecimals);
            }

            for (var transfer : list.transfers) {
                listBuilder.addTransfers(AccountAmount.newBuilder()
                    .setAccountID(transfer.getKey().toProtobuf())
                    .setAmount(transfer.getValue())
                );
            }

            for (var nftTransfer : list.nftTransfers) {
                listBuilder.addNftTransfers(nftTransfer.toProtobuf());
            }

            txBuilder.addTokenTransfers(listBuilder);
        }

        var list = TransferList.newBuilder();
        for (var entry : hbarTransferList) {
            list.addAccountAmounts(AccountAmount.newBuilder()
                .setAccountID(entry.getKey().toProtobuf())
                .setAmount(entry.getValue().toTinybars())
            );
        }
        txBuilder.setTransfers(list);

        return txBuilder;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        for (var a : hbarTransfers.keySet()) {
            a.validateChecksum(client);
        }

        for (var entry : nftTransfers.entrySet()) {
            entry.getKey().validateChecksum(client);

            for (var nftTransfer : entry.getValue()) {
                nftTransfer.sender.validateChecksum(client);
                nftTransfer.receiver.validateChecksum(client);
            }
        }

        for (var entry : tokenTransfers.entrySet()) {
            entry.getKey().validateChecksum(client);

            for (var a : entry.getValue().transfers.keySet()) {
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

    private TokenTransferMap getTokenTransferMap(TokenId tokenId) {
        // Cannot use `Map.merge()` as it uses `BiFunction`
        var map = tokenTransfers.containsKey(tokenId) ?
            Objects.requireNonNull(tokenTransfers.get(tokenId)) :
            new TokenTransferMap();
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

    private void doAddHbarTransfer(AccountId accountId, long amount) {
        Objects.requireNonNull(accountId);
        var current = hbarTransfers.containsKey(accountId) ?
            Objects.requireNonNull(hbarTransfers.get(accountId)).toTinybars() :
            0L;

        hbarTransfers.put(accountId, Hbar.fromTinybars(current + amount));
    }

    void initFromTransactionBody() {
        var body = sourceTransactionBody.getCryptoTransfer();
        if (body.hasTransfers()) {
            for (var transfer : body.getTransfers().getAccountAmountsList()) {
                doAddHbarTransfer(AccountId.fromProtobuf(transfer.getAccountID()), transfer.getAmount());
            }
        }

        for (var tokenTransferList : body.getTokenTransfersList()) {
            var token = TokenId.fromProtobuf(tokenTransferList.getToken());

            if (tokenTransferList.getTransfersCount() > 0) {
                var map = getTokenTransferMap(token);
                if (tokenTransferList.hasExpectedDecimals()) {
                    map.setExpectedDecimals(tokenTransferList.getExpectedDecimals());
                }

                for (var aa : tokenTransferList.getTransfersList()) {
                    doAddTokenTransfer(map.transfers, AccountId.fromProtobuf(aa.getAccountID()), aa.getAmount());
                }
            }

            if (tokenTransferList.getNftTransfersCount() > 0) {
                var list = getNftTransferList(token);
                for (var nftTransfer : tokenTransferList.getNftTransfersList()) {
                    list.add(TokenNftTransfer.fromProtobuf(nftTransfer));
                }
            }
        }
    }
}
