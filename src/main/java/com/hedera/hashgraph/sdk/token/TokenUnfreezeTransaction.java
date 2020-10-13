package com.hedera.hashgraph.sdk.token;

import com.hedera.hashgraph.proto.*;
import com.hedera.hashgraph.sdk.SingleTransactionBuilder;

import com.hedera.hashgraph.sdk.account.AccountId;
import io.grpc.MethodDescriptor;

/* Unfreezes transfers of the specified token for the account. Must be signed by the Token's freezeKey.
 If the provided account is not found, the transaction will resolve to INVALID_ACCOUNT_ID.
 If the provided account has been deleted, the transaction will resolve to ACCOUNT_DELETED.
 If the provided token is not found, the transaction will resolve to INVALID_TOKEN_ID.
 If the provided token has been deleted, the transaction will resolve to TOKEN_WAS_DELETED.
 If an Association between the provided token and account is not found, the transaction will resolve to TOKEN_NOT_ASSOCIATED_TO_ACCOUNT.
 If no Freeze Key is defined, the transaction will resolve to TOKEN_HAS_NO_FREEZE_KEY.
 Once executed the Account is marked as Unfrozen and will be able to receive or send tokens. The operation is idempotent.
 */
public final class TokenUnfreezeTransaction extends SingleTransactionBuilder<TokenUnfreezeTransaction> {
    private final TokenUnfreezeAccountTransactionBody.Builder builder = bodyBuilder.getTokenUnfreezeBuilder();

    public TokenUnfreezeTransaction() {
        super();
    }

    /**
     * The token for which this account will be frozen. If token does not exist, transaction results in INVALID_TOKEN_ID
     *
     * @param token
     * @return TokenUnfreezeTransaction
     */
    public TokenUnfreezeTransaction setTokenId(TokenId token) {
        builder.setToken(token.toProto());
        return this;
    }

    /**
     * The account to be frozen
     *
     * @param accountId
     * @return TokenUnfreezeTransaction
     */
    public TokenUnfreezeTransaction setAccountId(AccountId accountId) {
        builder.setAccount(accountId.toProto());
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return TokenServiceGrpc.getUnfreezeTokenAccountMethod();
    }

    @Override
    protected void doValidate() {
    }
}
