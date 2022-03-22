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

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
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
            if (allowanceProto.hasApprovedForAll()) {
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

    private AccountAllowanceAdjustTransaction adjustHbarAllowance(
        @Nullable AccountId ownerAccountId,
        AccountId spenderAccountId,
        Hbar amount
    ) {
        requireNotFrozen();
        hbarAllowances.add(new HbarAllowance(ownerAccountId, Objects.requireNonNull(spenderAccountId), amount));
        return this;
    }

    /**
     * @deprecated - Use {@link #grantHbarAllowance(AccountId, AccountId, Hbar)} or
     * {@link #revokeHbarAllowance(AccountId, AccountId, Hbar)} instead
     */
    @Deprecated
    public AccountAllowanceAdjustTransaction addHbarAllowance(AccountId spenderAccountId, Hbar amount) {
        return adjustHbarAllowance(null, spenderAccountId, Objects.requireNonNull(amount));
    }

    public AccountAllowanceAdjustTransaction grantHbarAllowance(
        AccountId ownerAccountId,
        AccountId spenderAccountId,
        Hbar amount
    ) {
        Objects.requireNonNull(amount);
        if (amount.compareTo(Hbar.ZERO) < 0) {
            throw new IllegalArgumentException("amount passed to grantHbarAllowance must be positive");
        }
        return adjustHbarAllowance(Objects.requireNonNull(ownerAccountId), spenderAccountId, amount);
    }

    public AccountAllowanceAdjustTransaction revokeHbarAllowance(
        AccountId ownerAccountId,
        AccountId spenderAccountId,
        Hbar amount
    ) {
        Objects.requireNonNull(amount);
        if (amount.compareTo(Hbar.ZERO) < 0) {
            throw new IllegalArgumentException("amount passed to revokeHbarAllowance must be positive");
        }
        return adjustHbarAllowance(Objects.requireNonNull(ownerAccountId), spenderAccountId, amount.negated());
    }

    public List<HbarAllowance> getHbarAllowances() {
        return new ArrayList<>(hbarAllowances);
    }

    private AccountAllowanceAdjustTransaction adjustTokenAllowance(
        TokenId tokenId,
        @Nullable AccountId ownerAccountId,
        AccountId spenderAccountId,
        long amount
    ) {
        requireNotFrozen();
        tokenAllowances.add(new TokenAllowance(
            Objects.requireNonNull(tokenId),
            ownerAccountId,
            Objects.requireNonNull(spenderAccountId),
            amount
        ));
        return this;
    }

    /**
     * @deprecated - Use {@link #grantTokenAllowance(TokenId, AccountId, AccountId, long)} or
     * {@link #revokeTokenAllowance(TokenId, AccountId, AccountId, long)} instead
     */
    @Deprecated
    public AccountAllowanceAdjustTransaction addTokenAllowance(TokenId tokenId, AccountId spenderAccountId, long amount) {
        return adjustTokenAllowance(tokenId, null, spenderAccountId, amount);
    }

    public AccountAllowanceAdjustTransaction grantTokenAllowance(
        TokenId tokenId,
        AccountId ownerAccountId,
        AccountId spenderAccountId,
        @Nonnegative long amount
    ) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount passed to grantTokenAllowance must be positive");
        }
        return adjustTokenAllowance(tokenId, Objects.requireNonNull(ownerAccountId), spenderAccountId, amount);
    }

    public AccountAllowanceAdjustTransaction revokeTokenAllowance(
        TokenId tokenId,
        AccountId ownerAccountId,
        AccountId spenderAccountId,
        @Nonnegative long amount
    ) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount passed to revokeTokenAllowance must be positive");
        }
        return adjustTokenAllowance(tokenId, Objects.requireNonNull(ownerAccountId), spenderAccountId, -amount);
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
        TokenNftAllowance newAllowance = new TokenNftAllowance(tokenId, ownerAccountId, spenderAccountId, new ArrayList<>(), null);
        nftAllowances.add(newAllowance);
        return newAllowance.serialNumbers;
    }

    private AccountAllowanceAdjustTransaction adjustNftAllowance(
        TokenId tokenId,
        long serial,
        @Nullable AccountId ownerAccountId,
        AccountId spenderAccountId
    ) {
        requireNotFrozen();
        getNftSerials(ownerAccountId, Objects.requireNonNull(spenderAccountId), tokenId).add(serial);
        return this;
    }

    private AccountAllowanceAdjustTransaction adjustNftAllowanceAllSerials(
        TokenId tokenId,
        boolean allSerials,
        @Nullable AccountId ownerAccountId,
        AccountId spenderAccountId
    ) {
        requireNotFrozen();
        nftAllowances.add(new TokenNftAllowance(
            Objects.requireNonNull(tokenId),
            ownerAccountId,
            Objects.requireNonNull(spenderAccountId),
            Collections.emptyList(),
            allSerials
        ));
        return this;
    }

    /**
     * @deprecated - Use {@link #grantTokenNftAllowance(NftId, AccountId, AccountId)} or
     * {@link #revokeTokenNftAllowance(NftId, AccountId, AccountId)} instead
     */
    @Deprecated
    public AccountAllowanceAdjustTransaction addTokenNftAllowance(NftId nftId, AccountId spenderAccountId) {
        Objects.requireNonNull(nftId);
        return adjustNftAllowance(nftId.tokenId, nftId.serial, null, spenderAccountId);
    }

    /**
     * @deprecated - Use {@link #grantTokenNftAllowanceAllSerials(TokenId, AccountId, AccountId)} or
     * {@link #revokeTokenNftAllowanceAllSerials(TokenId, AccountId, AccountId)} instead
     */
    @Deprecated
    public AccountAllowanceAdjustTransaction addAllTokenNftAllowance(TokenId tokenId, AccountId spenderAccountId) {
        return adjustNftAllowanceAllSerials(tokenId, true, null, spenderAccountId);
    }

    public AccountAllowanceAdjustTransaction grantTokenNftAllowance(
        NftId nftId,
        AccountId ownerAccountId,
        AccountId spenderAccountId
    ) {
        Objects.requireNonNull(nftId);
        Objects.requireNonNull(ownerAccountId);
        return adjustNftAllowance(nftId.tokenId, nftId.serial, ownerAccountId, spenderAccountId);
    }

    public AccountAllowanceAdjustTransaction grantTokenNftAllowanceAllSerials(
        TokenId tokenId,
        AccountId ownerAccountId,
        AccountId spenderAccountId
    ) {
        Objects.requireNonNull(ownerAccountId);
        return adjustNftAllowanceAllSerials(tokenId, true, ownerAccountId, spenderAccountId);
    }

    public AccountAllowanceAdjustTransaction revokeTokenNftAllowance(
        NftId nftId,
        AccountId ownerAccountId,
        AccountId spenderAccountId
    ) {
        Objects.requireNonNull(nftId);
        Objects.requireNonNull(ownerAccountId);
        return adjustNftAllowance(nftId.tokenId, -nftId.serial, ownerAccountId, spenderAccountId);
    }

    public AccountAllowanceAdjustTransaction revokeTokenNftAllowanceAllSerials(
        TokenId tokenId,
        AccountId ownerAccountId,
        AccountId spenderAccountId
    ) {
        Objects.requireNonNull(ownerAccountId);
        return adjustNftAllowanceAllSerials(tokenId, false, ownerAccountId, spenderAccountId);
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
