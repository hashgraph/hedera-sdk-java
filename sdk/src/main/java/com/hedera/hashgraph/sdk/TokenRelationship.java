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

import com.google.common.base.MoreObjects;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.TokenFreezeStatus;
import com.hedera.hashgraph.sdk.proto.TokenKycStatus;

import javax.annotation.Nullable;

/**
 * Token's information related to the given Account.
 *
 * See <a href="https://docs.hedera.com/guides/docs/hedera-api/basic-types/tokenrelationship">Hedera Documentation</a>
 */
public class TokenRelationship {
    /**
     * A unique token id
     */
    public final TokenId tokenId;
    /**
     * The Symbol of the token
     */
    public final String symbol;
    /**
     * For token of type FUNGIBLE_COMMON - the balance that the Account holds
     * in the smallest denomination.
     *
     * For token of type NON_FUNGIBLE_UNIQUE - the number of NFTs held by the
     * account
     */
    public final long balance;
    /**
     * The KYC status of the account (KycNotApplicable, Granted or Revoked).
     *
     * If the token does not have KYC key, KycNotApplicable is returned
     */
    @Nullable
    public final Boolean kycStatus;
    /**
     * The Freeze status of the account (FreezeNotApplicable, Frozen or
     * Unfrozen). If the token does not have Freeze key,
     * FreezeNotApplicable is returned
     */
    @Nullable
    public final Boolean freezeStatus;
    /**
     * Specifies if the relationship is created implicitly.
     * False : explicitly associated,
     * True : implicitly associated.
     */
    public final boolean automaticAssociation;

    TokenRelationship(
        TokenId tokenId,
        String symbol,
        long balance,
        @Nullable Boolean kycStatus,
        @Nullable Boolean freezeStatus,
        boolean automaticAssociation
    ) {
        this.tokenId = tokenId;
        this.symbol = symbol;
        this.balance = balance;
        this.kycStatus = kycStatus;
        this.freezeStatus = freezeStatus;
        this.automaticAssociation = automaticAssociation;
    }

    /**
     * Retrieve freeze status from a protobuf.
     *
     * @param freezeStatus              the protobuf
     * @return                          the freeze status
     */
    @Nullable
    static Boolean freezeStatusFromProtobuf(TokenFreezeStatus freezeStatus) {
        return freezeStatus == TokenFreezeStatus.FreezeNotApplicable ? null : freezeStatus == TokenFreezeStatus.Frozen;
    }

    /**
     * Retrieve the kyc status from a protobuf.
     *
     * @param kycStatus                 the protobuf
     * @return                          the kyc status
     */
    @Nullable
    static Boolean kycStatusFromProtobuf(TokenKycStatus kycStatus) {
        return kycStatus == TokenKycStatus.KycNotApplicable ? null : kycStatus == TokenKycStatus.Granted;
    }

    /**
     * Create a token relationship object from a protobuf.
     *
     * @param tokenRelationship         the protobuf
     * @return                          the new token relationship
     */
    static TokenRelationship fromProtobuf(com.hedera.hashgraph.sdk.proto.TokenRelationship tokenRelationship) {
        return new TokenRelationship(
            TokenId.fromProtobuf(tokenRelationship.getTokenId()),
            tokenRelationship.getSymbol(),
            tokenRelationship.getBalance(),
            kycStatusFromProtobuf(tokenRelationship.getKycStatus()),
            freezeStatusFromProtobuf(tokenRelationship.getFreezeStatus()),
            tokenRelationship.getAutomaticAssociation()
        );
    }

    /**
     * Create a token relationship from a byte array.
     *
     * @param bytes                     the byte array
     * @return                          the new token relationship
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    public static TokenRelationship fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.TokenRelationship.parseFrom(bytes).toBuilder().build());
    }

    /**
     * Retrieve the freeze status from a protobuf.
     *
     * @param freezeStatus              the protobuf
     * @return                          the freeze status
     */
    static TokenFreezeStatus freezeStatusToProtobuf(@Nullable Boolean freezeStatus) {
        return freezeStatus == null ? TokenFreezeStatus.FreezeNotApplicable : freezeStatus ? TokenFreezeStatus.Frozen : TokenFreezeStatus.Unfrozen;
    }

    /**
     * Retrieve the kyc status from a protobuf.
     *
     * @param kycStatus                 the protobuf
     * @return                          the kyc status
     */
    static TokenKycStatus kycStatusToProtobuf(@Nullable Boolean kycStatus) {
        return kycStatus == null ? TokenKycStatus.KycNotApplicable : kycStatus ? TokenKycStatus.Granted : TokenKycStatus.Revoked;
    }

    /**
     * Create the protobuf.
     *
     * @return                          the protobuf representation
     */
    com.hedera.hashgraph.sdk.proto.TokenRelationship toProtobuf() {
        return com.hedera.hashgraph.sdk.proto.TokenRelationship.newBuilder()
            .setTokenId(tokenId.toProtobuf())
            .setSymbol(symbol)
            .setBalance(balance)
            .setKycStatus(kycStatusToProtobuf(kycStatus))
            .setFreezeStatus(freezeStatusToProtobuf(freezeStatus))
            .setAutomaticAssociation(automaticAssociation)
            .build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("tokenId", tokenId)
            .add("symbol", symbol)
            .add("balance", balance)
            .add("kycStatus", kycStatus)
            .add("freezeStatus", freezeStatus)
            .add("automaticAssociation", automaticAssociation)
            .toString();
    }

    /**
     * Create the byte array.
     *
     * @return                          the byte array representation
     */
    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
