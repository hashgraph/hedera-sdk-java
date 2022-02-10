package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.CryptoApproveAllowanceTransactionBody;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AccountAllowanceApproveTransaction extends Transaction<AccountAllowanceApproveTransaction> {
    private final List<HbarAllowance> hbarAllowances = new ArrayList<>();
    private final List<TokenAllowance> tokenAllowances =  new ArrayList<>();
    private final List<TokenNftAllowance> nftAllowances = new ArrayList<>();
    private final Map<AccountId, Map<TokenId, Integer>> nftMap = new HashMap<>();

    public AccountAllowanceApproveTransaction() {
    }

    AccountAllowanceApproveTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
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
                    AccountId.fromProtobuf(allowanceProto.getSpender()),
                    TokenId.fromProtobuf(allowanceProto.getTokenId())
                ).addAll(allowanceProto.getSerialNumbersList());
            }
        }
    }

    public AccountAllowanceApproveTransaction addHbarAllowance(AccountId spenderAccountId, Hbar amount) {
        hbarAllowances.add(new HbarAllowance(
            null,
            Objects.requireNonNull(spenderAccountId),
            Objects.requireNonNull(amount)
        ));
        return this;
    }

    public List<HbarAllowance> getHbarAllowances() {
        return new ArrayList<>(hbarAllowances);
    }

    public AccountAllowanceApproveTransaction addTokenAllowance(TokenId tokenId, AccountId spenderAccountId, long amount) {
        tokenAllowances.add(new TokenAllowance(
            Objects.requireNonNull(tokenId),
            null,
            Objects.requireNonNull(spenderAccountId),
            amount
        ));
        return this;
    }

    public List<TokenAllowance> getTokenAllowances() {
        return new ArrayList<>(tokenAllowances);
    }

    private List<Long> getNftSerials(AccountId spenderAccountId, TokenId tokenId) {
        if (nftMap.containsKey(spenderAccountId)) {
            var innerMap = nftMap.get(spenderAccountId);
            if (innerMap.containsKey(tokenId)) {
                return Objects.requireNonNull(nftAllowances.get(innerMap.get(tokenId)).serialNumbers);
            } else {
                return newNftSerials(spenderAccountId, tokenId, innerMap);
            }
        } else {
            Map<TokenId, Integer> innerMap = new HashMap<>();
            nftMap.put(spenderAccountId, innerMap);
            return newNftSerials(spenderAccountId, tokenId, innerMap);
        }
    }

    private List<Long> newNftSerials(AccountId spenderAccountId, TokenId tokenId, Map<TokenId, Integer> innerMap) {
        innerMap.put(tokenId, nftAllowances.size());
        TokenNftAllowance newAllowance = new TokenNftAllowance(tokenId, null,spenderAccountId, new ArrayList<>());
        nftAllowances.add(newAllowance);
        return newAllowance.serialNumbers;
    }

    public AccountAllowanceApproveTransaction addTokenNftAllowance(NftId nftId, AccountId spenderAccountId) {
        getNftSerials(spenderAccountId, nftId.tokenId).add(nftId.serial);
        return this;
    }

    public AccountAllowanceApproveTransaction addAllTokenNftAllowance(TokenId tokenId, AccountId spenderAccountId) {
        nftAllowances.add(new TokenNftAllowance(tokenId, null, spenderAccountId, null));
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
        return CryptoServiceGrpc.getApproveAllowancesMethod();
    }

    CryptoApproveAllowanceTransactionBody.Builder build() {
        var builder = CryptoApproveAllowanceTransactionBody.newBuilder();
        for (var allowance : hbarAllowances) {
            builder.addCryptoAllowances(allowance.toProtobuf());
        }
        for (var allowance : tokenAllowances) {
            builder.addTokenAllowances(allowance.toProtobuf());
        }
        for (var allowance : nftAllowances) {
            builder.addNftAllowances(allowance.toProtobuf());
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
