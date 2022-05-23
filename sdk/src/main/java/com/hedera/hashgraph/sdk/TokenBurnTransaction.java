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
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenBurnTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import com.hedera.hashgraph.sdk.proto.Transaction;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

/**
 * Burns fungible and non-fungible tokens owned by the Treasury Account.
 * If no Supply Key is defined, the transaction will resolve to
 * TOKEN_HAS_NO_SUPPLY_KEY.
 *
 * See <a href="https://docs.hedera.com/guides/docs/sdks/tokens/burn-a-token”>Hedera Documentation</a>
 */
public class TokenBurnTransaction extends com.hedera.hashgraph.sdk.Transaction<TokenBurnTransaction> {
    /**
     * The ID of the token to burn supply
     */
    @Nullable
    private TokenId tokenId = null;
    /**
     * The ID of the token to burn supply
     */
    private long amount = 0;
    /**
     * Applicable to tokens of type NON_FUNGIBLE_UNIQUE.The  list of NFT serial IDs to burn.
     */
    private List<Long> serials = new ArrayList<>();

    /**
     * Constructor.
     */
    public TokenBurnTransaction() {
    }

    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction)
     *            records
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    TokenBurnTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * Constructor.
     *
     * @param txBody protobuf TransactionBody
     */
    TokenBurnTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    /**
     * @return                          the token id
     */
    @Nullable
    public TokenId getTokenId() {
        return tokenId;
    }

    /**
     * Assign the token id.
     *
     * @param tokenId                   the token id
     * @return {@code this}
     */
    public TokenBurnTransaction setTokenId(TokenId tokenId) {
        Objects.requireNonNull(tokenId);
        requireNotFrozen();
        this.tokenId = tokenId;
        return this;
    }

    /**
     * @return                          the amount of tokens to burn
     */
    public long getAmount() {
        return amount;
    }

    /**
     * Assign the amount of tokens to burn.
     *
     * The amount provided must be in the lowest denomination possible.
     *
     * Example: Token A has 2 decimals. In order to burn 100 tokens, one must
     * provide an amount of 10000. In order to burn 100.55 tokens, one must
     * provide an amount of 10055.
     *
     * See <a href="https://docs.hedera.com/guides/docs/sdks/tokens/burn-a-token”>Hedera Documentation</a>
     *
     * @param amount                    the amount of tokens to burn
     * @return {@code this}
     */
    public TokenBurnTransaction setAmount(@Nonnegative long amount) {
        requireNotFrozen();
        this.amount = amount;
        return this;
    }

    /**
     * @return                          list of token serials
     */
    public List<Long> getSerials() {
        return new ArrayList<>(serials);
    }

    /**
     * Assign the list of token serials.
     *
     * @param serials                   list of token serials
     * @return {@code this}
     */
    public TokenBurnTransaction setSerials(List<Long> serials) {
        requireNotFrozen();
        Objects.requireNonNull(serials);
        this.serials = new ArrayList<>(serials);
        return this;
    }

    /**
     * Add a serial number to the list of serials.
     *
     * @param serial                    the serial number to add
     * @return {@code this}
     */
    public TokenBurnTransaction addSerial(@Nonnegative long serial) {
        requireNotFrozen();
        serials.add(serial);
        return this;
    }

    /**
     * Build the transaction body.
     *
     * @return {@code {@link com.hedera.hashgraph.sdk.proto.TokenBurnTransactionBody}}
     */
    TokenBurnTransactionBody.Builder build() {
        var builder = TokenBurnTransactionBody.newBuilder();
        if (tokenId != null) {
            builder.setToken(tokenId.toProtobuf());
        }
        builder.setAmount(amount);

        for (var serial : serials) {
            builder.addSerialNumbers(serial);
        }

        return builder;
    }

    /**
     * Initialize from the transaction body.
     */
    void initFromTransactionBody() {
        var body = sourceTransactionBody.getTokenBurn();
        if (body.hasToken()) {
            tokenId = TokenId.fromProtobuf(body.getToken());
        }
        amount = body.getAmount();
        serials = body.getSerialNumbersList();
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (tokenId != null) {
            tokenId.validateChecksum(client);
        }
    }

    @Override
    MethodDescriptor<Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getBurnTokenMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenBurn(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenBurn(build());
    }
}
