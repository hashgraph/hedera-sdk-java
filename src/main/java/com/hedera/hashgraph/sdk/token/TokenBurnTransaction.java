package com.hedera.hashgraph.sdk.token;

import com.hedera.hashgraph.proto.*;
import com.hedera.hashgraph.sdk.SingleTransactionBuilder;

import io.grpc.MethodDescriptor;

/**
 * Burns tokens from the Token's treasury Account. If no Supply Key is defined, the transaction will resolve to
 * TOKEN_HAS_NO_SUPPLY_KEY. The operation decreases the Total Supply of the Token. Total supply cannot go below zero.
 * The amount provided must be in the lowest denomination possible. Example:
 * Token A has 2 decimals. In order to burn 100 tokens, one must provide amount of 10000. In order to burn 100.55
 * tokens, one must provide amount of 10055.
 */
public final class TokenBurnTransaction extends SingleTransactionBuilder<TokenBurnTransaction> {
    private final TokenBurnTransactionBody.Builder builder = bodyBuilder.getTokenBurnBuilder();

    public TokenBurnTransaction() {
        super();
    }

    /**
     * The token for which to burn tokens. If token does not exist, transaction results in INVALID_TOKEN_ID
     *
     * @param token
     * @return TokenBurnTransaction
     */
    public TokenBurnTransaction setTokenId(TokenId token) {
        builder.setToken(token.toProto());
        return this;
    }

    /**
     * The amount to burn from the Treasury Account. Amount must be a positive non-zero number, not bigger than the
     * token balance of the treasury account (0; balance], represented in the lowest denomination.
     *
     * @param amount
     * @return TokenBurnTransaction
     */
    public TokenBurnTransaction setAmount(long amount) {
        builder.setAmount(amount);
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return TokenServiceGrpc.getBurnTokenMethod();
    }

    @Override
    protected void doValidate() {
    }
}
