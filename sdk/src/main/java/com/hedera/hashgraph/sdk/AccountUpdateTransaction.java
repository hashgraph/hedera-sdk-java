package com.hedera.hashgraph.sdk;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.CryptoUpdateTransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Change properties for the given account.
 * <p>
 * Any null field is ignored (left unchanged).
 * <p>
 * This transaction must be signed by the existing key for this account.
 * <p>
 * If the transaction is changing the key field, then the transaction must be signed by
 * both the old key (from before the change) and the new key. The old key must sign for security.
 * The new key must sign as a safeguard to avoid accidentally
 * changing to an invalid key, and then having no way to recover.
 * <p>
 * When extending the expiration date, the cost is affected by the size
 * of the list of attached claims, and of the keys
 * associated with the claims and the account.
 */
public final class AccountUpdateTransaction extends Transaction<AccountUpdateTransaction> {
    @Nullable
    private AccountId accountId = null;
    @Nullable
    private AccountId proxyAccountId = null;
    @Nullable
    private Key key = null;
    @Nullable
    private Instant expirationTime = null;
    @Nullable
    private Duration autoRenewPeriod = null;
    @Nullable
    private Boolean receiverSigRequired = null;
    @Nullable
    private String accountMemo = null;
    @Nullable
    private Integer maxAutomaticTokenAssociations = null;
    @Nullable
    private Key aliasKey;

    /**
     * Constructor.
     */
    public AccountUpdateTransaction() {
    }

    /**
     * Constructor.
     *
     * @param txs                       Compound list of transaction id's list of (AccountId, Transaction) records
     * @throws InvalidProtocolBufferException
     */
    AccountUpdateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * Constructor.
     *
     * @param txBody                    protobuf TransactionBody
     */
    AccountUpdateTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    /**
     * @return                          the account id
     */
    @Nullable
    public AccountId getAccountId() {
        return accountId;
    }

