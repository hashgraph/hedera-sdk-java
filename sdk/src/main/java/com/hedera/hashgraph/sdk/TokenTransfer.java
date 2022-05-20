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
import com.hedera.hashgraph.sdk.proto.AccountAmount;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * A token transfer record.
 *
 * Internal utility class.
 */
public class TokenTransfer {
    final TokenId tokenId;
    final AccountId accountId;

    @Nullable
    Integer expectedDecimals;
    long amount;

    boolean isApproved;

    /**
     * Constructor.
     *
     * @param tokenId                   the token id
     * @param accountId                 the account id
     * @param amount                    the amount
     * @param isApproved                is it approved
     */
    TokenTransfer(TokenId tokenId, AccountId accountId, long amount, boolean isApproved) {
        this(tokenId, accountId, amount, null, isApproved);
    }

    /**
     * Constructor.
     *
     * @param tokenId                   the token id
     * @param accountId                 the account id
     * @param amount                    the amount
     * @param expectedDecimals          the expected decimals
     * @param isApproved                is it approved
     */
    TokenTransfer(TokenId tokenId, AccountId accountId, long amount, @Nullable Integer expectedDecimals, boolean isApproved) {
        this.tokenId = tokenId;
        this.accountId = accountId;
        this.amount = amount;
        this.expectedDecimals = expectedDecimals;
        this.isApproved = isApproved;
    }

    /**
     * Create a list of token transfer records from a protobuf.
     *
     * @param tokenTransferLists        the protobuf
     * @return                          the list of token transfer records
     */
    static List<TokenTransfer> fromProtobuf(List<com.hedera.hashgraph.sdk.proto.TokenTransferList> tokenTransferLists) {
        var transfers = new ArrayList<TokenTransfer>();

        for (var tokenTransferList : tokenTransferLists) {
            var tokenId = TokenId.fromProtobuf(tokenTransferList.getToken());

            for (var transfer : tokenTransferList.getTransfersList()) {
                transfers.add(new TokenTransfer(
                    tokenId,
                    AccountId.fromProtobuf(transfer.getAccountID()),
                    transfer.getAmount(),
                    tokenTransferList.hasExpectedDecimals() ? tokenTransferList.getExpectedDecimals().getValue() : null,
                    transfer.getIsApproval()
                ));
            }
        }

        return transfers;
    }

    /**
     * @return                          an account amount protobuf
     */
    AccountAmount toProtobuf() {
        return AccountAmount.newBuilder()
            .setAccountID(accountId.toProtobuf())
            .setAmount(amount)
            .setIsApproval(isApproved)
            .build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("tokenId", tokenId)
            .add("accountId", accountId)
            .add("amount", amount)
            .add("expectedDecimals", expectedDecimals)
            .add("isApproved", isApproved)
            .toString();
    }
}
