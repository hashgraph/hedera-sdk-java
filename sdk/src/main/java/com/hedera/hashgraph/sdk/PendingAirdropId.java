/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2024 Hedera Hashgraph, LLC
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
import javax.annotation.Nullable;

/**
 * PendingAirdropId can contain only one of tokenId or nftId
 */
public class PendingAirdropId {
    private final AccountId sender;
    private final AccountId receiver;
    @Nullable
    private final TokenId tokenId;
    @Nullable
    private final NftId nftId;

    PendingAirdropId(AccountId sender, AccountId receiver, TokenId tokenId) {
        this.sender = sender;
        this.receiver = receiver;
        this.tokenId = tokenId;
        this.nftId = null;
    }

    PendingAirdropId(AccountId sender, AccountId receiver, NftId nftId) {
        this.sender = sender;
        this.receiver = receiver;
        this.nftId = nftId;
        this.tokenId = null;
    }

    public AccountId getSender() {
        return sender;
    }

    public AccountId getReceiver() {
        return receiver;
    }

    public TokenId getTokenId() {
        return tokenId;
    }

    public NftId getNftId() {
        return nftId;
    }

    static PendingAirdropId fromProtobuf(com.hedera.hashgraph.sdk.proto.PendingAirdropId pendingAirdropId) {
        if (pendingAirdropId.hasFungibleTokenType()) {
            return new PendingAirdropId(AccountId.fromProtobuf(pendingAirdropId.getSenderId()),
                AccountId.fromProtobuf(pendingAirdropId.getReceiverId()),
                TokenId.fromProtobuf(pendingAirdropId.getFungibleTokenType()));
        } else {
            return new PendingAirdropId(AccountId.fromProtobuf(pendingAirdropId.getSenderId()),
                AccountId.fromProtobuf(pendingAirdropId.getReceiverId()),
                NftId.fromProtobuf(pendingAirdropId.getNonFungibleToken()));
        }
    }

    com.hedera.hashgraph.sdk.proto.PendingAirdropId toProtobuf() {
        var builder = com.hedera.hashgraph.sdk.proto.PendingAirdropId.newBuilder()
            .setSenderId(sender.toProtobuf())
            .setReceiverId(receiver.toProtobuf());

        if (tokenId != null) {
            builder.setFungibleTokenType(tokenId.toProtobuf());
        } else if (nftId != null) {
            builder.setNonFungibleToken(nftId.toProtobuf());
        }
        return builder.build();
     }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("sender",  sender)
            .add("receiver",  receiver)
            .add("tokenId",  tokenId)
            .add("nftId",  nftId)
            .toString();
    }
}
