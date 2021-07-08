package com.hedera.hashgraph.sdk.token;

import com.hedera.hashgraph.proto.*;
import com.hedera.hashgraph.sdk.SingleTransactionBuilder;

import com.hedera.hashgraph.sdk.account.AccountId;
import io.grpc.MethodDescriptor;

import java.util.List;

/**
 * Wipes the provided amount of tokens from the specified Account. Must be signed by the Token's Wipe key.
 * If the provided account is not found, the transaction will resolve to INVALID_ACCOUNT_ID.
 * If the provided account has been deleted, the transaction will resolve to ACCOUNT_DELETED.
 * If the provided token is not found, the transaction will resolve to INVALID_TOKEN_ID.
 * If the provided token has been deleted, the transaction will resolve to TOKEN_WAS_DELETED.
 * If an Association between the provided token and account is not found, the transaction will resolve to
 * TOKEN_NOT_ASSOCIATED_TO_ACCOUNT.
 * If Wipe Key is not present in the Token, transaction results in TOKEN_HAS_NO_WIPE_KEY.
 * If the provided account is the Token's Treasury Account, transaction results in CANNOT_WIPE_TOKEN_TREASURY_ACCOUNT
 * On success, tokens are removed from the account and the total supply of the token is decreased by the wiped amount.
 *
 * The amount provided is in the lowest denomination possible. Example:
 * Token A has 2 decimals. In order to wipe 100 tokens from account, one must provide amount of 10000. In order to wipe
 * 100.55 tokens, one must provide amount of 10055.
 */
public final class TokenWipeTransaction extends SingleTransactionBuilder<TokenWipeTransaction> {
    private final TokenWipeAccountTransactionBody.Builder builder = bodyBuilder.getTokenWipeBuilder();

    public TokenWipeTransaction() {
        super();
    }

    /**
     * The token for which the account will be wiped. If token does not exist, transaction results in INVALID_TOKEN_ID
     *
     * @param token
     * @return TokenWipeAccountTransaction
     */
    public TokenWipeTransaction setTokenId(TokenId token) {
        builder.setToken(token.toProto());
        return this;
    }

    /**
     * The account to be wiped
     *
     * @param accountId
     * @return TokenWipeAccountTransaction
     */
    public TokenWipeTransaction setAccountId(AccountId accountId) {
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
    public TokenWipeTransaction setAmount(long amount) {
        builder.setAmount(amount);
        return this;
    }

    public TokenWipeTransaction addSerial(long serial) {
        builder.addSerialNumbers(serial);
        return this;
    }

    public TokenWipeTransaction setSerials(List<Long> serials) {
        builder.clearSerialNumbers();
        builder.addAllSerialNumbers(serials);
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
