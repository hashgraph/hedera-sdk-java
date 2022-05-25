/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.CryptoCreateTransactionBody;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import org.threeten.bp.Duration;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Create a new Hedera™ account.
 */
public final class AccountCreateTransaction extends Transaction<AccountCreateTransaction> {
    @Nullable
    private AccountId proxyAccountId = null;
    @Nullable
    private Key key = null;
    private String accountMemo = "";
    private Hbar initialBalance = new Hbar(0);
    private boolean receiverSigRequired = false;
    private Duration autoRenewPeriod = DEFAULT_AUTO_RENEW_PERIOD;
    private int maxAutomaticTokenAssociations = 0;

    /**
     * Constructor.
     */
    public AccountCreateTransaction() {
    }

    /**
     * Constructor.
     *
     * @param txs                                   Compound list of transaction id's list of (AccountId, Transaction) records
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    AccountCreateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * Constructor.
     *
     * @param txBody                    protobuf TransactionBody
     */
    AccountCreateTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    /**
     * Extract the key.
     *
     * @return                          the creating account's key
     */
    @Nullable
    public Key getKey() {
        return key;
    }

    /**
     * Set the key for this account.
     *
     * <p>The key that must sign each transfer out of the account. If receiverSignatureRequired is
     * true, then it must also sign any transfer into the account.
     *
     * @param key the key for this account.
     * @return {@code this}
     */
    public AccountCreateTransaction setKey(Key key) {
        Objects.requireNonNull(key);
        requireNotFrozen();
        this.key = key;
        return this;
    }

    /**
     * Extract the amount in hbar.
     *
     * @return                          the initial balance for the new account
     */
    public Hbar getInitialBalance() {
        return initialBalance;
    }

    /**
     * Set the initial amount to transfer into this account.
     *
     * @param initialBalance the initial balance.
     * @return {@code this}
     */
    public AccountCreateTransaction setInitialBalance(Hbar initialBalance) {
        Objects.requireNonNull(initialBalance);
        requireNotFrozen();
        this.initialBalance = initialBalance;
        return this;
    }

    /**
     * Is the receiver required to sign?
     *
     * @return                          is the receiver required to sign
     */
    public boolean getReceiverSignatureRequired() {
        return receiverSigRequired;
    }

    /**
     * Set to true to require this account to sign any transfer of hbars to this account.
     *
     * <p>All transfers of hbars from this account must always be signed. This property only affects
     * transfers to this account.
     *
     * @param receiveSignatureRequired true to require a signature when receiving hbars.
     * @return {@code this}
     */
    public AccountCreateTransaction setReceiverSignatureRequired(boolean receiveSignatureRequired) {
        requireNotFrozen();
        receiverSigRequired = receiveSignatureRequired;
        return this;
    }

    /**
     * Extract the proxy account id.
     *
     * @return                          the proxy account id
     */
    @Nullable
    public AccountId getProxyAccountId() {
        return proxyAccountId;
    }

    /**
     * Set the ID of the account to which this account is proxy staked.
     *
     * @param proxyAccountId the proxy account ID.
     * @return {@code this}
     */
    public AccountCreateTransaction setProxyAccountId(AccountId proxyAccountId) {
        requireNotFrozen();
        Objects.requireNonNull(proxyAccountId);
        this.proxyAccountId = proxyAccountId;
        return this;
    }

    /**
     * Extract the duration for the auto renew period.
     *
     * @return                          the duration for auto-renew
     */
    @Nullable
    public Duration getAutoRenewPeriod() {
        return autoRenewPeriod;
    }

    /**
     * Set the auto renew period for this account.
     *
     * <p>A Hedera™ account is charged to extend its expiration date every renew period. If it
     * doesn't have enough balance, it extends as long as possible. If the balance is zero when it
     * expires, then the account is deleted.
     *
     * <p>This is defaulted to 3 months by the SDK.
     *
     * @param autoRenewPeriod the auto renew period for this account.
     * @return {@code this}
     */
    public AccountCreateTransaction setAutoRenewPeriod(Duration autoRenewPeriod) {
        requireNotFrozen();
        Objects.requireNonNull(autoRenewPeriod);
        this.autoRenewPeriod = autoRenewPeriod;
        return this;
    }

    /**
     * Extract the maximum automatic token associations.
     *
     * @return                          the max automatic token associations
     */
    public int getMaxAutomaticTokenAssociations() {
        return maxAutomaticTokenAssociations;
    }

    /**
     * Grant an amount of tokens.
     *
     * @param amount                    the amount of tokens
     * @return                          {@code this}
     */
    public AccountCreateTransaction setMaxAutomaticTokenAssociations(int amount) {
        requireNotFrozen();
        maxAutomaticTokenAssociations = amount;
        return this;
    }

    /**
     * Extract the account memo.
     *
     * @return                          the account memo
     */
    public String getAccountMemo() {
        return accountMemo;
    }

    /**
     * Assign a memo to the account.
     *
     * @param memo                      the memo
     * @return                          {@code this}
     */
    public AccountCreateTransaction setAccountMemo(String memo) {
        Objects.requireNonNull(memo);
        requireNotFrozen();
        accountMemo = memo;
        return this;
    }

    /**
     * Build the transaction body.
     *
     * @return {@link com.hedera.hashgraph.sdk.proto.CryptoApproveAllowanceTransactionBody}
     */
    CryptoCreateTransactionBody.Builder build() {
        var builder = CryptoCreateTransactionBody.newBuilder()
            .setInitialBalance(initialBalance.toTinybars())
            .setReceiverSigRequired(receiverSigRequired)
            .setAutoRenewPeriod(DurationConverter.toProtobuf(autoRenewPeriod))
            .setMemo(accountMemo)
            .setMaxAutomaticTokenAssociations(maxAutomaticTokenAssociations);

        if (proxyAccountId != null) {
            builder.setProxyAccountID(proxyAccountId.toProtobuf());
        }

        if (key != null) {
            builder.setKey(key.toProtobufKey());
        }

        return builder;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (proxyAccountId != null) {
            proxyAccountId.validateChecksum(client);
        }
    }

    /**
     * Initialize from the transaction body.
     */
    void initFromTransactionBody() {
        var body = sourceTransactionBody.getCryptoCreateAccount();

        if (body.hasProxyAccountID()) {
            proxyAccountId = AccountId.fromProtobuf(body.getProxyAccountID());
        }
        if (body.hasKey()) {
            key = Key.fromProtobufKey(body.getKey());
        }
        if (body.hasAutoRenewPeriod()) {
            autoRenewPeriod = DurationConverter.fromProtobuf(body.getAutoRenewPeriod());
        }
        initialBalance = Hbar.fromTinybars(body.getInitialBalance());
        accountMemo = body.getMemo();
        receiverSigRequired = body.getReceiverSigRequired();
        maxAutomaticTokenAssociations = body.getMaxAutomaticTokenAssociations();
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return CryptoServiceGrpc.getCreateAccountMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setCryptoCreateAccount(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setCryptoCreateAccount(build());
    }
}
