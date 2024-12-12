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
package com.hiero.sdk;

import com.google.protobuf.UInt32Value;
import com.hiero.sdk.proto.AccountAmount;
import com.hiero.sdk.proto.NftTransfer;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

class TokenTransferList {
    final TokenId tokenId;
    @Nullable
    final Integer expectDecimals;
    List<TokenTransfer> transfers = new ArrayList<>();
    List<TokenNftTransfer> nftTransfers = new ArrayList<>();

    TokenTransferList(TokenId tokenId, @Nullable Integer expectDecimals, @Nullable TokenTransfer transfer,
        @Nullable TokenNftTransfer nftTransfer) {
        this.tokenId = tokenId;
        this.expectDecimals = expectDecimals;

        if (transfer != null) {
            this.transfers.add(transfer);
        }

        if (nftTransfer != null) {
            this.nftTransfers.add(nftTransfer);
        }
    }

    com.hiero.sdk.proto.TokenTransferList toProtobuf() {
        var transfers = new ArrayList<AccountAmount>();
        var nftTransfers = new ArrayList<NftTransfer>();

        for (var transfer : this.transfers) {
            transfers.add(transfer.toProtobuf());
        }

        for (var transfer : this.nftTransfers) {
            nftTransfers.add(transfer.toProtobuf());
        }

        var builder = com.hiero.sdk.proto.TokenTransferList.newBuilder()
            .setToken(tokenId.toProtobuf())
            .addAllTransfers(transfers)
            .addAllNftTransfers(nftTransfers);

        if (expectDecimals != null) {
            builder.setExpectedDecimals(UInt32Value.newBuilder().setValue(expectDecimals).build());
        }

        return builder.build();
    }
}
