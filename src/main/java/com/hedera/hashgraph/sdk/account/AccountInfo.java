package com.hedera.hashgraph.sdk.account;

import com.google.common.base.MoreObjects;
import com.hedera.hashgraph.proto.CryptoGetInfoResponse;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.sdk.DurationHelper;
import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.crypto.PublicKey;
import com.hedera.hashgraph.sdk.token.TokenId;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public final class AccountInfo {
    public final AccountId accountId;
    public final String contractAccountId;

    public final boolean isDeleted;

    @Nullable
    public final AccountId proxyAccountId;
    public final long proxyReceived;

    public final PublicKey key;

    public final long balance;

    public final long generateSendRecordThreshold;
    public final long generateReceiveRecordThreshold;

    public final boolean isReceiverSignatureRequired;

    public final Instant expirationTime;

    public final Duration autoRenewPeriod;

    public final Map<TokenId, TokenRelationship> tokenRelationships;

    public final long ownedNfts;

    AccountInfo(CryptoGetInfoResponse.AccountInfoOrBuilder info) {
        if (!info.hasKey()) {
            throw new IllegalArgumentException("query response missing key");
        }

        accountId = new AccountId(info.getAccountIDOrBuilder());
        contractAccountId = info.getContractAccountID();
        isDeleted = info.getDeleted();
        proxyAccountId = info.hasProxyAccountID() ? new AccountId(info.getProxyAccountIDOrBuilder()) : null;
        proxyReceived = info.getProxyReceived();
        key = PublicKey.fromProtoKey(info.getKeyOrBuilder());
        balance = info.getBalance();
        generateSendRecordThreshold = info.getGenerateSendRecordThreshold();
        generateReceiveRecordThreshold = info.getGenerateReceiveRecordThreshold();
        isReceiverSignatureRequired = info.getReceiverSigRequired();
        expirationTime = TimestampHelper.timestampTo(info.getExpirationTime());
        autoRenewPeriod = DurationHelper.durationTo(info.getAutoRenewPeriod());

        HashMap<TokenId, TokenRelationship> relationships = new HashMap<>();

        for (com.hedera.hashgraph.proto.TokenRelationship relationship : info.getTokenRelationshipsList()) {
            TokenId tokenId = new TokenId(relationship.getTokenId());
            relationships.put(tokenId, new TokenRelationship(relationship));
        }

        this.tokenRelationships = Collections.unmodifiableMap(relationships);
        this.ownedNfts = info.getOwnedNfts();
    }

    static AccountInfo fromResponse(Response response) {
        if (!response.hasCryptoGetInfo()) {
            throw new IllegalArgumentException("query response was not `CryptoGetInfoResponse`");
        }

        CryptoGetInfoResponse infoResponse = response.getCryptoGetInfo();

        return new AccountInfo(infoResponse.getAccountInfo());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("accountId", accountId)
            .add("contractAccountId", contractAccountId)
            .add("isDeleted", isDeleted)
            .add("proxyAccountId", proxyAccountId)
            .add("proxyReceived", proxyReceived)
            .add("key", key)
            .add("balance", balance)
            .add("generateSendRecordThreshold", generateSendRecordThreshold)
            .add("generateReceiveRecordThreshold", generateReceiveRecordThreshold)
            .add("isReceiverSignatureRequired", isReceiverSignatureRequired)
            .add("expirationTime", expirationTime)
            .add("autoRenewPeriod", autoRenewPeriod)
            .add("tokenRelationships", tokenRelationships)
            .add("ownedNfts", ownedNfts)
            .toString();
    }
}
