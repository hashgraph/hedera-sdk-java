package com.hedera.hashgraph.sdk.token;

import com.hedera.hashgraph.proto.*;
import com.hedera.hashgraph.sdk.SingleTransactionBuilder;

import com.hedera.hashgraph.sdk.account.AccountId;
import io.grpc.MethodDescriptor;

/**
 * Freezes transfers of the specified token for the account. Must be signed by the Token's freezeKey.
 * If the provided account is not found, the transaction will resolve to INVALID_ACCOUNT_ID.
 * If the provided account has been deleted, the transaction will resolve to ACCOUNT_DELETED.
 * If the provided token is not found, the transaction will resolve to INVALID_TOKEN_ID.
 * If the provided token has been deleted, the transaction will resolve to TOKEN_WAS_DELETED.
 * If an Association between the provided token and account is not found, the transaction will resolve to
 * TOKEN_NOT_ASSOCIATED_TO_ACCOUNT.
 * If no Freeze Key is defined, the transaction will resolve to TOKEN_HAS_NO_FREEZE_KEY.
 * Once executed the Account is marked as Frozen and will not be able to receive or send tokens unless unfrozen.
 * The operation is idempotent.
 */
public final class TokenFreezeAccountTransaction extends SingleTransactionBuilder<TokenFreezeAccountTransaction> {
    private final TokenFreezeAccountTransactionBody.Builder builder = bodyBuilder.getTokenFreezeBuilder();

    public TokenFreezeAccountTransaction() {
        super();
    }

    /**
     * The token for which this account will be frozen. If token does not exist, transaction results in INVALID_TOKEN_ID
     *
     * @param token
     * @return TokenFreezeAccountTransaction
     */
    public TokenFreezeAccountTransaction setTokenId(TokenId token) {
        builder.setToken(token.toProto());
        return this;
    }

    /**
     * The account to be frozen
     *
     * @param accountId
     * @return TokenFreezeAccountTransaction
     */
    public TokenFreezeAccountTransaction setAccount(AccountId accountId) {
        builder.setAccount(accountId.toProto());
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return TokenServiceGrpc.getFreezeTokenAccountMethod();
    }

    @Override
    protected void doValidate() {
    }
}
