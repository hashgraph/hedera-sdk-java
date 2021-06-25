package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.ContractGetInfoResponse;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Current information on the smart contract instance, including its balance.
 */
public final class ContractInfo {
    /**
     * ID of the contract instance, in the format used in transactions.
     */
    public final ContractId contractId;

    /**
     * ID of the cryptocurrency account owned by the contract instance,
     * in the format used in transactions.
     */
    public final AccountId accountId;

    /**
     * ID of both the contract instance and the cryptocurrency account owned by the contract
     * instance, in the format used by Solidity.
     */
    public final String contractAccountId;

    /**
     * The state of the instance and its fields can be modified arbitrarily if this key signs a
     * transaction to modify it. If this is null, then such modifications are not possible,
     * and there is no administrator that can override the normal operation of this smart
     * contract instance. Note that if it is created with no admin keys, then there is no
     * administrator to authorize changing the admin keys, so there can never be any admin keys
     * for that instance.
     */
    @Nullable
    public final Key adminKey;

    /**
     * The current time at which this contract instance (and its account) is set to expire.
     */
    public final Instant expirationTime;

    /**
     * The expiration time will extend every this many seconds. If there are insufficient funds,
     * then it extends as long as possible. If the account is empty when it expires,
     * then it is deleted.
     */
    public final Duration autoRenewPeriod;

    /**
     * Number of bytes of storage being used by this instance (which affects the cost to
     * extend the expiration time).
     */
    public final long storage;

    /**
     * The memo associated with the contract (max 100 bytes).
     */
    public final String contractMemo;

    /**
     * The current balance of the contract.
     */
    public final Hbar balance;

    /**
     * Whether the contract has been deleted
     */
    public final boolean isDeleted;

    /**
     * The tokens associated to the contract
     */
    public final Map<TokenId, TokenRelationship> tokenRelationships;

    private ContractInfo(
        ContractId contractId,
        AccountId accountId,
        String contractAccountId,
        @Nullable Key adminKey,
        Instant expirationTime,
        Duration autoRenewPeriod,
        long storage,
        String contractMemo,
        Hbar balance,
        boolean isDeleted,
        Map<TokenId, TokenRelationship> tokenRelationships
    ) {
        this.contractId = contractId;
        this.accountId = accountId;
        this.contractAccountId = contractAccountId;
        this.adminKey = adminKey;
        this.expirationTime = expirationTime;
        this.autoRenewPeriod = autoRenewPeriod;
        this.storage = storage;
        this.contractMemo = contractMemo;
        this.balance = balance;
        this.isDeleted = isDeleted;
        this.tokenRelationships = tokenRelationships;
    }

    static ContractInfo fromProtobuf(ContractGetInfoResponse.ContractInfo contractInfo) {
        return ContractInfo.fromProtobuf(contractInfo, null);
    }

    static ContractInfo fromProtobuf(ContractGetInfoResponse.ContractInfo contractInfo, @Nullable NetworkName networkName) {
        var adminKey = contractInfo.hasAdminKey()
            ? Key.fromProtobufKey(contractInfo.getAdminKey(), networkName)
            : null;

        var tokenRelationships = new HashMap<TokenId, TokenRelationship>(contractInfo.getTokenRelationshipsCount());

        for (var relationship : contractInfo.getTokenRelationshipsList()) {
            tokenRelationships.put(
                TokenId.fromProtobuf(relationship.getTokenId(), networkName),
                TokenRelationship.fromProtobuf(relationship, networkName)
            );
        }

        return new ContractInfo(
            ContractId.fromProtobuf(contractInfo.getContractID(), networkName),
            AccountId.fromProtobuf(contractInfo.getAccountID(), networkName),
            contractInfo.getContractAccountID(),
            adminKey,
            InstantConverter.fromProtobuf(contractInfo.getExpirationTime()),
            DurationConverter.fromProtobuf(contractInfo.getAutoRenewPeriod()),
            contractInfo.getStorage(),
            contractInfo.getMemo(),
            Hbar.fromTinybars(contractInfo.getBalance()),
            contractInfo.getDeleted(),
            tokenRelationships
        );
    }

    public static ContractInfo fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(ContractGetInfoResponse.ContractInfo.parseFrom(bytes).toBuilder().build());
    }

    ContractGetInfoResponse.ContractInfo toProtobuf() {
        var contractInfoBuilder = ContractGetInfoResponse.ContractInfo.newBuilder()
            .setContractID(contractId.toProtobuf())
            .setAccountID(accountId.toProtobuf())
            .setContractAccountID(contractAccountId)
            .setExpirationTime(InstantConverter.toProtobuf(expirationTime))
            .setAutoRenewPeriod(DurationConverter.toProtobuf(autoRenewPeriod))
            .setStorage(storage)
            .setMemo(contractMemo)
            .setBalance(balance.toTinybars());

        if (adminKey != null) {
            contractInfoBuilder.setAdminKey(adminKey.toProtobufKey());
        }

        return contractInfoBuilder.build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("contractId", contractId)
            .add("accountId", accountId)
            .add("contractAccountId", contractAccountId)
            .add("adminKey", adminKey)
            .add("expirationTime", expirationTime)
            .add("autoRenewPeriod", autoRenewPeriod)
            .add("storage", storage)
            .add("contractMemo", contractMemo)
            .add("balance", balance)
            .add("isDeleted", isDeleted)
            .add("tokenRelationships", tokenRelationships)
            .toString();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
