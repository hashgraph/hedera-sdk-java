package com.hedera.hashgraph.sdk.token;

import com.hedera.hashgraph.proto.*;
import com.hedera.hashgraph.sdk.SingleTransactionBuilder;

import com.hedera.hashgraph.sdk.account.AccountId;
import io.grpc.MethodDescriptor;

/**
 * Revokes KYC to the account for the given token. Must be signed by the Token's kycKey.
 * If the provided account is not found, the transaction will resolve to INVALID_ACCOUNT_ID.
 * If the provided account has been deleted, the transaction will resolve to ACCOUNT_DELETED.
 * If the provided token is not found, the transaction will resolve to INVALID_TOKEN_ID.
 * If the provided token has been deleted, the transaction will resolve to TOKEN_WAS_DELETED.
 * If an Association between the provided token and account is not found, the transaction will resolve to
 * TOKEN_NOT_ASSOCIATED_TO_ACCOUNT.
 * If no KYC Key is defined, the transaction will resolve to TOKEN_HAS_NO_KYC_KEY.
 * Once executed the Account is marked as KYC Revoked
 */
public final class TokenWipeAccountTransaction extends SingleTransactionBuilder<TokenWipeAccountTransaction> {
    private final TokenWipeAccountTransactionBody.Builder builder = bodyBuilder.getTokenWipeBuilder();

    public TokenWipeAccountTransaction() {
        super();
    }

    /**
     * The token for which the account will be wiped. If token does not exist, transaction results in INVALID_TOKEN_ID
     *
     * @param token
     * @return TokenWipeAccountTransaction
     */
    public TokenWipeAccountTransaction setTokenId(TokenId token) {
        builder.setToken(token.toProto());
        return this;
    }

    /**
     * The account to be wiped
     *
     * @param accountId
     * @return TokenWipeAccountTransaction
     */
    public TokenWipeAccountTransaction setAccount(AccountId accountId) {
        builder.setAccount(accountId.toProto());
        return this;
    }

    /**
     * The amount of tokens to wipe from the specified account. Amount must be a positive non-zero number in the lowest
     * denomination possible, not bigger than the token balance of the account (0; balance]
     *
     * @param amount
     * @return TokenWipeAccountTransaction
     */
    public TokenWipeAccountTransaction setAmount(long amount) {
        builder.setAmount(amount);
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return TokenServiceGrpc.getWipeTokenAccountMethod();
    }

    @Override
    protected void doValidate() {
    }
}
