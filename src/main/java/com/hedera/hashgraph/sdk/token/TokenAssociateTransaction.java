package com.hedera.hashgraph.sdk.token;

import com.hedera.hashgraph.proto.*;
import com.hedera.hashgraph.sdk.SingleTransactionBuilder;
import com.hedera.hashgraph.sdk.account.AccountId;

import io.grpc.MethodDescriptor;

/**
 * Associates the provided account with the provided tokens. Must be signed by the provided Account's key.
 * If the provided account is not found, the transaction will resolve to INVALID_ACCOUNT_ID.
 * If the provided account has been deleted, the transaction will resolve to ACCOUNT_DELETED.
 * If any of the provided tokens is not found, the transaction will resolve to INVALID_TOKEN_REF.
 * If any of the provided tokens has been deleted, the transaction will resolve to TOKEN_WAS_DELETED.
 * If an association between the provided account and any of the tokens already exists, the transaction will resolve to
 * TOKEN_ALREADY_ASSOCIATED_TO_ACCOUNT.
 * If the provided account's associations count exceed the constraint of maximum token associations per account, the
 * transaction will resolve to TOKENS_PER_ACCOUNT_LIMIT_EXCEEDED.
 * On success, associations between the provided account and tokens are made and the account is ready to interact with
 * the tokens.
 */
public final class TokenAssociateTransaction extends SingleTransactionBuilder<TokenAssociateTransaction> {
    private final TokenAssociateTransactionBody.Builder builder = bodyBuilder.getTokenAssociateBuilder();

    public TokenAssociateTransaction() {
        super();
    }

    /**
     * The account to be associated with the provided tokens
     *
     * @param accountId
     * @return TokenAssociateTransaction
     */
    public TokenAssociateTransaction setAccountId(AccountId accountId) {
        builder.setAccount(accountId.toProto());
        return this;
    }

    /**
     * The tokens to be associated with the provided account
     *
     * @param tokenId
     * @return TokenAssociateTransaction
     */
    public TokenAssociateTransaction addTokenId(TokenId tokenId) {
        builder.addTokens(tokenId.toProto());
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return TokenServiceGrpc.getAssociateTokensMethod();
    }

    @Override
    protected void doValidate() {
    }
}
