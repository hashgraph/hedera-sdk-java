package com.hedera.hashgraph.sdk.token;

import com.hedera.hashgraph.proto.*;
import com.hedera.hashgraph.sdk.SingleTransactionBuilder;
import com.hedera.hashgraph.sdk.account.AccountId;

import io.grpc.MethodDescriptor;

/**
 * Dissociates the provided account with the provided tokens. Must be signed by the provided Account's key.
 * If the provided account is not found, the transaction will resolve to INVALID_ACCOUNT_ID.
 * If the provided account has been deleted, the transaction will resolve to ACCOUNT_DELETED.
 * If any of the provided tokens is not found, the transaction will resolve to INVALID_TOKEN_REF.
 * If any of the provided tokens has been deleted, the transaction will resolve to TOKEN_WAS_DELETED.
 * If an association between the provided account and any of the tokens does not exist, the transaction will resolve to
 * TOKEN_NOT_ASSOCIATED_TO_ACCOUNT.
 * If the provided account has a nonzero balance with any of the provided tokens, the transaction will resolve to
 * TRANSACTION_REQUIRES_ZERO_TOKEN_BALANCES.
 * On success, associations between the provided account and tokens are removed.
 */
public final class TokenDissociateTransaction extends SingleTransactionBuilder<TokenDissociateTransaction> {
    private final TokenDissociateTransactionBody.Builder builder = bodyBuilder.getTokenDissociateBuilder();

    public TokenDissociateTransaction() {
        super();
    }

    /**
     * The account to be dissociated with the provided tokens
     *
     * @param accountId
     * @return TokenDissociateTransaction
     */
    public TokenDissociateTransaction setAccountId(AccountId accountId) {
        builder.setAccount(accountId.toProto());
        return this;
    }

    /**
     * The tokens to be dissociated with the provided account
     *
     * @param tokenId
     * @return TokenDissociateTransaction
     */
    public TokenDissociateTransaction addTokenId(TokenId tokenId) {
        builder.addTokens(tokenId.toProto());
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return TokenServiceGrpc.getDissociateTokensMethod();
    }

    @Override
    protected void doValidate() {
    }
}
