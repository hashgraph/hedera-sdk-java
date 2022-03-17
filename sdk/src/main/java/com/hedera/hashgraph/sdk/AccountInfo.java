package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.CryptoGetInfoResponse;
import java8.util.J8Arrays;
import java8.util.stream.Collectors;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Current information about an account, including the balance.
 */
public final class AccountInfo {
    /**
     * The account ID for which this information applies.
     */
    public final AccountId accountId;

    /**
     * The Contract Account ID comprising of both the contract instance and the cryptocurrency
     * account owned by the contract instance, in the format used by Solidity.
     */
    public final String contractAccountId;

    /**
     * If true, then this account has been deleted, it will disappear when it expires, and
     * all transactions for it will fail except the transaction to extend its expiration date.
     */
    public final boolean isDeleted;

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
    public final Hbar proxyReceived;

    /**
     * The key for the account, which must sign in order to transfer out, or to modify the account
     * in any way other than extending its expiration date.
     */
    public final Key key;

    /**
     * The current balance of account.
     */
    public final Hbar balance;

    /**
     * The threshold amount for which an account record is created (and this account
     * charged for them) for any send/withdraw transaction.
     */
    public final Hbar sendRecordThreshold;

    /**
     * The threshold amount for which an account record is created
     * (and this account charged for them) for any transaction above this amount.
     */
    public final Hbar receiveRecordThreshold;

    /**
     * If true, no transaction can transfer to this account unless signed by this account's key.
     */
    public final boolean isReceiverSignatureRequired;

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

    public final List<LiveHash> liveHashes;

    public final Map<TokenId, TokenRelationship> tokenRelationships;

    public final String accountMemo;

    public final long ownedNfts;

    public final int maxAutomaticTokenAssociations;

    /**
     * The ledger ID the response was returned from; please see <a href="https://github.com/hashgraph/hedera-improvement-proposal/blob/master/HIP/hip-198.md">HIP-198</a> for the network-specific IDs.
     */
    public final LedgerId ledgerId;

    @Nullable
    public final PublicKey aliasKey;

    public final List<HbarAllowance> hbarAllowances;

    public final List<TokenAllowance> tokenAllowances;

    public final List<TokenNftAllowance> tokenNftAllowances;

    private AccountInfo(
        AccountId accountId,
        String contractAccountId,
        boolean isDeleted,
        @Nullable AccountId proxyAccountId,
        long proxyReceived,
        Key key,
        long balance,
        long sendRecordThreshold,
        long receiveRecordThreshold,
        boolean receiverSignatureRequired,
        Instant expirationTime,
        Duration autoRenewPeriod,
        List<LiveHash> liveHashes,
        Map<TokenId, TokenRelationship> tokenRelationships,
        String accountMemo,
        long ownedNfts,
        int maxAutomaticTokenAssociations,
        @Nullable PublicKey aliasKey,
        LedgerId ledgerId,
        List<HbarAllowance> hbarAllowances,
        List<TokenAllowance> tokenAllowances,
        List<TokenNftAllowance> tokenNftAllowances
    ) {
        this.accountId = accountId;
        this.contractAccountId = contractAccountId;
        this.isDeleted = isDeleted;
        this.proxyAccountId = proxyAccountId;
        this.proxyReceived = Hbar.fromTinybars(proxyReceived);
        this.key = key;
        this.balance = Hbar.fromTinybars(balance);
        this.sendRecordThreshold = Hbar.fromTinybars(sendRecordThreshold);
        this.receiveRecordThreshold = Hbar.fromTinybars(receiveRecordThreshold);
        this.isReceiverSignatureRequired = receiverSignatureRequired;
        this.expirationTime = expirationTime;
        this.autoRenewPeriod = autoRenewPeriod;
        this.liveHashes = liveHashes;
        this.tokenRelationships = Collections.unmodifiableMap(tokenRelationships);
        this.accountMemo = accountMemo;
        this.ownedNfts = ownedNfts;
        this.maxAutomaticTokenAssociations = maxAutomaticTokenAssociations;
        this.aliasKey = aliasKey;
        this.ledgerId = ledgerId;
        this.hbarAllowances = hbarAllowances;
        this.tokenAllowances = tokenAllowances;
        this.tokenNftAllowances = tokenNftAllowances;
    }

