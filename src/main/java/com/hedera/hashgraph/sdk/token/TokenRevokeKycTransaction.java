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
public final class TokenRevokeKycTransaction extends SingleTransactionBuilder<TokenRevokeKycTransaction> {
    private final TokenRevokeKycTransactionBody.Builder builder = bodyBuilder.getTokenRevokeKycBuilder();

    public TokenRevokeKycTransaction() {
        super();
    }

    /**
     * The token for which this account will get his KYC revoked. If token does not exist, transaction results in
     * INVALID_TOKEN_ID
     *
     * @param token
     * @return TokenRevokeKycTransaction
     */
    public TokenRevokeKycTransaction setTokenId(TokenId token) {
        builder.setToken(token.toProto());
        return this;
    }

    /**
     * The account to be KYC Revoked
     *
     * @param accountId
     * @return TokenRevokeKycTransaction
     */
    public TokenRevokeKycTransaction setAccountId(AccountId accountId) {
        builder.setAccount(accountId.toProto());
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return TokenServiceGrpc.getRevokeKycFromTokenAccountMethod();
    }

    @Override
    protected void doValidate() {
    }
}
