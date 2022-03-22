package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.hedera.hashgraph.sdk.proto.AccountAmount;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TokenTransfer {
    final TokenId tokenId;
    final AccountId accountId;

    @Nullable
    Integer expectedDecimals;
    long amount;

    boolean isApproved;

    TokenTransfer(TokenId tokenId, AccountId accountId, long amount, boolean isApproved) {
        this(tokenId, accountId, amount, null, isApproved);
    }

    TokenTransfer(TokenId tokenId, AccountId accountId, long amount, @Nullable Integer expectedDecimals, boolean isApproved) {
        this.tokenId = tokenId;
        this.accountId = accountId;
        this.amount = amount;
        this.expectedDecimals = expectedDecimals;
        this.isApproved = isApproved;
    }

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
