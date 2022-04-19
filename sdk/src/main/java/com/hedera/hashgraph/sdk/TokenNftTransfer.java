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
import com.hedera.hashgraph.sdk.proto.NftTransfer;
import com.hedera.hashgraph.sdk.proto.TokenID;
import com.hedera.hashgraph.sdk.proto.TokenTransferList;
import java8.util.Lists;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TokenNftTransfer implements Comparable<TokenNftTransfer> {
    public final TokenId tokenId;
    public final AccountId sender;
    public final AccountId receiver;
    public final long serial;
    public boolean isApproved;

    TokenNftTransfer(TokenId tokenId, AccountId sender, AccountId receiver, long serial, boolean isApproved) {
        this.tokenId = tokenId;
        this.sender = sender;
        this.receiver = receiver;
        this.serial = serial;
        this.isApproved = isApproved;
    }

    static ArrayList<TokenNftTransfer> fromProtobuf(List<TokenTransferList> tokenTransferList) {
        var transfers = new ArrayList<TokenNftTransfer>();

        for (var tokenTransfer : tokenTransferList) {
            var tokenId = TokenId.fromProtobuf(tokenTransfer.getToken());

            for (var transfer : tokenTransfer.getNftTransfersList()) {
                transfers.add(new TokenNftTransfer(
                    tokenId,
                    AccountId.fromProtobuf(transfer.getSenderAccountID()),
                    AccountId.fromProtobuf(transfer.getReceiverAccountID()),
                    transfer.getSerialNumber(),
                    transfer.getIsApproval()
                ));
            }
        }

        return transfers;
    }

    @Deprecated
    public static TokenNftTransfer fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(
            Lists.of(
                TokenTransferList.newBuilder()
                    .setToken(TokenID.newBuilder().build())
                    .addNftTransfers(NftTransfer.parseFrom(bytes))
                    .build()
            )
        ).get(0);
    }

    NftTransfer toProtobuf() {
        return NftTransfer.newBuilder()
            .setSenderAccountID(sender.toProtobuf())
            .setReceiverAccountID(receiver.toProtobuf())
            .setSerialNumber(serial)
            .setIsApproval(isApproved)
            .build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("tokenId", tokenId)
            .add("sender", sender)
            .add("receiver", receiver)
            .add("serial", serial)
            .add("isApproved", isApproved)
            .toString();
    }

    @Deprecated
    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }

    @Override
    public int compareTo(TokenNftTransfer o) {
        int senderComparison = sender.compareTo(o.sender);
        if (senderComparison != 0) {
            return senderComparison;
        }
        int receiverComparison = receiver.compareTo(o.receiver);
        if (receiverComparison != 0) {
            return receiverComparison;
        }
        return Long.compare(serial, o.serial);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof TokenNftTransfer)) {
            return false;
        }

        TokenNftTransfer otherTransfer = (TokenNftTransfer) o;
        return sender.equals(otherTransfer.sender) && receiver.equals(otherTransfer.receiver) && serial == otherTransfer.serial;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender.hashCode(), receiver.hashCode(), serial);
    }
}
