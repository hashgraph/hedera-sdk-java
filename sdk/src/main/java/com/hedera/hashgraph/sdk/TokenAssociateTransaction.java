package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.TokenAssociateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenID;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import io.grpc.MethodDescriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TokenAssociateTransaction extends Transaction<TokenAssociateTransaction> {
    private final TokenAssociateTransactionBody.Builder builder;

    public TokenAssociateTransaction() {
        builder = TokenAssociateTransactionBody.newBuilder();
    }

    TokenAssociateTransaction(HashMap<TransactionId, HashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs.values().iterator().next());

        builder = bodyBuilder.getTokenAssociate().toBuilder();
    }

    public AccountId getAccountId() {
        return AccountId.fromProtobuf(builder.getAccount());
    }

    public TokenAssociateTransaction setAccountId(AccountId accountId) {
        requireNotFrozen();
        builder.setAccount(accountId.toProtobuf());
        return this;
    }

    public List<TokenId> getTokenIds() {
        var list = new ArrayList<TokenId>(builder.getTokensCount());
        for (var token : builder.getTokensList()) {
            list.add(TokenId.fromProtobuf(token));
        }
        return list;
    }

    public TokenAssociateTransaction setTokenIds(TokenId... tokens) {
        requireNotFrozen();
        builder.clearTokens();

        for (TokenId token : tokens) {
            builder.addTokens(token.toProtobuf());
        }
        return this;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getAssociateTokensMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenAssociate(builder);
        return true;
    }
}
