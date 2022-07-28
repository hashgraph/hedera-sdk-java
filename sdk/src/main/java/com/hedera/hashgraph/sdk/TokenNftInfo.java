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
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.TokenGetNftInfoResponse;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 *
 *
 * See <a href="https://docs.hedera.com/guides/docs/hedera-api/token-service/tokengetnftinfo#tokennftinfo">Hedera Documentation</a>
 */
public class TokenNftInfo {
    /**
     * The ID of the NFT
     */
    public final NftId nftId;

    /**
     * The current owner of the NFT
     */
    public final AccountId accountId;

    /**
     * The effective consensus timestamp at which the NFT was minted
     */
    public final Instant creationTime;

    /**
     * Represents the unique metadata of the NFT
     */
    public final byte[] metadata;

    /**
     * The ledger ID the response was returned from; please see <a href="https://github.com/hashgraph/hedera-improvement-proposal/blob/master/HIP/hip-198.md">HIP-198</a> for the network-specific IDs.
     */
    public final LedgerId ledgerId;

    /**
     * If an allowance is granted for the NFT, its corresponding spender account
     */
    @Nullable
    public final AccountId allowanceSpenderAccountId;

    /**
     * Constructor.
     *
     * @param nftId                     the id of the nft
     * @param accountId                 the current owner of the nft
     * @param creationTime              the effective consensus time
     * @param metadata                  the unique metadata
     * @param ledgerId                  the ledger id of the response
     * @param allowanceSpenderAccountId the spender of the allowance (null if not an allowance)
     */
    TokenNftInfo(
        NftId nftId,
        AccountId accountId,
        Instant creationTime,
        byte[] metadata,
        LedgerId ledgerId,
        @Nullable AccountId allowanceSpenderAccountId
    ) {
        this.nftId = nftId;
        this.accountId = accountId;
        this.creationTime = Objects.requireNonNull(creationTime);
        this.metadata = metadata;
        this.ledgerId = ledgerId;
        this.allowanceSpenderAccountId = allowanceSpenderAccountId;
    }

    /**
     * Create token nft info from a protobuf.
     *
     * @param info                      the protobuf
     * @return                          the new token nft info
     */
    static TokenNftInfo fromProtobuf(com.hedera.hashgraph.sdk.proto.TokenNftInfo info) {
        return new TokenNftInfo(
            NftId.fromProtobuf(info.getNftID()),
            AccountId.fromProtobuf(info.getAccountID()),
            InstantConverter.fromProtobuf(info.getCreationTime()),
            info.getMetadata().toByteArray(),
            LedgerId.fromByteString(info.getLedgerId()),
            info.hasSpenderId() ? AccountId.fromProtobuf(info.getSpenderId()) : null
        );
    }

    /**
     * Create token nft info from byte array.
     *
     * @param bytes                     the byte array
     * @return                          the new token nft info
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    public static TokenNftInfo fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.TokenNftInfo.parseFrom(bytes));
    }

    /**
     * Create the protobuf.
     *
     * @return                          the protobuf representation
     */
    com.hedera.hashgraph.sdk.proto.TokenNftInfo toProtobuf() {
        var builder = com.hedera.hashgraph.sdk.proto.TokenNftInfo.newBuilder()
            .setNftID(nftId.toProtobuf())
            .setAccountID(accountId.toProtobuf())
            .setCreationTime(InstantConverter.toProtobuf(creationTime))
            .setMetadata(ByteString.copyFrom(metadata))
            .setLedgerId(ledgerId.toByteString());
        if (allowanceSpenderAccountId != null) {
            builder.setSpenderId(allowanceSpenderAccountId.toProtobuf());
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("nftId", nftId)
            .add("accountId", accountId)
            .add("creationTime", creationTime)
            .add("metadata", metadata)
            .add("ledgerId", ledgerId)
            .add("allowanceSpenderAccountId", allowanceSpenderAccountId)
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