    static AccountInfo fromProtobuf(CryptoGetInfoResponse.AccountInfo accountInfo) {
        var accountId = AccountId.fromProtobuf(accountInfo.getAccountID());

        var proxyAccountId = accountInfo.getProxyAccountID().getAccountNum() > 0
            ? AccountId.fromProtobuf(accountInfo.getProxyAccountID())
            : null;

        var liveHashes = J8Arrays.stream(accountInfo.getLiveHashesList().toArray())
            .map((liveHash) -> LiveHash.fromProtobuf((com.hedera.hashgraph.sdk.proto.LiveHash) liveHash))
            .collect(Collectors.toList());

        Map<TokenId, TokenRelationship> relationships = new HashMap<>();

        for (com.hedera.hashgraph.sdk.proto.TokenRelationship relationship : accountInfo.getTokenRelationshipsList()) {
            TokenId tokenId = TokenId.fromProtobuf(relationship.getTokenId());
            relationships.put(tokenId, TokenRelationship.fromProtobuf(relationship));
        }

        List<HbarAllowance> hbarAllowances = new ArrayList<>(accountInfo.getGrantedCryptoAllowancesCount());
        for (var allowanceProto : accountInfo.getGrantedCryptoAllowancesList()) {
            hbarAllowances.add(HbarAllowance.fromProtobuf(allowanceProto).withOwner(accountId));
        }

        List<TokenAllowance> tokenAllowances = new ArrayList<>(accountInfo.getGrantedTokenAllowancesCount());
        for (var allowanceProto : accountInfo.getGrantedTokenAllowancesList()) {
            tokenAllowances.add(TokenAllowance.fromProtobuf(allowanceProto).withOwner(accountId));
        }

        List<TokenNftAllowance> nftAllowances = new ArrayList<>(accountInfo.getGrantedNftAllowancesCount());
        for (var allowanceProto : accountInfo.getGrantedNftAllowancesList()) {
            nftAllowances.add(TokenNftAllowance.fromProtobuf(allowanceProto).withOwner(accountId));
        }

        @Nullable
        var aliasKey = PublicKey.fromAliasBytes(accountInfo.getAlias());

        return new AccountInfo(
            accountId,
            accountInfo.getContractAccountID(),
            accountInfo.getDeleted(),
            proxyAccountId,
            accountInfo.getProxyReceived(),
            Key.fromProtobufKey(accountInfo.getKey()),
            accountInfo.getBalance(),
            accountInfo.getGenerateSendRecordThreshold(),
            accountInfo.getGenerateReceiveRecordThreshold(),
            accountInfo.getReceiverSigRequired(),
            InstantConverter.fromProtobuf(accountInfo.getExpirationTime()),
            DurationConverter.fromProtobuf(accountInfo.getAutoRenewPeriod()),
            liveHashes,
            relationships,
            accountInfo.getMemo(),
            accountInfo.getOwnedNfts(),
            accountInfo.getMaxAutomaticTokenAssociations(),
            aliasKey,
            LedgerId.fromByteString(accountInfo.getLedgerId()),
            hbarAllowances,
            tokenAllowances,
            nftAllowances
        );
    }

    public static AccountInfo fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(CryptoGetInfoResponse.AccountInfo.parseFrom(bytes).toBuilder().build());
    }

    CryptoGetInfoResponse.AccountInfo toProtobuf() {
        var hashes = J8Arrays.stream(liveHashes.toArray())
            .map((liveHash) -> ((LiveHash) liveHash).toProtobuf())
            .collect(Collectors.toList());

        var accountInfoBuilder = CryptoGetInfoResponse.AccountInfo.newBuilder()
            .setAccountID(accountId.toProtobuf())
            .setDeleted(isDeleted)
            .setProxyReceived(proxyReceived.toTinybars())
            .setKey(key.toProtobufKey())
            .setBalance(balance.toTinybars())
            .setGenerateSendRecordThreshold(sendRecordThreshold.toTinybars())
            .setGenerateReceiveRecordThreshold(receiveRecordThreshold.toTinybars())
            .setReceiverSigRequired(isReceiverSignatureRequired)
            .setExpirationTime(InstantConverter.toProtobuf(expirationTime))
            .setAutoRenewPeriod(DurationConverter.toProtobuf(autoRenewPeriod))
            .addAllLiveHashes(hashes)
            .setMemo(accountMemo)
            .setOwnedNfts(ownedNfts)
            .setMaxAutomaticTokenAssociations(maxAutomaticTokenAssociations)
            .setLedgerId(ledgerId.toByteString());

        if (contractAccountId != null) {
            accountInfoBuilder.setContractAccountID(contractAccountId);
        }

        if (proxyAccountId != null) {
            accountInfoBuilder.setProxyAccountID(proxyAccountId.toProtobuf());
        }

        if (aliasKey != null) {
            accountInfoBuilder.setAlias(aliasKey.toProtobufKey().toByteString());
        }

        for (var allowance : hbarAllowances) {
            accountInfoBuilder.addGrantedCryptoAllowances(allowance.toGrantedProtobuf());
        }

        for (var allowance : tokenAllowances) {
            accountInfoBuilder.addGrantedTokenAllowances(allowance.toGrantedProtobuf());
        }

        for (var allowance : tokenNftAllowances) {
            accountInfoBuilder.addGrantedNftAllowances(allowance.toGrantedProtobuf());
        }

        return accountInfoBuilder.build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("accountId", accountId)
            .add("contractAccountId", contractAccountId)
            .add("deleted", isDeleted)
            .add("proxyAccountId", proxyAccountId)
            .add("proxyReceived", proxyReceived)
            .add("key", key)
            .add("balance", balance)
            .add("sendRecordThreshold", sendRecordThreshold)
            .add("receiveRecordThreshold", receiveRecordThreshold)
            .add("receiverSignatureRequired", isReceiverSignatureRequired)
            .add("expirationTime", expirationTime)
            .add("autoRenewPeriod", autoRenewPeriod)
            .add("liveHashes", liveHashes)
            .add("tokenRelationships", tokenRelationships)
            .add("accountMemo", accountMemo)
            .add("ownedNfts", ownedNfts)
            .add("maxAutomaticTokenAssociations", maxAutomaticTokenAssociations)
            .add("aliasKey", aliasKey)
            .add("ledgerId", ledgerId)
            .add("hbarAllowances", hbarAllowances)
            .add("tokenAllowances", tokenAllowances)
            .add("tokenNftAllowances", tokenNftAllowances)
            .toString();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
