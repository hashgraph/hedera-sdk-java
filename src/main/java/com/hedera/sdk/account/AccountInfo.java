package com.hedera.sdk.account;

import com.hedera.sdk.AccountId;
import com.hedera.sdk.Claim;
import com.hedera.sdk.DurationHelper;
import com.hedera.sdk.TimestampHelper;
import com.hedera.sdk.crypto.Key;
import com.hedera.sdk.proto.Response;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class AccountInfo {
    public final AccountId accountId;
    public final String contractAccountId;
    public final boolean deleted;

    @Nullable
    public final AccountId proxyAccountId;

    public final int proxyFraction;
    public final long proxyReceived;

    public final Key key;

    public final long balance;
    public final long generateSendRecordThreshold;
    public final long generateReceiveRecordThreshold;

    public final boolean receiverSigRequired;

    public final Instant expirationTime;

    public final Duration autoRenewPeriod;

    public final List<Claim> claims;

    AccountInfo(Response response) {
        if (!response.hasCryptoGetInfo()) {
            throw new IllegalArgumentException("query response was not `CryptoGetInfoResponse`");
        }

        var infoResponse = response.getCryptoGetInfo();
        var accountInfo = infoResponse.getAccountInfo();
        accountId = new AccountId(accountInfo.getAccountID());
        contractAccountId = accountInfo.getContractAccountID();
        deleted = accountInfo.getDeleted();
        proxyAccountId = accountInfo.hasProxyAccountID() ? new AccountId(accountInfo.getProxyAccountID()) : null;
        proxyFraction = accountInfo.getProxyFraction();
        proxyReceived = accountInfo.getProxyReceived();

        key = Key.fromProtoKey(accountInfo.getKey());

        balance = accountInfo.getBalance();
        generateSendRecordThreshold = accountInfo.getGenerateSendRecordThreshold();
        generateReceiveRecordThreshold = accountInfo.getGenerateReceiveRecordThreshold();

        receiverSigRequired = accountInfo.getReceiverSigRequired();
        expirationTime = TimestampHelper.timestampToInstant(accountInfo.getExpirationTime());
        autoRenewPeriod = DurationHelper.durationToJava(accountInfo.getAutoRenewPeriod());

        claims = accountInfo.getClaimsList()
            .stream()
            .map(Claim::fromProto)
            .collect(Collectors.toList());
    }
}
