package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenAirdropTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TokenAirdropTransaction  extends Transaction<TokenAirdropTransaction> {

    private final ArrayList<TokenTransfer> tokenTransfers = new ArrayList<>();
    private final ArrayList<TokenNftTransfer> nftTransfers = new ArrayList<>();

    /**
     * Constructor.
     */
    public TokenAirdropTransaction() {
        defaultMaxTransactionFee = new Hbar(1);
    }

    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction)
     *            records
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    TokenAirdropTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * Constructor.
     *
     * @param txBody protobuf TransactionBody
     */
    TokenAirdropTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
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

    private TokenAirdropTransaction doAddTokenTransfer(TokenId tokenId, AccountId accountId, long value, boolean isApproved) {
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
     * Add a non-approved token transfer to the transaction.
     *
     * @param tokenId                   the token id
     * @param accountId                 the account id
     * @param value                     the value
     * @return                          the updated transaction
     */
    public TokenAirdropTransaction addTokenTransfer(TokenId tokenId, AccountId accountId, long value) {
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
    public TokenAirdropTransaction addApprovedTokenTransfer(TokenId tokenId, AccountId accountId, long value) {
        return doAddTokenTransfer(tokenId, accountId, value, true);
    }

    private TokenAirdropTransaction doAddTokenTransferWithDecimals(
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
     * Add a non-approved token transfer with decimals.
     *
     * @param tokenId                   the token id
     * @param accountId                 the account id
     * @param value                     the value
     * @param decimals                  the decimals
     * @return                          the updated transaction
     */
    public TokenAirdropTransaction addTokenTransferWithDecimals(
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
     * @param decimals                  the decimals
     * @return                          the updated transaction
     */
    public TokenAirdropTransaction addApprovedTokenTransferWithDecimals(
        TokenId tokenId,
        AccountId accountId,
        long value,
        int decimals
    ) {
        return doAddTokenTransferWithDecimals(tokenId, accountId, value, decimals, true);
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

    private TokenAirdropTransaction doAddNftTransfer(NftId nftId, AccountId sender, AccountId receiver, boolean isApproved) {
        requireNotFrozen();
        nftTransfers.add(new TokenNftTransfer(nftId.tokenId, sender, receiver, nftId.serial, isApproved));
        return this;
    }

    /**
     * Add a non-approved nft transfer.
     *
     * @param nftId                     the nft's id
     * @param sender                    the sender account id
     * @param receiver                  the receiver account id
     * @return                          the updated transaction
     */
    public TokenAirdropTransaction addNftTransfer(NftId nftId, AccountId sender, AccountId receiver) {
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
    public TokenAirdropTransaction addApprovedNftTransfer(NftId nftId, AccountId sender, AccountId receiver) {
        return doAddNftTransfer(nftId, sender, receiver, true);
    }

    /**
     * Build the transaction body.
     *
     * @return {@link
     *         com.hedera.hashgraph.sdk.proto.TokenAirdropTransactionBody}
     */
    TokenAirdropTransactionBody.Builder build() {
        var tokenTransfers = new ArrayList<com.hedera.hashgraph.sdk.TokenTransferList>();

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
                    tokenTransfers.add(new com.hedera.hashgraph.sdk.TokenTransferList(iTokenId, this.tokenTransfers.get(i).expectedDecimals, this.tokenTransfers.get(i++), this.nftTransfers.get(j++)));
                } else if (result < 0) {
                    tokenTransfers.add(new com.hedera.hashgraph.sdk.TokenTransferList(iTokenId, this.tokenTransfers.get(i).expectedDecimals, this.tokenTransfers.get(i++), null));
                } else {
                    tokenTransfers.add(new com.hedera.hashgraph.sdk.TokenTransferList(jTokenId, null, null, this.nftTransfers.get(j++)));
                }
            } else if (i < this.tokenTransfers.size()) {
                var iTokenId = this.tokenTransfers.get(i).tokenId;
                var last = !tokenTransfers.isEmpty() ? tokenTransfers.get(tokenTransfers.size() - 1) : null;
                var lastTokenId = last != null ? last.tokenId : null;

                if (last != null && iTokenId.compareTo(lastTokenId) == 0) {
                    last.transfers.add(this.tokenTransfers.get(i++));
                    continue;
                }

                tokenTransfers.add(new com.hedera.hashgraph.sdk.TokenTransferList(iTokenId, this.tokenTransfers.get(i).expectedDecimals, this.tokenTransfers.get(i++), null));
            } else {
                var jTokenId = this.nftTransfers.get(j).tokenId;
                var last = !tokenTransfers.isEmpty() ? tokenTransfers.get(tokenTransfers.size() - 1) : null;
                var lastTokenId = last != null ? last.tokenId : null;

                if (last != null && jTokenId.compareTo(lastTokenId) == 0) {
                    last.nftTransfers.add(this.nftTransfers.get(j++));
                    continue;
                }

                tokenTransfers.add(new com.hedera.hashgraph.sdk.TokenTransferList(jTokenId, null, null, this.nftTransfers.get(j++)));
            }
        }

        var builder = TokenAirdropTransactionBody.newBuilder();

        for (var transfer : tokenTransfers) {
            builder.addTokenTransfers(transfer.toProtobuf());
        }

        return builder;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
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
        return TokenServiceGrpc.getAirdropTokensMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenAirdrop(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenAirdrop(build());
    }

    /**
     * Initialize from the transaction body.
     */
    void initFromTransactionBody() {
        var body = sourceTransactionBody.getTokenAirdrop();

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
