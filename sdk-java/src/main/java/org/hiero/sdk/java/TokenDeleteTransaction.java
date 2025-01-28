// SPDX-License-Identifier: Apache-2.0
package org.hiero.sdk.java;

import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.MethodDescriptor;
import java.util.LinkedHashMap;
import java.util.Objects;
import javax.annotation.Nullable;
import org.hiero.sdk.java.proto.SchedulableTransactionBody;
import org.hiero.sdk.java.proto.TokenDeleteTransactionBody;
import org.hiero.sdk.java.proto.TokenServiceGrpc;
import org.hiero.sdk.java.proto.Transaction;
import org.hiero.sdk.java.proto.TransactionBody;
import org.hiero.sdk.java.proto.TransactionResponse;

/**
 * Deleting a token marks a token as deleted, though it will remain in the
 * ledger. The operation must be signed by the specified Admin Key of the
 * Token. If the Admin Key is not set, Transaction will result in
 * TOKEN_IS_IMMUTABlE. Once deleted update, mint, burn, wipe, freeze,
 * unfreeze, grant kyc, revoke kyc and token transfer transactions will
 * resolve to TOKEN_WAS_DELETED.
 *
 * See <a href="https://docs.hedera.com/guides/docs/sdks/tokens/delete-a-token">Hedera Documentation</a>
 */
public class TokenDeleteTransaction extends org.hiero.sdk.java.Transaction<TokenDeleteTransaction> {
    @Nullable
    private TokenId tokenId = null;

    /**
     * Constructor.
     */
    public TokenDeleteTransaction() {}

    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction)
     *            records
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    TokenDeleteTransaction(
            LinkedHashMap<TransactionId, LinkedHashMap<AccountId, org.hiero.sdk.java.proto.Transaction>> txs)
            throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * Constructor.
     *
     * @param txBody protobuf TransactionBody
     */
    TokenDeleteTransaction(org.hiero.sdk.java.proto.TransactionBody txBody) {
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
    public TokenDeleteTransaction setTokenId(TokenId tokenId) {
        Objects.requireNonNull(tokenId);
        requireNotFrozen();
        this.tokenId = tokenId;
        return this;
    }

    /**
     * Initialize from the transaction body.
     */
    void initFromTransactionBody() {
        var body = sourceTransactionBody.getTokenDeletion();
        if (body.hasToken()) {
            tokenId = TokenId.fromProtobuf(body.getToken());
        }
    }

    /**
     * Build the transaction body.
     *
     * @return {@link
     *         org.hiero.sdk.java.proto.TokenDeleteTransactionBody}
     */
    TokenDeleteTransactionBody.Builder build() {
        var builder = TokenDeleteTransactionBody.newBuilder();
        if (tokenId != null) {
            builder.setToken(tokenId.toProtobuf());
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
        return TokenServiceGrpc.getDeleteTokenMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenDeletion(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenDeletion(build());
    }
}
