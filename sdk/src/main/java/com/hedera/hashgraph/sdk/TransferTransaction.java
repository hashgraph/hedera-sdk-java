/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.UInt32Value;
import com.hedera.hashgraph.sdk.proto.AccountAmount;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.CryptoTransferTransactionBody;
import com.hedera.hashgraph.sdk.proto.NftTransfer;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.proto.TransferList;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A transaction that transfers hbars and tokens between Hedera accounts.
 * You can enter multiple transfers in a single transaction. The net value
 * of hbars between the sending accounts and receiving accounts must equal
 * zero.
 *
 * See <a href="https://docs.hedera.com/guides/docs/sdks/cryptocurrency/transfer-cryptocurrency">Hedera Documentation</a>
 */
public class TransferTransaction extends Transaction<TransferTransaction> {
    private final ArrayList<TokenTransfer> tokenTransfers = new ArrayList<>();
    private final ArrayList<TokenNftTransfer> nftTransfers = new ArrayList<>();
    private final ArrayList<HbarTransfer> hbarTransfers = new ArrayList<>();

    private static class HbarTransfer {
        final AccountId accountId;
        Hbar amount;
        boolean isApproved;

        HbarTransfer(AccountId accountId, Hbar amount, boolean isApproved) {
            this.accountId = accountId;
            this.amount = amount;
            this.isApproved = isApproved;
        }

        AccountAmount toProtobuf() {
            return AccountAmount.newBuilder()
                .setAccountID(accountId.toProtobuf())
                .setAmount(amount.toTinybars())
                .setIsApproval(isApproved)
                .build();
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                .add("accountId", accountId)
                .add("amount", amount)
                .add("isApproved", isApproved)
                .toString();
        }
    }

    private static class TokenTransferList {
        final TokenId tokenId;

        @Nullable
        final Integer expectDecimals;

        List<TokenTransfer> transfers = new ArrayList<>();
        List<TokenNftTransfer> nftTransfers = new ArrayList<>();

        TokenTransferList(TokenId tokenId, @Nullable Integer expectDecimals, @Nullable TokenTransfer transfer, @Nullable TokenNftTransfer nftTransfer) {
            this.tokenId = tokenId;
            this.expectDecimals = expectDecimals;

            if (transfer != null) {
                this.transfers.add(transfer);
            }

            if (nftTransfer != null) {
                this.nftTransfers.add(nftTransfer);
            }
        }

        com.hedera.hashgraph.sdk.proto.TokenTransferList toProtobuf() {
            var transfers = new ArrayList<AccountAmount>();
            var nftTransfers = new ArrayList<NftTransfer>();

            for (var transfer : this.transfers) {
                transfers.add(transfer.toProtobuf());
            }

            for (var transfer : this.nftTransfers) {
                nftTransfers.add(transfer.toProtobuf());
            }

            var builder = com.hedera.hashgraph.sdk.proto.TokenTransferList.newBuilder()
                .setToken(tokenId.toProtobuf())
                .addAllTransfers(transfers)
                .addAllNftTransfers(nftTransfers);

            if (expectDecimals != null) {
                builder.setExpectedDecimals(UInt32Value.newBuilder().setValue(expectDecimals).build());
            }

            return builder.build();
        }
    }

