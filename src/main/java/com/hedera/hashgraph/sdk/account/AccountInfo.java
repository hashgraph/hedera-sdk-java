package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.sdk.DurationHelper;
import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.crypto.PublicKey;
import com.hederahashgraph.api.proto.java.CryptoGetInfoResponse;
import com.hederahashgraph.api.proto.java.Response;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class AccountInfo {
    private final CryptoGetInfoResponse.AccountInfo inner;

    public AccountId getAccountId() {
        return new AccountId(inner.getAccountIDOrBuilder());
    }

    public String getContractAccountId() {
        return inner.getContractAccountID();
    }

    public boolean isDeleted() {
        return inner.getDeleted();
    }

    @Nullable
    public AccountId getProxyAccountId() {
        return inner.hasProxyAccountID() ? new AccountId(inner.getProxyAccountIDOrBuilder()) : null;
    }

    public long getProxyReceived() {
        return inner.getProxyReceived();
    }

    public PublicKey getKey() {
        return PublicKey.fromProtoKey(inner.getKey());
    }

    public long getBalance() {
        return inner.getBalance();
    }

    public long getGenerateSendRecordThreshold() {
        return inner.getGenerateSendRecordThreshold();
    }

    public long getGenerateReceiveRecordThreshold() {
        return inner.getGenerateReceiveRecordThreshold();
    }

    public boolean isReceiverSignatureRequired() {
        return inner.getReceiverSigRequired();
    }

    public Instant getExpirationTime() {
        return TimestampHelper.timestampTo(inner.getExpirationTime());
    }

    public Duration getAutoRenewPeriod() {
        return DurationHelper.durationTo(inner.getAutoRenewPeriod());
    }

    public List<Claim> getClaims() {
        return inner.getClaimsList()
            .stream()
            .map(Claim::new)
            .collect(Collectors.toList());
    }

    AccountInfo(Response response) {
        if (!response.hasCryptoGetInfo()) {
            throw new IllegalArgumentException("query response was not `CryptoGetInfoResponse`");
        }

        CryptoGetInfoResponse infoResponse = response.getCryptoGetInfo();
        inner = infoResponse.getAccountInfo();

        if (!inner.hasKey()) {
            throw new IllegalArgumentException("query response missing key");
        }
    }
}
