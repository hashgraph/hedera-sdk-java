// SPDX-License-Identifier: Apache-2.0
package com.hiero.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hiero.sdk.proto.SchedulableTransactionBody;
import com.hiero.sdk.proto.TokenPauseTransactionBody;
import com.hiero.sdk.proto.TokenServiceGrpc;
import com.hiero.sdk.proto.TransactionBody;
import com.hiero.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import java.util.LinkedHashMap;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * A token pause transaction prevents the token from being involved in any
 * kind of operation. The token's pause key is required to sign the
 * transaction. This is a key that is specified during the creation of a
 * token. If a token has no pause key, you will not be able to pause the
 * token.  If the pause key was not set during the creation of a token, you
 * will not be able to update the token to add this key.
 *
 * See <a href="https://docs.hedera.com/guides/docs/sdks/tokens/pause-a-token">Hedera Documentation</a>
 */
public class TokenPauseTransaction extends Transaction<TokenPauseTransaction> {
    @Nullable
    private TokenId tokenId = null;

    /**
     * Constructor.
     */
    public TokenPauseTransaction() {}

    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction)
     *            records
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    TokenPauseTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hiero.sdk.proto.Transaction>> txs)
            throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * Constructor.
     *
     * @param txBody protobuf TransactionBody
     */
    TokenPauseTransaction(com.hiero.sdk.proto.TransactionBody txBody) {
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
    public TokenPauseTransaction setTokenId(TokenId tokenId) {
        Objects.requireNonNull(tokenId);
        requireNotFrozen();
        this.tokenId = tokenId;
        return this;
    }

    void initFromTransactionBody() {
        var body = sourceTransactionBody.getTokenPause();
        if (body.hasToken()) {
            tokenId = TokenId.fromProtobuf(body.getToken());
        }
    }

    /**
     * Build the transaction body.
     *
     * @return {@link
     *         com.hiero.sdk.proto.TokenPauseTransactionBody}
     */
    TokenPauseTransactionBody.Builder build() {
        var builder = TokenPauseTransactionBody.newBuilder();
        if (tokenId != null) {
            builder.setToken(tokenId.toProtobuf());
        }

        return builder;
    }

    @Override
    MethodDescriptor<com.hiero.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getPauseTokenMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenPause(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenPause(build());
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (tokenId != null) {
            tokenId.validateChecksum(client);
        }
    }
}
