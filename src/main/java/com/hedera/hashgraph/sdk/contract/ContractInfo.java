package com.hedera.hashgraph.sdk.contract;

import com.hedera.hashgraph.proto.ContractGetInfoResponse;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.sdk.DurationHelper;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.account.TokenRelationship;
import com.hedera.hashgraph.sdk.crypto.PublicKey;
import com.hedera.hashgraph.sdk.token.TokenId;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public final class ContractInfo {
    public final ContractId contractId;
    public final AccountId accountId;

    public final String contractAccountId;

    @Nullable
    public final PublicKey adminKey;

    public final Instant expirationTime;

    public final Duration autoRenewPeriod;

    public final long storage;

    /**
     * The memo for the contract itself, set by
     * {@link ContractCreateTransaction#setContractMemo(String)} or
     * {@link ContractUpdateTransaction#setContractMemo(String)}.
     *
     * Null if the memo was empty.
     */
    @Nullable
    public final String contractMemo;

    public final Hbar balance;

    public final boolean isDeleted;

    public final Map<TokenId, TokenRelationship> tokenRelationships;

    ContractInfo(ContractGetInfoResponse.ContractInfoOrBuilder info) {
        if (!info.hasContractID()) {
            throw new IllegalArgumentException("info is empty");
        }

        contractId = new ContractId(info.getContractIDOrBuilder());
        accountId = new AccountId(info.getAccountIDOrBuilder());
        contractAccountId = info.getContractAccountID();
        adminKey = info.hasAdminKey() ? PublicKey.fromProtoKey(info.getAdminKey()) : null;
        expirationTime = TimestampHelper.timestampTo(info.getExpirationTime());
        autoRenewPeriod = DurationHelper.durationTo(info.getAutoRenewPeriod());
        storage = info.getStorage();
        String memo = info.getMemo();
        contractMemo = memo.isEmpty() ? null : memo;
        balance = Hbar.fromTinybar(info.getBalance());
        isDeleted = info.getDeleted();

        HashMap<TokenId, TokenRelationship> relationships = new HashMap<>(info.getTokenRelationshipsCount());

        for (com.hedera.hashgraph.proto.TokenRelationship relationship : info.getTokenRelationshipsList()) {
            relationships.put(new TokenId(relationship.getTokenId()), new TokenRelationship(relationship));
        }

        tokenRelationships = relationships;
    }

    static ContractInfo fromResponse(Response response) {
        if (!response.hasContractGetInfo()) {
            throw new IllegalArgumentException("response was not `contractGetInfo`");
        }

        return new ContractInfo(response.getContractGetInfo().getContractInfo());
    }
}
