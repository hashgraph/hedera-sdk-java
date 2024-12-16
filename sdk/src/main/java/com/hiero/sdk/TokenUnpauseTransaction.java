// SPDX-License-Identifier: Apache-2.0
package com.hiero.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hiero.sdk.proto.SchedulableTransactionBody;
import com.hiero.sdk.proto.TokenServiceGrpc;
import com.hiero.sdk.proto.TokenUnpauseTransactionBody;
import com.hiero.sdk.proto.TransactionBody;
import com.hiero.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import java.util.LinkedHashMap;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * A token unpause transaction is a transaction that unpauses the token
 * that was previously disabled from participating in transactions. The
 * token's pause key is required to sign the transaction. Once the unpause
 * transaction is submitted the token pause status is updated to unpause.
 *
 * See <a href="https://docs.hedera.com/guides/docs/sdks/tokens/unpause-a-token">Hedera Documentation</a>
 */
public class TokenUnpauseTransaction extends Transaction<TokenUnpauseTransaction> {
    @Nullable
    private TokenId tokenId = null;

    /**
     * Constructor
     */
    public TokenUnpauseTransaction() {}

    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction)
     *            records
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    TokenUnpauseTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hiero.sdk.proto.Transaction>> txs)
            throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * Constructor.
     *
     * @param txBody protobuf TransactionBody
     */
    TokenUnpauseTransaction(com.hiero.sdk.proto.TransactionBody txBody) {
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
    public TokenUnpauseTransaction setTokenId(TokenId tokenId) {
        Objects.requireNonNull(tokenId);
        requireNotFrozen();
        this.tokenId = tokenId;
        return this;
    }

    /**
     * Initialize from the transaction body.
     */
    void initFromTransactionBody() {
        var body = sourceTransactionBody.getTokenUnpause();
        if (body.hasToken()) {
            tokenId = TokenId.fromProtobuf(body.getToken());
        }
    }

    /**
     * Build the transaction body.
     *
     * @return {@link
     *         com.hiero.sdk.proto.TokenUnpauseTransactionBody}
     */
    TokenUnpauseTransactionBody.Builder build() {
        var builder = TokenUnpauseTransactionBody.newBuilder();
        if (tokenId != null) {
            builder.setToken(tokenId.toProtobuf());
        }

        return builder;
    }

    @Override
    MethodDescriptor<com.hiero.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getUnpauseTokenMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenUnpause(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenUnpause(build());
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (tokenId != null) {
            tokenId.validateChecksum(client);
        }
    }
}
