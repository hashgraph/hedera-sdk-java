package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.CryptoAdjustAllowanceTransactionBody;
import com.hedera.hashgraph.sdk.proto.CryptoApproveAllowanceTransactionBody;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.CryptoUpdateTransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AccountAllowanceAdjustTransaction extends Transaction<AccountAllowanceAdjustTransaction> {
private final List<HbarAllowance> hbarAllowances = new ArrayList<>();
    private final List<TokenAllowance> tokenAllowances =  new ArrayList<>();
    private final List<TokenNftAllowance> nftAllowances = new ArrayList<>();
    // key is "{ownerId}:{spenderId}".  OwnerId may be "FEE_PAYER"
    private final Map<String, Map<TokenId, Integer>> nftMap = new HashMap<>();

    public AccountAllowanceAdjustTransaction() {
    }

    AccountAllowanceAdjustTransaction(
        LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs
    ) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    AccountAllowanceAdjustTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    private void initFromTransactionBody() {
        var body = sourceTransactionBody.getCryptoAdjustAllowance();
        for (var allowanceProto : body.getCryptoAllowancesList()) {
            hbarAllowances.add(HbarAllowance.fromProtobuf(allowanceProto));
        }
        for (var allowanceProto : body.getTokenAllowancesList()) {
            tokenAllowances.add(TokenAllowance.fromProtobuf(allowanceProto));
        }
        for (var allowanceProto : body.getNftAllowancesList()) {
            if (allowanceProto.hasApprovedForAll() && allowanceProto.getApprovedForAll().getValue()) {
                nftAllowances.add(TokenNftAllowance.fromProtobuf(allowanceProto));
            } else {
                getNftSerials(
                    AccountId.fromProtobuf(allowanceProto.getOwner()),
                    AccountId.fromProtobuf(allowanceProto.getSpender()),
                    TokenId.fromProtobuf(allowanceProto.getTokenId())
                ).addAll(allowanceProto.getSerialNumbersList());
            }
        }
    }

    public AccountAllowanceAdjustTransaction addHbarAllowance(AccountId spenderAccountId, Hbar amount) {
        hbarAllowances.add(new HbarAllowance(
            null,
            Objects.requireNonNull(spenderAccountId),
            Objects.requireNonNull(amount)
        ));
        return this;
    }

    public AccountAllowanceAdjustTransaction addHbarAllowanceWithOwner(
        AccountId spenderAccountId,
        Hbar amount,
        AccountId ownerAccountId
    ) {
        hbarAllowances.add(new HbarAllowance(
            Objects.requireNonNull(ownerAccountId),
            Objects.requireNonNull(spenderAccountId),
            Objects.requireNonNull(amount)
        ));
        return this;
    }

    public List<HbarAllowance> getHbarAllowances() {
        return new ArrayList<>(hbarAllowances);
    }

    public AccountAllowanceAdjustTransaction addTokenAllowance(TokenId tokenId, AccountId spenderAccountId, long amount) {
        tokenAllowances.add(new TokenAllowance(
            Objects.requireNonNull(tokenId),
            null,
            Objects.requireNonNull(spenderAccountId),
            amount
        ));
        return this;
    }

    public AccountAllowanceAdjustTransaction addTokenAllowanceWithOwner(
        TokenId tokenId,
        AccountId spenderAccountId,
        long amount,
        AccountId ownerAccountId
    ) {
        tokenAllowances.add(new TokenAllowance(
            Objects.requireNonNull(tokenId),
            Objects.requireNonNull(ownerAccountId),
            Objects.requireNonNull(spenderAccountId),
            amount
        ));
        return this;
    }

    public List<TokenAllowance> getTokenAllowances() {
        return new ArrayList<>(tokenAllowances);
    }

    private static String ownerToString(@Nullable AccountId ownerAccountId) {
        return ownerAccountId != null ? ownerAccountId.toString() : "FEE_PAYER";
    }

    private List<Long> getNftSerials(@Nullable AccountId ownerAccountId, AccountId spenderAccountId, TokenId tokenId) {
        var key = ownerToString(ownerAccountId) + ":" + spenderAccountId;
        if (nftMap.containsKey(key)) {
            var innerMap = nftMap.get(key);
            if (innerMap.containsKey(tokenId)) {
                return Objects.requireNonNull(nftAllowances.get(innerMap.get(tokenId)).serialNumbers);
            } else {
                return newNftSerials(ownerAccountId, spenderAccountId, tokenId, innerMap);
            }
        } else {
            Map<TokenId, Integer> innerMap = new HashMap<>();
            nftMap.put(key, innerMap);
            return newNftSerials(ownerAccountId, spenderAccountId, tokenId, innerMap);
        }
    }

    private List<Long> newNftSerials(
        @Nullable AccountId ownerAccountId,
        AccountId spenderAccountId,
        TokenId tokenId,
        Map<TokenId, Integer> innerMap
    ) {
        innerMap.put(tokenId, nftAllowances.size());
        TokenNftAllowance newAllowance = new TokenNftAllowance(tokenId, ownerAccountId, spenderAccountId, new ArrayList<>());
        nftAllowances.add(newAllowance);
        return newAllowance.serialNumbers;
    }

    public AccountAllowanceAdjustTransaction addTokenNftAllowance(NftId nftId, AccountId spenderAccountId) {
        Objects.requireNonNull(nftId);
        getNftSerials(
            null,
            Objects.requireNonNull(spenderAccountId),
            nftId.tokenId
        ).add(nftId.serial);
        return this;
    }

    public AccountAllowanceAdjustTransaction addAllTokenNftAllowance(TokenId tokenId, AccountId spenderAccountId) {
        nftAllowances.add(new TokenNftAllowance(
            Objects.requireNonNull(tokenId),
            null,
            Objects.requireNonNull(spenderAccountId),
            null
        ));
        return this;
    }

    public AccountAllowanceAdjustTransaction addTokenNftAllowanceWithOwner(
        NftId nftId,
        AccountId spenderAccountId,
        AccountId ownerAccountId
    ) {
        Objects.requireNonNull(nftId);
        getNftSerials(
            Objects.requireNonNull(ownerAccountId),
            Objects.requireNonNull(spenderAccountId),
            nftId.tokenId
        ).add(nftId.serial);
        return this;
    }

    public AccountAllowanceAdjustTransaction addAllTokenNftAllowanceWithOwner(
        TokenId tokenId,
        AccountId spenderAccountId,
        AccountId ownerAccountId
    ) {
        nftAllowances.add(new TokenNftAllowance(
            Objects.requireNonNull(tokenId),
            Objects.requireNonNull(ownerAccountId),
            Objects.requireNonNull(spenderAccountId),
            null
        ));
        return this;
    }

    public List<TokenNftAllowance> getTokenNftAllowances() {
        List<TokenNftAllowance> retval = new ArrayList<>(nftAllowances.size());
        for (var allowance : nftAllowances) {
            retval.add(TokenNftAllowance.copyFrom(allowance));
        }
        return retval;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return CryptoServiceGrpc.getAdjustAllowanceMethod();
    }

    CryptoAdjustAllowanceTransactionBody.Builder build() {
        var builder = CryptoAdjustAllowanceTransactionBody.newBuilder();

        @Nullable
        AccountId ownerAccountId = (transactionIds.size() > 0 && transactionIds.get(0) != null) ?
            transactionIds.get(0).accountId : null;

        for (var allowance : hbarAllowances) {
            builder.addCryptoAllowances(allowance.withOwner(ownerAccountId).toProtobuf());
        }
        for (var allowance : tokenAllowances) {
            builder.addTokenAllowances(allowance.withOwner(ownerAccountId).toProtobuf());
        }
        for (var allowance : nftAllowances) {
            builder.addNftAllowances(allowance.withOwner(ownerAccountId).toProtobuf());
        }
        return builder;
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setCryptoAdjustAllowance(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setCryptoAdjustAllowance(build());
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        for (var allowance : hbarAllowances) {
            if (allowance.spenderAccountId != null) {
                allowance.spenderAccountId.validateChecksum(client);
            }
        }
        for (var allowance : tokenAllowances) {
            if (allowance.spenderAccountId != null) {
                allowance.spenderAccountId.validateChecksum(client);
            }
            if (allowance.tokenId != null) {
                allowance.tokenId.validateChecksum(client);
            }
        }
        for (var allowance : nftAllowances) {
            if (allowance.spenderAccountId != null) {
                allowance.spenderAccountId.validateChecksum(client);
            }
            if (allowance.tokenId != null) {
                allowance.tokenId.validateChecksum(client);
            }
        }
    }
}
