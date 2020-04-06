package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.hedera.hashgraph.sdk.proto.CryptoGetInfoResponse;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;

public final class AccountInfo {
    /**
     * The account ID for which this information applies.
     */
    public final AccountId accountId;

    /**
     * The Contract Account ID comprising of both the contract instance and the cryptocurrency
     * account owned by the contract instance, in the format used by Solidity.
     */
    @Nullable
    public final String contractAccountId;

    /**
     * If true, then this account has been deleted, it will disappear when it expires, and
     * all transactions for it will fail except the transaction to extend its expiration date.
     */
    public final boolean deleted;

    /**
     * The Account ID of the account to which this is proxy staked. If proxyAccountID is null,
     * or is an invalid account, or is an account that isn't a node, then this account is
     * automatically proxy staked to a node chosen by the network, but without earning payments.
     * If the proxyAccountID account refuses to accept proxy staking , or if it is not currently
     * running a node, then it will behave as if proxyAccountID was null.
     */
    @Nullable
    public final AccountId proxyAccountId;

    /**
     * The total proxy staked to this account.
     */
    public final long proxyReceived;

    /**
     * The key for the account, which must sign in order to transfer out, or to modify the account
     * in any way other than extending its expiration date.
     */
    public final Key key;

    /**
     * The current balance of account.
     */
    public final long balance;

    /**
     * The threshold amount for which an account record is created (and this account
     * charged for them) for any send/withdraw transaction.
     */
    public final long sendRecordThreshold;

    /**
     * The threshold amount for which an account record is created
     * (and this account charged for them) for any transaction above this amount.
     */
    public final long receiveRecordThreshold;

    /**
     * If true, no transaction can transfer to this account unless signed by this account's key.
     */
    public final boolean receiverSignatureRequired;

    /**
     * The time at which this account is set to expire.
     */
    public final Instant expirationTime;

    /**
     * The duration for expiration time will extend every this many seconds. If there are
     * insufficient funds, then it extends as long as possible. If it is empty when it
     * expires, then it is deleted.
     */
    public final Duration autoRenewPeriod;

    private AccountInfo(
        AccountId accountId,
        @Nullable String contractAccountId,
        boolean deleted,
        @Nullable AccountId proxyAccountId,
        long proxyReceived,
        Key key,
        long balance,
        long sendRecordThreshold,
        long receiveRecordThreshold,
        boolean receiverSignatureRequired,
        Instant expirationTime,
        Duration autoRenewPeriod
    ) {
        this.accountId = accountId;
        this.contractAccountId = contractAccountId;
        this.deleted = deleted;
        this.proxyAccountId = proxyAccountId;
        this.proxyReceived = proxyReceived;
        this.key = key;
        this.balance = balance;
        this.sendRecordThreshold = sendRecordThreshold;
        this.receiveRecordThreshold = receiveRecordThreshold;
        this.receiverSignatureRequired = receiverSignatureRequired;
        this.expirationTime = expirationTime;
        this.autoRenewPeriod = autoRenewPeriod;
    }

    static AccountInfo fromProtobuf(CryptoGetInfoResponse.AccountInfo accountInfo) {
        var accountId = AccountId.fromProtobuf(accountInfo.getAccountID());

        var proxyAccountId = accountInfo.hasProxyAccountID()
            ? AccountId.fromProtobuf(accountInfo.getProxyAccountID())
            : null;

        return new AccountInfo(
            accountId,
            accountInfo.getContractAccountID(),
            accountInfo.getDeleted(),
            proxyAccountId,
            accountInfo.getProxyReceived(),
            Key.fromProtobuf(accountInfo.getKey()),
            accountInfo.getBalance(),
            accountInfo.getGenerateSendRecordThreshold(),
            accountInfo.getGenerateReceiveRecordThreshold(),
            accountInfo.getReceiverSigRequired(),
            InstantConverter.fromProtobuf(accountInfo.getExpirationTime()),
            DurationConverter.fromProtobuf(accountInfo.getAutoRenewPeriod())
        );
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("accountId", accountId)
            .add("contractAccountId", contractAccountId)
            .add("deleted", deleted)
            .add("proxyAccountId", proxyAccountId)
            .add("proxyReceived", proxyReceived)
            .add("key", key)
            .add("balance", balance)
            .add("sendRecordThreshold", sendRecordThreshold)
            .add("receiveRecordThreshold", receiveRecordThreshold)
            .add("receiverSignatureRequired", receiverSignatureRequired)
            .add("expirationTime", expirationTime)
            .add("autoRenewPeriod", autoRenewPeriod)
            .toString();
    }
}
