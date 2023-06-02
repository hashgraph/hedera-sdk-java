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

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenMintTransactionBody;
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
 * Minting fungible token allows you to increase the total supply of the
 * token. Minting a non-fungible token creates an NFT with its unique
 * metadata for the class of NFTs defined by the token ID. The Supply
 * Key must sign the transaction.
 *
 * See <a href="https://docs.hedera.com/guides/docs/sdks/tokens/mint-a-token">Hedera Documentation</a>
 */
public class TokenMintTransaction extends com.hedera.hashgraph.sdk.Transaction<TokenMintTransaction> {
    @Nullable
    private TokenId tokenId;
    /**
     * The metadata field is specific to NFTs.
     *
     * Once an NFT is minted, the metadata cannot be changed and is immutable.
     *
     * You can use the metadata field to add a URI that contains additional
     * information about the token.
     *
     * The metadata field has a 100 byte limit.
     */
    private List<byte[]> metadataList = new ArrayList<>();
    /**
     * amount provided must be in the lowest denomination possible.
     *
     * Example: Token A has 2 decimals.
     *
     * In order to mint 100 tokens, one must provide an amount of 10000.
     *
     * In order to mint 100.55 tokens, one must provide an amount of 10055.
     */
    private long amount;

    /**
     * Constructor.
     */
    public TokenMintTransaction() {
    }

    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction)
     *            records
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    TokenMintTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * Constructor.
     *
     * @param txBody protobuf TransactionBody
     */
    TokenMintTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    /**
     * Extract the token id.
     *
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
    public TokenMintTransaction setTokenId(TokenId tokenId) {
        Objects.requireNonNull(tokenId);
        requireNotFrozen();
        this.tokenId = tokenId;
        return this;
    }

    /**
     * Extract the amount.
     *
     * @return                          the amount to mint
     */
    public long getAmount() {
        return amount;
    }

    /**
     * Assign the amount to mint.
     *
     * @param amount                    the amount to mint
     * @return {@code this}
     */
    public TokenMintTransaction setAmount(@Nonnegative long amount) {
        requireNotFrozen();
        this.amount = amount;
        return this;
    }

    /**
     * Add to the metadata list.
     *
     * @param metadata                  the metadata 100 bytes max
     * @return {@code this}
     */
    public TokenMintTransaction addMetadata(byte[] metadata) {
        requireNotFrozen();
        Objects.requireNonNull(metadata);
        metadataList.add(metadata);
        return this;
    }

    /**
     * Extract the list of metadata byte array records.
     *
     * @return                          the metadata list
     */
    public List<byte[]> getMetadata() {
        return new ArrayList<>(metadataList);
    }

    /**
     * Assign the metadata list.
     *
     * @param metadataList              the metadata list
     * @return {@code this}
     */
    public TokenMintTransaction setMetadata(List<byte[]> metadataList) {
        requireNotFrozen();
        this.metadataList = new ArrayList<>(metadataList);
        return this;
    }

    /**
     * Initialize from the transaction body.
     */
    void initFromTransactionBody() {
        var body = sourceTransactionBody.getTokenMint();
        if (body.hasToken()) {
            tokenId = TokenId.fromProtobuf(body.getToken());
        }
        amount = body.getAmount();
        for (var metadata : body.getMetadataList()) {
            metadataList.add(metadata.toByteArray());
        }
    }

    /**
     * Build the transaction body.
     *
     * @return {@link
     *         com.hedera.hashgraph.sdk.proto.TokenMintTransactionBody}
     */
    TokenMintTransactionBody.Builder build() {
        var builder = TokenMintTransactionBody.newBuilder();
        if (tokenId != null) {
            builder.setToken(tokenId.toProtobuf());
        }
        builder.setAmount(amount);
        for (var metadata : metadataList) {
            builder.addMetadata(ByteString.copyFrom(metadata));
        }

        return builder;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (tokenId != null) {
            tokenId.validateChecksum(client);
        }
    }

    @Override
    MethodDescriptor<Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getMintTokenMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenMint(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenMint(build());
    }
}