    /**
     * Sets the account ID which is being updated in this transaction.
     *
     * @param accountId The AccountId to be set
     * @return {@code this}
     */
    public AccountUpdateTransaction setAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId);
        requireNotFrozen();
        this.accountId = accountId;
        return this;
    }

    /**
     * @return                          the key
     */
    @Nullable
    public Key getKey() {
        return key;
    }

    /**
     * Sets the new key.
     *
     * @param key The Key to be set
     * @return {@code this}
     */
    public AccountUpdateTransaction setKey(Key key) {
        Objects.requireNonNull(key);
        requireNotFrozen();
        this.key = key;
        return this;
    }

    /**
     * @deprecated with no replacement
     */
    @Deprecated
    @Nullable
    public Key getAliasKey() {
        return aliasKey;
    }

    /**
     * @deprecated with no replacement
     *
     * Sets the new key.
     *
     * @param aliasKey The Key to be set
     * @return {@code this}
     */
    @Deprecated
    public AccountUpdateTransaction setAliasKey(Key aliasKey) {
        Objects.requireNonNull(aliasKey);
        requireNotFrozen();
        this.aliasKey = aliasKey;
        return this;
    }

    /**
     * @return                          the proxy account id
     */
    @Nullable
    public AccountId getProxyAccountId() {
        return proxyAccountId;
    }

    /**
     * Sets the ID of the account to which this account is proxy staked.
     * <p>
     * If proxyAccountID is null, or is an invalid account, or is an account
     * that isn't a node, then this account is automatically proxy staked to
     * a node chosen by the network, but without earning payments.
     * <p>
     * If the proxyAccountID account refuses to accept proxy staking, or
     * if it is not currently running a node, then it
     * will behave as if proxyAccountID was null.
     *
     * @param proxyAccountId The AccountId to be set
     * @return {@code this}
     */
    public AccountUpdateTransaction setProxyAccountId(AccountId proxyAccountId) {
        Objects.requireNonNull(proxyAccountId);
        requireNotFrozen();
        this.proxyAccountId = proxyAccountId;
        return this;
    }

    /**
     * @return                          the expiration time
     */
    @Nullable
    public Instant getExpirationTime() {
        return expirationTime;
    }

    /**
     * Sets the new expiration time to extend to (ignored if equal to or
     * before the current one).
     *
     * @param expirationTime The Instant to be set as the expiration time
     * @return {@code this}
     */
    public AccountUpdateTransaction setExpirationTime(Instant expirationTime) {
        Objects.requireNonNull(expirationTime);
        requireNotFrozen();
        this.expirationTime = expirationTime;
        return this;
    }

    /**
     * @return                          the auto renew period
     */
    @Nullable
    public Duration getAutoRenewPeriod() {
        return autoRenewPeriod;
    }

    /**
     * Sets the duration in which it will automatically extend the expiration period.
     * <p>
     * If it doesn't have enough balance, it extends as long as possible.
     * If it is empty when it expires, then it is deleted.
     *
     * @param autoRenewPeriod The Duration to be set for auto renewal
     * @return {@code this}
     */
    public AccountUpdateTransaction setAutoRenewPeriod(Duration autoRenewPeriod) {
        Objects.requireNonNull(autoRenewPeriod);
        requireNotFrozen();
        this.autoRenewPeriod = autoRenewPeriod;
        return this;
    }

    /**
     * @return                          is the receiver required to sign
     */
    @Nullable
    public Boolean getReceiverSignatureRequired() {
        return receiverSigRequired;
    }

    /**
     * Sets whether this account's key must sign any transaction
     * depositing into this account (in addition to all withdrawals).
     *
     * @param receiverSignatureRequired The bool to be set
     * @return {@code this}
     */
    public AccountUpdateTransaction setReceiverSignatureRequired(boolean receiverSignatureRequired) {
        requireNotFrozen();
        receiverSigRequired = receiverSignatureRequired;
        return this;
    }

    /**
     * @return                          the max automatic token associations
     */
    @Nullable
    public Integer getMaxAutomaticTokenAssociations() {
        return maxAutomaticTokenAssociations;
    }

    /**
     * Grant an amount of tokens.   // TODO: need to verify I think I'm wrong
     * @param amount                    the amount of tokens
     * @return                          {@code this}
     */
    public AccountUpdateTransaction setMaxAutomaticTokenAssociations(int amount) {
        requireNotFrozen();
        maxAutomaticTokenAssociations = amount;
        return this;
    }

    /**
     * @return                          the account memo
     */
    @Nullable
    public String getAccountMemo() {
        return accountMemo;
    }

    /**
     * Assign a memo to the account.
     *
     * @param memo                      the memo
     * @return                          {@code this}
     */
    public AccountUpdateTransaction setAccountMemo(String memo) {
        requireNotFrozen();
        Objects.requireNonNull(memo);
        accountMemo = memo;
        return this;
    }

    /**
     * @return {@code this}
     */
    public AccountUpdateTransaction clearMemo() {
        requireNotFrozen();
        accountMemo = "";
        return this;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (accountId != null) {
            accountId.validateChecksum(client);
        }
        if (proxyAccountId != null) {
            proxyAccountId.validateChecksum(client);
        }
    }
    /**
     * Initialize from the transaction body.
     */
    void initFromTransactionBody() {
        var body = sourceTransactionBody.getCryptoUpdateAccount();

        if (body.hasAccountIDToUpdate()) {
            accountId = AccountId.fromProtobuf(body.getAccountIDToUpdate());
        }
        if (body.hasProxyAccountID()) {
            proxyAccountId = AccountId.fromProtobuf(body.getProxyAccountID());
        }
        if (body.hasKey()) {
            key = Key.fromProtobufKey(body.getKey());
        }
        if (body.hasExpirationTime()) {
            expirationTime = InstantConverter.fromProtobuf(body.getExpirationTime());
        }
        if (body.hasAutoRenewPeriod()) {
            autoRenewPeriod = DurationConverter.fromProtobuf(body.getAutoRenewPeriod());
        }
        if (body.hasReceiverSigRequiredWrapper()) {
            receiverSigRequired = body.getReceiverSigRequiredWrapper().getValue();
        }
        if (body.hasMemo()) {
            accountMemo = body.getMemo().getValue();
        }
        if (body.hasMaxAutomaticTokenAssociations()) {
            maxAutomaticTokenAssociations = body.getMaxAutomaticTokenAssociations().getValue();
        }
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return CryptoServiceGrpc.getUpdateAccountMethod();
    }

    /**
     * @return                          the transaction builder
     */
    CryptoUpdateTransactionBody.Builder build() {
        var builder = CryptoUpdateTransactionBody.newBuilder();
        if (accountId != null) {
            builder.setAccountIDToUpdate(accountId.toProtobuf());
        }
        if (proxyAccountId != null) {
            builder.setProxyAccountID(proxyAccountId.toProtobuf());
        }
        if (key != null) {
            builder.setKey(key.toProtobufKey());
        }
        if (expirationTime != null) {
            builder.setExpirationTime(InstantConverter.toProtobuf(expirationTime));
        }
        if (autoRenewPeriod != null) {
            builder.setAutoRenewPeriod(DurationConverter.toProtobuf(autoRenewPeriod));
        }
        if (receiverSigRequired != null) {
            builder.setReceiverSigRequiredWrapper(BoolValue.of(receiverSigRequired));
        }
        if (accountMemo != null) {
            builder.setMemo(StringValue.of(accountMemo));
        }
        if (maxAutomaticTokenAssociations != null) {
            builder.setMaxAutomaticTokenAssociations(Int32Value.of(maxAutomaticTokenAssociations));
        }
        return builder;
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setCryptoUpdateAccount(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setCryptoUpdateAccount(build());
    }
}