    /**
     * Constructor.
     */
    public TransferTransaction() {
        defaultMaxTransactionFee = new Hbar(1);
    }

    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction)
     *            records
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    TransferTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * Constructor.
     *
     * @param txBody protobuf TransactionBody
     */
    TransferTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    /**
     * Extract the list of token id decimals.
     *
     * @return                          the list of token id decimals
     */
    public Map<TokenId, Integer> getTokenIdDecimals() {
        Map<TokenId, Integer> decimalsMap = new HashMap<>();

        for (var transfer : tokenTransfers) {
            decimalsMap.put(transfer.tokenId, transfer.expectedDecimals);
        }

        return decimalsMap;
    }

    /**
     * Extract the list of token transfer records.
     *
     * @return                          the list of token transfer records
     */
    public Map<TokenId, Map<AccountId, Long>> getTokenTransfers() {
        Map<TokenId, Map<AccountId, Long>> transfers = new HashMap<>();

        for (var transfer : tokenTransfers) {
            var current = transfers.get(transfer.tokenId) != null
                ? transfers.get(transfer.tokenId) : new HashMap<AccountId, Long>();
            current.put(transfer.accountId, transfer.amount);
            transfers.put(transfer.tokenId, current);
        }

        return transfers;
    }

    /**
     * Add a token transfer to the transaction.
     *
     * @param tokenId                   the token id
     * @param accountId                 the account id
     * @param value                     the value
     * @param isApproved                is it approved
     * @return {@code this}
     */
    private TransferTransaction doAddTokenTransfer(TokenId tokenId, AccountId accountId, long value, boolean isApproved) {
        requireNotFrozen();

        for (var transfer : tokenTransfers) {
            if (transfer.tokenId.equals(tokenId) && transfer.accountId.equals(accountId) && transfer.isApproved == isApproved) {
                transfer.amount = transfer.amount + value;
                return this;
            }
        }

        tokenTransfers.add(new TokenTransfer(tokenId, accountId, value, isApproved));
        return this;
    }

    /**
     * Add a non approved token transfer to the transaction.
     *
     * @param tokenId                   the token id
     * @param accountId                 the account id
     * @param value                     the value
     * @return                          the updated transaction
     */
    public TransferTransaction addTokenTransfer(TokenId tokenId, AccountId accountId, long value) {
        return doAddTokenTransfer(tokenId, accountId, value, false);
    }

    /**
     * Add an approved token transfer to the transaction.
     *
     * @param tokenId                   the token id
     * @param accountId                 the account id
     * @param value                     the value
     * @return                          the updated transaction
     */
    public TransferTransaction addApprovedTokenTransfer(TokenId tokenId, AccountId accountId, long value) {
        return doAddTokenTransfer(tokenId, accountId, value, true);
    }

    private TransferTransaction doAddTokenTransferWithDecimals(
        TokenId tokenId,
        AccountId accountId,
        long value,
        int decimals,
        boolean isApproved
    ) {
        requireNotFrozen();

        var found = false;

        for (var transfer : tokenTransfers) {
            if (transfer.tokenId.equals(tokenId)) {
                if (transfer.expectedDecimals != null && transfer.expectedDecimals != decimals) {
                    throw new IllegalArgumentException("expected decimals for a token in a token transfer cannot be changed after being set");
                }

                transfer.expectedDecimals = decimals;

                if (transfer.accountId.equals(accountId) && transfer.isApproved == isApproved) {
                    transfer.amount = transfer.amount + value;
                    found = true;
                }

            }
        }

        if (found) {
            return this;
        }

        tokenTransfers.add(new TokenTransfer(tokenId, accountId, value, decimals, isApproved));

        return this;
    }

    /**
     * Add a non approved token transfer with decimals.
     *
     * @param tokenId                   the token id
     * @param accountId                 the account id
     * @param value                     the value
     * @param decimals                  the deco
     * @return                          the updated transaction
     */
    public TransferTransaction addTokenTransferWithDecimals(
        TokenId tokenId,
        AccountId accountId,
        long value,
        int decimals
    ) {
        return doAddTokenTransferWithDecimals(tokenId, accountId, value, decimals, false);
    }

    /**
     * Add an approved token transfer with decimals.
     *
     * @param tokenId                   the token id
     * @param accountId                 the account id
     * @param value                     the value
     * @param decimals                  the deco
     * @return                          the updated transaction
     */
    public TransferTransaction addApprovedTokenTransferWithDecimals(
        TokenId tokenId,
        AccountId accountId,
        long value,
        int decimals
    ) {
        return doAddTokenTransferWithDecimals(tokenId, accountId, value, decimals, true);
    }

    /**
     * @deprecated - Use {@link #addApprovedTokenTransfer(TokenId, AccountId, long)} instead
     * @param tokenId       the token id
     * @param accountId     the account id
     * @param isApproved    whether the transfer is approved
     * @return {@code this}
     */
    @Deprecated
    public TransferTransaction setTokenTransferApproval(TokenId tokenId, AccountId accountId, boolean isApproved) {
        requireNotFrozen();

        for (var transfer : tokenTransfers) {
            if (transfer.tokenId.equals(tokenId) && transfer.accountId.equals(accountId)) {
                transfer.isApproved = isApproved;
                return this;
            }
        }

        return this;
    }

    /**
     * Extract the of token nft transfers.
     *
     * @return                          list of token nft transfers
     */
    public Map<TokenId, List<TokenNftTransfer>> getTokenNftTransfers() {
        Map<TokenId, List<TokenNftTransfer>> transfers = new HashMap<>();

        for (var transfer : nftTransfers) {
            var current = transfers.get(transfer.tokenId) != null
                ? transfers.get(transfer.tokenId) : new ArrayList<TokenNftTransfer>();
            current.add(transfer);
            transfers.put(transfer.tokenId, current);
        }

        return transfers;
    }

    private TransferTransaction doAddNftTransfer(NftId nftId, AccountId sender, AccountId receiver, boolean isApproved) {
        requireNotFrozen();
        nftTransfers.add(new TokenNftTransfer(nftId.tokenId, sender, receiver, nftId.serial, isApproved));
        return this;
    }

    /**
     * Add a non approved nft transfer.
     *
     * @param nftId                     the nft's id
     * @param sender                    the sender account id
     * @param receiver                  the receiver account id
     * @return                          the updated transaction
     */
    public TransferTransaction addNftTransfer(NftId nftId, AccountId sender, AccountId receiver) {
        return doAddNftTransfer(nftId, sender, receiver, false);
    }

    /**
     * Add an approved nft transfer.
     *
     * @param nftId                     the nft's id
     * @param sender                    the sender account id
     * @param receiver                  the receiver account id
     * @return                          the updated transaction
     */
    public TransferTransaction addApprovedNftTransfer(NftId nftId, AccountId sender, AccountId receiver) {
        return doAddNftTransfer(nftId, sender, receiver, true);
    }

    /**
     * @deprecated - Use {@link #addApprovedNftTransfer(NftId, AccountId, AccountId)} instead
     * @param nftId         the NFT id
     * @param isApproved    whether the transfer is approved
     * @return {@code this}
     */
    @Deprecated
    public TransferTransaction setNftTransferApproval(NftId nftId, boolean isApproved) {
        requireNotFrozen();

        for (var transfer : nftTransfers) {
            if (transfer.tokenId.equals(nftId.tokenId) && transfer.serial == nftId.serial) {
                transfer.isApproved = isApproved;
                return this;
            }
        }

        return this;
    }

    /**
     * Extract the of hbar transfers.
     *
     * @return                          list of hbar transfers
     */
    public Map<AccountId, Hbar> getHbarTransfers() {
        Map<AccountId, Hbar> transfers = new HashMap<>();

        for (var transfer : hbarTransfers) {
            transfers.put(transfer.accountId, transfer.amount);
        }

        return transfers;
    }

    private TransferTransaction doAddHbarTransfer(AccountId accountId, Hbar value, boolean isApproved) {
        requireNotFrozen();

        for (var transfer : hbarTransfers) {
            if (transfer.accountId.equals(accountId) && transfer.isApproved == isApproved) {
                transfer.amount = Hbar.fromTinybars(transfer.amount.toTinybars() + value.toTinybars());
                return this;
            }
        }

        hbarTransfers.add(new HbarTransfer(accountId, value, isApproved));
        return this;
    }

    /**
     * Add a non approved hbar transfer to an EVM address.
     *
     * @param evmAddress                the EVM address
     * @param value                     the value
     * @return                          the updated transaction
     */
    public TransferTransaction addHbarTransfer(EvmAddress evmAddress, Hbar value) {
        AccountId accountId = AccountId.fromEvmAddress(evmAddress);
        return doAddHbarTransfer(accountId, value, false);
    }

    /**
     * Add a non approved hbar transfer.
     *
     * @param accountId                 the account id
     * @param value                     the value
     * @return                          the updated transaction
     */
    public TransferTransaction addHbarTransfer(AccountId accountId, Hbar value) {
        return doAddHbarTransfer(accountId, value, false);
    }

    /**
     * Add an approved hbar transfer.
     *
     * @param accountId                 the account id
     * @param value                     the value
     * @return                          the updated transaction
     */
    public TransferTransaction addApprovedHbarTransfer(AccountId accountId, Hbar value) {
        return doAddHbarTransfer(accountId, value, true);
    }

    /**
     * @deprecated - Use {@link #addApprovedHbarTransfer(AccountId, Hbar)} instead
     * @param accountId     the account id
     * @param isApproved    whether the transfer is approved
     * @return {@code this}
     */
    @Deprecated
    public TransferTransaction setHbarTransferApproval(AccountId accountId, boolean isApproved) {
        requireNotFrozen();

        for (var transfer : hbarTransfers) {
            if (transfer.accountId.equals(accountId)) {
                transfer.isApproved = isApproved;
                return this;
            }
        }

        return this;
    }

    /**
     * Build the transaction body.
     *
     * @return {@link
     *         com.hedera.hashgraph.sdk.proto.CryptoTransferTransactionBody}
     */
    CryptoTransferTransactionBody.Builder build() {
        var tokenTransfers = new ArrayList<TokenTransferList>();

        this.hbarTransfers.sort(Comparator.comparing((HbarTransfer a) -> a.accountId).thenComparing(a -> a.isApproved));
        this.tokenTransfers.sort(Comparator.comparing((TokenTransfer a) -> a.tokenId).thenComparing(a -> a.accountId).thenComparing(a -> a.isApproved));
        this.nftTransfers.sort(Comparator.comparing((TokenNftTransfer a) -> a.tokenId).thenComparing(a -> a.sender).thenComparing(a -> a.receiver).thenComparing(a -> a.serial));

        var i = 0;
        var j = 0;

        // Effectively merge sort
        while (i < this.tokenTransfers.size() || j < this.nftTransfers.size()) {
            if (i < this.tokenTransfers.size() && j < this.nftTransfers.size()) {
                var iTokenId = this.tokenTransfers.get(i).tokenId;
                var jTokenId = this.nftTransfers.get(j).tokenId;
                var last = !tokenTransfers.isEmpty() ? tokenTransfers.get(tokenTransfers.size() - 1) : null;
                var lastTokenId = last != null ? last.tokenId : null;

                if (last != null && iTokenId.compareTo(lastTokenId) == 0) {
                    last.transfers.add(this.tokenTransfers.get(i++));
                    continue;
                }

                if (last != null && jTokenId.compareTo(lastTokenId) == 0) {
                    last.nftTransfers.add(this.nftTransfers.get(j++));
                    continue;
                }

                var result = iTokenId.compareTo(jTokenId);

                if (result == 0) {
                    tokenTransfers.add(new TokenTransferList(iTokenId, this.tokenTransfers.get(i).expectedDecimals, this.tokenTransfers.get(i++), this.nftTransfers.get(j++)));
                } else if (result < 0) {
                    tokenTransfers.add(new TokenTransferList(iTokenId, this.tokenTransfers.get(i).expectedDecimals, this.tokenTransfers.get(i++), null));
                } else {
                    tokenTransfers.add(new TokenTransferList(jTokenId, null, null, this.nftTransfers.get(j++)));
                }
            } else if (i < this.tokenTransfers.size()) {
                var iTokenId = this.tokenTransfers.get(i).tokenId;
                var last = !tokenTransfers.isEmpty() ? tokenTransfers.get(tokenTransfers.size() - 1) : null;
                var lastTokenId = last != null ? last.tokenId : null;

                if (last != null && iTokenId.compareTo(lastTokenId) == 0) {
                    last.transfers.add(this.tokenTransfers.get(i++));
                    continue;
                }

                tokenTransfers.add(new TokenTransferList(iTokenId, this.tokenTransfers.get(i).expectedDecimals, this.tokenTransfers.get(i++), null));
            } else {
                var jTokenId = this.nftTransfers.get(j).tokenId;
                var last = !tokenTransfers.isEmpty() ? tokenTransfers.get(tokenTransfers.size() - 1) : null;
                var lastTokenId = last != null ? last.tokenId : null;

                if (last != null && jTokenId.compareTo(lastTokenId) == 0) {
                    last.nftTransfers.add(this.nftTransfers.get(j++));
                    continue;
                }

                tokenTransfers.add(new TokenTransferList(jTokenId, null, null, this.nftTransfers.get(j++)));
            }
        }

        var builder = CryptoTransferTransactionBody.newBuilder();

        var transfers = TransferList.newBuilder();
        for (var transfer : hbarTransfers) {
            transfers.addAccountAmounts(transfer.toProtobuf());
        }
        builder.setTransfers(transfers);

        for (var transfer : tokenTransfers) {
            builder.addTokenTransfers(transfer.toProtobuf());
        }

        return builder;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        for (var transfer : hbarTransfers) {
            transfer.accountId.validateChecksum(client);
        }

        for (var transfer : nftTransfers) {
            transfer.tokenId.validateChecksum(client);
            transfer.sender.validateChecksum(client);
            transfer.receiver.validateChecksum(client);
        }

        for (var transfer : tokenTransfers) {
            transfer.tokenId.validateChecksum(client);
            transfer.accountId.validateChecksum(client);
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

    /**
     * Initialize from the transaction body.
     */
    void initFromTransactionBody() {
        var body = sourceTransactionBody.getCryptoTransfer();

        for (var transfer : body.getTransfers().getAccountAmountsList()) {
            hbarTransfers.add(new HbarTransfer(
                AccountId.fromProtobuf(transfer.getAccountID()),
                Hbar.fromTinybars(transfer.getAmount()),
                transfer.getIsApproval()
            ));
        }

        for (var tokenTransferList : body.getTokenTransfersList()) {
            var token = TokenId.fromProtobuf(tokenTransferList.getToken());

            for (var transfer : tokenTransferList.getTransfersList()) {
                tokenTransfers.add(new TokenTransfer(
                    token,
                    AccountId.fromProtobuf(transfer.getAccountID()),
                    transfer.getAmount(),
                    tokenTransferList.hasExpectedDecimals() ? tokenTransferList.getExpectedDecimals().getValue() : null,
                    transfer.getIsApproval()
                ));
            }

            for (var transfer : tokenTransferList.getNftTransfersList()) {
                nftTransfers.add(new TokenNftTransfer(
                    token,
                    AccountId.fromProtobuf(transfer.getSenderAccountID()),
                    AccountId.fromProtobuf(transfer.getReceiverAccountID()),
                    transfer.getSerialNumber(),
                    transfer.getIsApproval()
                ));
            }
        }
    }
}
