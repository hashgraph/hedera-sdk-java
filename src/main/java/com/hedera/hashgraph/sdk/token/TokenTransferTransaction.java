package com.hedera.hashgraph.sdk.token;

import com.hedera.hashgraph.proto.*;
import com.hedera.hashgraph.sdk.SingleTransactionBuilder;

import com.hedera.hashgraph.sdk.account.AccountId;
import io.grpc.MethodDescriptor;

import java.util.HashMap;

import javax.annotation.Nonnegative;

/**
 * Transfer tokens from some accounts to other accounts. Each negative amount is withdrawn from the corresponding
 * account (a sender), and each positive one is added to the corresponding account (a receiver). All amounts must
 * have sum of zero.
 * Each amount is a number with the lowest denomination possible for a token. Example:
 * Token X has 2 decimals. Account A transfers amount of 100 tokens by providing 10000 as amount in the TransferList.
 * If Account A wants to send 100.55 tokens, he must provide 10055 as amount.
 *
 * If any sender account fails to have sufficient token balance, then the entire transaction fails and none of the
 * transfers occur, though transaction fee is still charged.
*/
public final class TokenTransferTransaction extends SingleTransactionBuilder<TokenTransferTransaction> {
    private final TokenTransfersTransactionBody.Builder builder = bodyBuilder.getTokenTransfersBuilder();
    private HashMap<TokenId, Integer> tokenIndexes = new HashMap<>();

    public TokenTransferTransaction() {
        super();
    }

    public TokenTransferTransaction addSender(TokenId tokenId, AccountId accountId, @Nonnegative long amount) {
        return addTransfer(tokenId, accountId, -amount);
    }

    public TokenTransferTransaction addRecipient(TokenId tokenId, AccountId accountId, @Nonnegative long amount) {
        return addTransfer(tokenId, accountId, amount);
    }

    public TokenTransferTransaction addTransfer(TokenId tokenId, AccountId accountId, long amount) {
        Integer index = tokenIndexes.get(tokenId);
        int size = builder.getTokenTransfersCount();

        TokenTransferList.Builder transfers;

        if (index != null) {
            transfers = builder.getTokenTransfers(index).toBuilder();
        } else {
            transfers = TokenTransferList.newBuilder();
            builder.addTokenTransfers(transfers);
            tokenIndexes.put(tokenId, size);
        }

        transfers.addTransfers(AccountAmount.newBuilder()
            .setAccountID(accountId.toProto())
            .setAmount(amount)
        );

        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return TokenServiceGrpc.getTransferTokensMethod();
    }

    @Override
    protected void doValidate() {
    }
}
