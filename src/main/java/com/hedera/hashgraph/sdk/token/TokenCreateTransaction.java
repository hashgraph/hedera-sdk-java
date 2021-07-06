package com.hedera.hashgraph.sdk.token;

import com.hedera.hashgraph.proto.TokenCreateTransactionBody;
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
 * Create a new token. After the token is created, the Token ID for it is in the receipt.
 * The specified Treasury Account is receiving the initial supply of tokens as-well as the tokens from the Token Mint
 * operation once executed. The balance of the treasury account is decreased when the Token Burn operation is executed.
 *
 * The supply that is going to be put in circulation is going to be the initial supply provided. The maximum supply a
 * token can have is 2^63-1.
 *
 * Example:
 * Token A has initial supply set to 10_000 and decimals set to 2. The tokens that will be put into circulation are
 * going be 100. Token B has initial supply set to 10_012_345_678 and decimals set to 8. The number of tokens that
 * will be put into circulation are going to be 100.12345678
 *
 * Creating immutable token: Token can be created as immutable if the adminKey is omitted. In this case, the name,
 * symbol, treasury, management keys, expiry and renew properties cannot be updated. If a token is created as immutable,
 * anyone is able to extend the expiry time by paying the fee.
 *
 * After executing, you can retrieve the token ID via
 * {@link com.hedera.hashgraph.sdk.TransactionId#getReceipt(Client)}
 * and then {@link TransactionReceipt#getTokenId()}.
 */
public final class TokenCreateTransaction extends SingleTransactionBuilder<TokenCreateTransaction> {
    private final TokenCreateTransactionBody.Builder builder = bodyBuilder.getTokenCreationBuilder()
        .setAutoRenewPeriod(DurationHelper.durationFrom(HederaConstants.DEFAULT_AUTORENEW_DURATION))
        .setExpiry(TimestampHelper.timestampFrom(Instant.now().plus(Duration.ofDays(90))));


    public TokenCreateTransaction() {
        super();
    }

    /**
     * Set the publicly visible name of the token, specified as a string of only ASCII characters
     *
     * @param name
     * @return TokenCreateTransaction
     */
    public TokenCreateTransaction setName(String name) {
        builder.setName(name);
        return this;
    }

    /**
     * The publicly visible token symbol. It is UTF-8 capitalized alphabetical string identifying the token
     *
     * @param symbol
     * @return TokenCreateTransaction
     */
    public TokenCreateTransaction setSymbol(String symbol) {
        builder.setSymbol(symbol);
        return this;
    }

    /**
     * The number of decimal places a token is divisible by. This field can never be changed!
     *
     * @param decimal
     * @return TokenCreateTransaction
     */
    public TokenCreateTransaction setDecimals(int decimal) {
        builder.setDecimals(decimal);
        return this;
    }

    /**
     * Specifies the initial supply of tokens to be put in circulation.
     * The initial supply is sent to the Treasury Account.
     * The supply is in the lowest denomination possible.
     *
     * @param initialSupply
     * @return TokenCreateTransaction
     */
    public TokenCreateTransaction setInitialSupply(int initialSupply) {
        builder.setInitialSupply(initialSupply);
        return this;
    }

    /**
     * The account which will act as a treasury for the token. This account will receive the specified initial supply
     *
     * @param treasury
     * @return TokenCreateTransaction
     */
    public TokenCreateTransaction setTreasury(AccountId treasury) {
        builder.setTreasury(treasury.toProto());
        return this;
    }

    /**
     * The key which can perform update/delete operations on the token. If empty, the token can be perceived as
     * immutable (not being able to be updated/deleted)
     *
     * @param key
     * @return TokenCreateTransaction
     */
    public TokenCreateTransaction setAdminKey(PublicKey key) {
        builder.setAdminKey(key.toKeyProto());
        return this;
    }

    /**
     * The key which can grant or revoke KYC of an account for the token's transactions. If empty, KYC is not required,
     * and KYC grant or revoke operations are not possible.
     *
     * @param key
     * @return TokenCreateTransaction
     */
    public TokenCreateTransaction setKycKey(PublicKey key) {
        builder.setKycKey(key.toKeyProto());
        return this;
    }

    /**
     * The key which can sign to freeze or unfreeze an account for token transactions.
     * If empty, freezing is not possible
     *
     * @param key
     * @return TokenCreateTransaction
     */
    public TokenCreateTransaction setFreezeKey(PublicKey key) {
        builder.setFreezeKey(key.toKeyProto());
        return this;
    }

    /**
     * The key which can wipe the token balance of an account. If empty, wipe is not possible
     *
     * @param key
     * @return TokenCreateTransaction
     */
    public TokenCreateTransaction setWipeKey(PublicKey key) {
        builder.setWipeKey(key.toKeyProto());
        return this;
    }

    /**
     * The key which can change the supply of a token. The key is used to sign Token Mint/Burn operations
     *
     * @param key
     * @return TokenCreateTransaction
     */
    public TokenCreateTransaction setSupplyKey(PublicKey key) {
        builder.setSupplyKey(key.toKeyProto());
        return this;
    }

    /**
     * The default Freeze status (frozen or unfrozen) of Hedera accounts relative to this token. If true, an account
     * must be unfrozen before it can receive the token
     *
     * @param freeze
     * @return TokenCreateTransaction
     */
    public TokenCreateTransaction setFreezeDefault(Boolean freeze) {
        builder.setFreezeDefault(freeze);
        return this;
    }

    /**
     * The epoch second at which the token should expire; if an auto-renew account and period are specified, this is
     * coerced to the current epoch second plus the autoRenewPeriod
     *
     * @param expirationTime
     * @return TokenCreateTransaction
     */
    public TokenCreateTransaction setExpirationTime(Instant expirationTime) {
        builder.setExpiry(TimestampHelper.timestampFrom(expirationTime));
        return this;
    }

    /**
     * An account which will be automatically charged to renew the token's expiration, at autoRenewPeriod interval
     *
     * @param account
     * @return TokenCreateTransaction
     */
    public TokenCreateTransaction setAutoRenewAccount(AccountId account) {
        builder.setAutoRenewAccount(account.toProto());
        return this;
    }

    /**
     * The interval at which the auto-renew account will be charged to extend the token's expiry
     *
     * @param period
     * @return TokenCreateTransaction
     */
    public TokenCreateTransaction setAutoRenewPeriod(Duration period) {
        builder.setAutoRenewPeriod(DurationHelper.durationFrom(period));
        return this;
    }

    public TokenCreateTransaction setTokenType(TokenType tokenType) {
        builder.setTokenType(tokenType.code);
        return this;
    }

    public TokenCreateTransaction setSupplyType(TokenSupplyType supplyType) {
        builder.setSupplyType(supplyType.code);
        return this;
    }

    public TokenCreateTransaction setMaxSupply(long maxSupply) {
        builder.setMaxSupply(maxSupply);
        return this;
    }

    public TokenCreateTransaction setCustomFeeList(List<CustomFee> fees) {
        for (CustomFee fee : fees) {
            this.builder.addCustomFees(fee.toProto());
        }
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return TokenServiceGrpc.getCreateTokenMethod();
    }

    @Override
    protected void doValidate() {
    }
}
