package com.hedera.hashgraph.sdk.token;

import com.google.protobuf.StringValue;
import com.hedera.hashgraph.proto.TokenUpdateTransactionBody;
import com.hedera.hashgraph.proto.TokenServiceGrpc;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.*;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.PublicKey;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import io.grpc.MethodDescriptor;

/**
 * Updates an already created Token. If no value is given for a field, that field is left unchanged. For an immutable
 * tokens (that is, a token created without an adminKey), only the expiry may be updated. Setting any other field in
 * that case will cause the transaction status to resolve to TOKEN_IS_IMMUTABlE.
 */
public final class TokenUpdateTransaction extends SingleTransactionBuilder<TokenUpdateTransaction> {
    private final TokenUpdateTransactionBody.Builder builder = bodyBuilder.getTokenUpdateBuilder();

    public TokenUpdateTransaction() {
        super();
    }

    /**
     * The Token to be updated
     *
     * @param tokenId
     * @return TokenUpdateTransaction
     */
    public TokenUpdateTransaction setTokenId(TokenId tokenId) {
        builder.setToken(tokenId.toProto());
        return this;
    }

    /**
     * The new Name of the Token. Must be a string of ASCII characters.
     *
     * @param name
     * @return TokenUpdateTransaction
     */
    public TokenUpdateTransaction setName(String name) {
        builder.setName(name);
        return this;
    }

    /**
     * The new Symbol of the Token. Must be UTF-8 capitalized alphabetical string identifying the token.
     *
     * @param symbol
     * @return TokenUpdateTransaction
     */
    public TokenUpdateTransaction setSymbol(String symbol) {
        builder.setSymbol(symbol);
        return this;
    }

    /**
     * The new Treasury account of the Token. If the provided treasury account is not existing or deleted, the response
     * will be INVALID_TREASURY_ACCOUNT_FOR_TOKEN. If successful, the Token balance held in the previous
     * Treasury Account is transferred to the new one.
     *
     * @param treasury
     * @return TokenUpdateTransaction
     */
    public TokenUpdateTransaction setTreasury(AccountId treasury) {
        builder.setTreasury(treasury.toProto());
        return this;
    }

    /**
     * The new Admin key of the Token. If Token is immutable, transaction will resolve to TOKEN_IS_IMMUTABlE.
     *
     * @param key
     * @return TokenUpdateTransaction
     */
    public TokenUpdateTransaction setAdminKey(PublicKey key) {
        builder.setAdminKey(key.toKeyProto());
        return this;
    }

    /**
     * The new KYC key of the Token. If Token does not have currently a KYC key, transaction will resolve to
     * TOKEN_HAS_NO_KYC_KEY.
     *
     * @param key
     * @return TokenUpdateTransaction
     */
    public TokenUpdateTransaction setKycKey(PublicKey key) {
        builder.setKycKey(key.toKeyProto());
        return this;
    }

    /**
     * The new Freeze key of the Token. If the Token does not have currently a Freeze key, transaction will resolve to
     * TOKEN_HAS_NO_FREEZE_KEY.
     *
     * @param key
     * @return TokenUpdateTransaction
     */
    public TokenUpdateTransaction setFreezeKey(PublicKey key) {
        builder.setFreezeKey(key.toKeyProto());
        return this;
    }

    /**
     * The new Wipe key of the Token. If the Token does not have currently a Wipe key, transaction will resolve to
     * TOKEN_HAS_NO_WIPE_KEY.
     *
     * @param key
     * @return TokenUpdateTransaction
     */
    public TokenUpdateTransaction setWipeKey(PublicKey key) {
        builder.setWipeKey(key.toKeyProto());
        return this;
    }

    /**
     * The new Supply key of the Token. If the Token does not have currently a Supply key, transaction will resolve to
     * TOKEN_HAS_NO_SUPPLY_KEY.
     *
     * @param key
     * @return TokenUpdateTransaction
     */
    public TokenUpdateTransaction setSupplyKey(PublicKey key) {
        builder.setSupplyKey(key.toKeyProto());
        return this;
    }

    /**
     * he new expiry time of the token. Expiry can be updated even if admin key is not set. If the provided expiry is
     * earlier than the current token expiry, transaction wil resolve to INVALID_EXPIRATION_TIME
     *
     * @param expirationTime
     * @return TokenUpdateTransaction
     */
    public TokenUpdateTransaction setExpirationTime(Instant expirationTime) {
        builder.setExpiry(TimestampHelper.timestampFrom(expirationTime));
        return this;
    }

    /**
     * The new account which will be automatically charged to renew the token's expiration, at autoRenewPeriod interval.
     *
     * @param account
     * @return TokenUpdateTransaction
     */
    public TokenUpdateTransaction setAutoRenewAccount(AccountId account) {
        builder.setAutoRenewAccount(account.toProto());
        return this;
    }

    /**
     * The new interval at which the auto-renew account will be charged to extend the token's expiry.
     *
     * @param period
     * @return TokenUpdateTransaction
     */
    public TokenUpdateTransaction setAutoRenewPeriod(Duration period) {
        builder.setAutoRenewPeriod(DurationHelper.durationFrom(period));
        return this;
    }

    public TokenUpdateTransaction setFeeScheduleKey(PublicKey key) {
        builder.setFeeScheduleKey(key.toKeyProto());
        return this;
    }

    public TokenUpdateTransaction setTokenMemo(String memo) {
        builder.setMemo(StringValue.newBuilder().setValue(memo).build());
        return this;
    }

    public TokenUpdateTransaction clearTokenMemo() {
        builder.clearMemo();
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return TokenServiceGrpc.getUpdateTokenMethod();
    }

    @Override
    protected void doValidate() {
    }
}
