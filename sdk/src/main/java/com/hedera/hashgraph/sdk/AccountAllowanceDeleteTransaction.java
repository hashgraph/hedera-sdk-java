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

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.CryptoDeleteAllowanceTransactionBody;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.Transaction;
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

public class AccountAllowanceDeleteTransaction extends com.hedera.hashgraph.sdk.Transaction<AccountAllowanceDeleteTransaction> {
    private final List<HbarAllowance> hbarAllowances = new ArrayList<>();
    private final List<TokenAllowance> tokenAllowances = new ArrayList<>();
    private final List<TokenNftAllowance> nftAllowances = new ArrayList<>();
    // <ownerId, <tokenId, index>>
    private final Map<AccountId, Map<TokenId, Integer>> nftMap = new HashMap<>();

    public AccountAllowanceDeleteTransaction() {
    }

    AccountAllowanceDeleteTransaction(
        LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs
    ) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    AccountAllowanceDeleteTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    private void initFromTransactionBody() {
        var body = sourceTransactionBody.getCryptoDeleteAllowance();
        for (var allowanceProto : body.getCryptoAllowancesList()) {
            hbarAllowances.add(HbarAllowance.fromProtobuf(allowanceProto));
        }
        for (var allowanceProto : body.getTokenAllowancesList()) {
            tokenAllowances.add(TokenAllowance.fromProtobuf(allowanceProto));
        }
        for (var allowanceProto : body.getNftAllowancesList()) {
            getNftSerials(
                AccountId.fromProtobuf(allowanceProto.getOwner()),
                TokenId.fromProtobuf(allowanceProto.getTokenId())
            ).addAll(allowanceProto.getSerialNumbersList());
        }
    }

    public AccountAllowanceDeleteTransaction deleteAllHbarAllowances(AccountId ownerAccountId) {
        requireNotFrozen();
        hbarAllowances.add(new HbarAllowance(Objects.requireNonNull(ownerAccountId), null, null));
        return this;
    }

    public List<HbarAllowance> getHbarAllowanceDeletions() {
        return new ArrayList<>(hbarAllowances);
    }

    public AccountAllowanceDeleteTransaction deleteAllTokenAllowances(TokenId tokenId, AccountId ownerAccountId) {
        requireNotFrozen();
        tokenAllowances.add(new TokenAllowance(
            Objects.requireNonNull(tokenId),
            Objects.requireNonNull(ownerAccountId),
            null,
            0
        ));
        return this;
    }

    public List<TokenAllowance> getTokenAllowanceDeletions() {
        return new ArrayList<>(tokenAllowances);
    }

    public AccountAllowanceDeleteTransaction deleteAllTokenNftAllowances(NftId nftId, AccountId ownerAccountId) {
        requireNotFrozen();
        Objects.requireNonNull(nftId);
        getNftSerials(Objects.requireNonNull(ownerAccountId), nftId.tokenId).add(nftId.serial);
        return this;
    }

    public List<TokenNftAllowance> getTokenNftAllowanceDeletions() {
        List<TokenNftAllowance> retval = new ArrayList<>(nftAllowances.size());
        for (var allowance : nftAllowances) {
            retval.add(TokenNftAllowance.copyFrom(allowance));
        }
        return retval;
    }

    private List<Long> getNftSerials(@Nullable AccountId ownerAccountId, TokenId tokenId) {
        var key = ownerAccountId;
        if (nftMap.containsKey(key)) {
            var innerMap = nftMap.get(key);
            if (innerMap.containsKey(tokenId)) {
                return Objects.requireNonNull(nftAllowances.get(innerMap.get(tokenId)).serialNumbers);
            } else {
                return newNftSerials(ownerAccountId, tokenId, innerMap);
            }
        } else {
            Map<TokenId, Integer> innerMap = new HashMap<>();
            nftMap.put(key, innerMap);
            return newNftSerials(ownerAccountId, tokenId, innerMap);
        }
    }

    private List<Long> newNftSerials(
        @Nullable AccountId ownerAccountId,
        TokenId tokenId,
        Map<TokenId, Integer> innerMap
    ) {
        innerMap.put(tokenId, nftAllowances.size());
        TokenNftAllowance newAllowance = new TokenNftAllowance(
            tokenId,
            ownerAccountId,
            null,
            new ArrayList<>(),
            null
        );
        nftAllowances.add(newAllowance);
        return newAllowance.serialNumbers;
    }

    @Override
    MethodDescriptor<Transaction, TransactionResponse> getMethodDescriptor() {
        return CryptoServiceGrpc.getDeleteAllowancesMethod();
    }

    CryptoDeleteAllowanceTransactionBody.Builder build() {
        var builder = CryptoDeleteAllowanceTransactionBody.newBuilder();
        for (var allowance : hbarAllowances) {
            builder.addCryptoAllowances(allowance.toWipeProtobuf());
        }
        for (var allowance : tokenAllowances) {
            builder.addTokenAllowances(allowance.toWipeProtobuf());
        }
        for (var allowance : nftAllowances) {
            builder.addNftAllowances(allowance.toRemoveProtobuf());
        }
        return builder;
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setCryptoDeleteAllowance(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setCryptoDeleteAllowance(build());
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        for (var allowance : hbarAllowances) {
            allowance.validateChecksums(client);
        }
        for (var allowance : tokenAllowances) {
            allowance.validateChecksums(client);
        }
        for (var allowance : nftAllowances) {
            allowance.validateChecksums(client);
        }
    }
}
