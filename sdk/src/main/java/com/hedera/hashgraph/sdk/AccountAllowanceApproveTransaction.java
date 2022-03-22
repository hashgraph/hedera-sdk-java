package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.CryptoApproveAllowanceTransactionBody;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AccountAllowanceApproveTransaction extends Transaction<AccountAllowanceApproveTransaction> {
    private final List<HbarAllowance> hbarAllowances = new ArrayList<>();
    private final List<TokenAllowance> tokenAllowances =  new ArrayList<>();
    private final List<TokenNftAllowance> nftAllowances = new ArrayList<>();
    // key is "{ownerId}:{spenderId}".  OwnerId may be "FEE_PAYER"
    private final Map<String, Map<TokenId, Integer>> nftMap = new HashMap<>();

    public AccountAllowanceApproveTransaction() {
    }

    AccountAllowanceApproveTransaction(
        LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs
    ) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    AccountAllowanceApproveTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    private void initFromTransactionBody() {
        var body = sourceTransactionBody.getCryptoApproveAllowance();
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

    private AccountAllowanceApproveTransaction doApproveHbarAllowance(
        @Nullable AccountId ownerAccountId,
        AccountId spenderAccountId,
        Hbar amount
    ) {
        requireNotFrozen();
        Objects.requireNonNull(amount);
        if (amount.compareTo(Hbar.ZERO) < 0) {
            throw new IllegalArgumentException("amount passed to approveHbarAllowance must be positive");
        }
        hbarAllowances.add(new HbarAllowance(ownerAccountId, Objects.requireNonNull(spenderAccountId), amount));
        return this;
    }

    /**
     * @deprecated - Use {@link #approveHbarAllowance(AccountId, AccountId, Hbar)} instead
     */
    @Deprecated
    public AccountAllowanceApproveTransaction addHbarAllowance(AccountId spenderAccountId, Hbar amount) {
        return doApproveHbarAllowance(null, spenderAccountId, amount);
    }

    public AccountAllowanceApproveTransaction approveHbarAllowance(
        AccountId ownerAccountId,
        AccountId spenderAccountId,
        Hbar amount
    ) {
        return doApproveHbarAllowance(Objects.requireNonNull(ownerAccountId), spenderAccountId, amount);
    }

    /**
     * @deprecated - Use {@link #getHbarApprovals()} instead
     */
    @Deprecated
    public List<HbarAllowance> getHbarAllowances() {
        return getHbarApprovals();
    }

    public List<HbarAllowance> getHbarApprovals() {
        return new ArrayList<>(hbarAllowances);
    }

    private AccountAllowanceApproveTransaction doApproveTokenAllowance(
        TokenId tokenId,
        @Nullable AccountId ownerAccountId,
        AccountId spenderAccountId,
        long amount
    ) {
        requireNotFrozen();
        if (amount < 0) {
            throw new IllegalArgumentException("amount given to approveTokenAllowance must be positive");
        }
        tokenAllowances.add(new TokenAllowance(
            Objects.requireNonNull(tokenId),
            ownerAccountId,
            Objects.requireNonNull(spenderAccountId),
            amount
        ));
        return this;
    }

    /**
     * @deprecated - Use {@link #approveTokenAllowance(TokenId, AccountId, AccountId, long)} instead
     */
    @Deprecated
    public AccountAllowanceApproveTransaction addTokenAllowance(
        TokenId tokenId,
        AccountId spenderAccountId,
        long amount
    ) {
        return doApproveTokenAllowance(tokenId, null, spenderAccountId, amount);
    }

    public AccountAllowanceApproveTransaction approveTokenAllowance(
        TokenId tokenId,
        AccountId ownerAccountId,
        AccountId spenderAccountId,
        long amount
    ) {
        return doApproveTokenAllowance(tokenId, Objects.requireNonNull(ownerAccountId), spenderAccountId, amount);
    }

    /**
     * @deprecated - Use {@link #getTokenApprovals()} instead
     */
    @Deprecated
    public List<TokenAllowance> getTokenAllowances() {
        return getTokenApprovals();
    }

    public List<TokenAllowance> getTokenApprovals() {
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

    /**
     * @deprecated - Use {@link #approveTokenNftAllowance(NftId, AccountId, AccountId)} instead
     */
    @Deprecated
    public AccountAllowanceApproveTransaction addTokenNftAllowance(NftId nftId, AccountId spenderAccountId) {
        requireNotFrozen();
        getNftSerials(null, spenderAccountId, nftId.tokenId).add(nftId.serial);
        return this;
    }

    /**
     * @deprecated - Use {@link #approveTokenNftAllowanceAllSerials(TokenId, AccountId, AccountId)} instead
     */
    @Deprecated
    public AccountAllowanceApproveTransaction addAllTokenNftAllowance(TokenId tokenId, AccountId spenderAccountId) {
        requireNotFrozen();
        nftAllowances.add(new TokenNftAllowance(
            tokenId,
            null, spenderAccountId,
            Collections.emptyList(),
            true
        ));
        return this;
    }

    public AccountAllowanceApproveTransaction approveTokenNftAllowance(
        NftId nftId,
        AccountId ownerAccountId,
        AccountId spenderAccountId
    ) {
        requireNotFrozen();
        Objects.requireNonNull(nftId);
        getNftSerials(
            Objects.requireNonNull(ownerAccountId),
            Objects.requireNonNull(spenderAccountId),
            nftId.tokenId
        ).add(nftId.serial);
        return this;
    }

    public AccountAllowanceApproveTransaction approveTokenNftAllowanceAllSerials(
        TokenId tokenId,
        AccountId ownerAccountId,
        AccountId spenderAccountId
    ) {
        requireNotFrozen();
        nftAllowances.add(new TokenNftAllowance(
            Objects.requireNonNull(tokenId),
            Objects.requireNonNull(ownerAccountId),
            Objects.requireNonNull(spenderAccountId),
            Collections.emptyList(),
            true
        ));
        return this;
    }

    /**
     * @deprecated - Use {@link #getTokenNftApprovals()} instead
     */
    @Deprecated
    public List<TokenNftAllowance> getTokenNftAllowances() {
        return getTokenNftApprovals();
    }

    public List<TokenNftAllowance> getTokenNftApprovals() {
        List<TokenNftAllowance> retval = new ArrayList<>(nftAllowances.size());
        for (var allowance : nftAllowances) {
            retval.add(TokenNftAllowance.copyFrom(allowance));
        }
        return retval;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return CryptoServiceGrpc.getApproveAllowancesMethod();
    }

    CryptoApproveAllowanceTransactionBody.Builder build() {
        var builder = CryptoApproveAllowanceTransactionBody.newBuilder();

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
        bodyBuilder.setCryptoApproveAllowance(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setCryptoApproveAllowance(build());
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
