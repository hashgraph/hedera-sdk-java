package com.hedera.hashgraph.sdk.token;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.proto.TokenDeleteTransactionBody;
import com.hedera.hashgraph.proto.TokenServiceGrpc;
import com.hedera.hashgraph.proto.KeyList;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.*;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.PublicKey;

import java.time.Duration;
import java.time.Instant;

import io.grpc.MethodDescriptor;

/**
 * Marks a token as deleted, though it will remain in the ledger.
 * The operation must be signed by the specified Admin Key of the Token. If admin key is not set, Transaction will
 * result in TOKEN_IS_IMMUTABlE. Once deleted update, mint, burn, wipe, freeze, unfreeze, grant kyc, revoke kyc and
 * token transfer transactions will resolve to TOKEN_WAS_DELETED.
 */
public final class TokenDeleteTransaction extends SingleTransactionBuilder<TokenDeleteTransaction> {
    private final TokenDeleteTransactionBody.Builder builder = bodyBuilder.getTokenDeletionBuilder();

    public TokenDeleteTransaction() {
        super();
    }

    /**
     * The token to be deleted. If invalid token is specified, transaction will result in INVALID_TOKEN_ID
     *
     * @param token
     * @return TokenDeleteTransaction
     */
    public TokenDeleteTransaction setTokenId(TokenId token) {
        builder.setToken(token.toProto());
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return TokenServiceGrpc.getDeleteTokenMethod();
    }

    @Override
    protected void doValidate() {
    }
}
