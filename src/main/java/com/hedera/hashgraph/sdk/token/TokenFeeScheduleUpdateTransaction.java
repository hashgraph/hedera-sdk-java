package com.hedera.hashgraph.sdk.token;

import com.google.common.annotations.Beta;
import com.hedera.hashgraph.proto.TokenFeeScheduleUpdateTransactionBody;
import com.hedera.hashgraph.proto.TokenServiceGrpc;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.*;

import java.util.List;

import io.grpc.MethodDescriptor;

@Beta
public final class TokenFeeScheduleUpdateTransaction extends SingleTransactionBuilder<TokenFeeScheduleUpdateTransaction> {
    private final TokenFeeScheduleUpdateTransactionBody.Builder builder = bodyBuilder.getTokenFeeScheduleUpdateBuilder();

    public TokenFeeScheduleUpdateTransaction() {
        super();
    }

    public TokenFeeScheduleUpdateTransaction setTokenId(TokenId tokenId) {
        builder.setTokenId(tokenId.toProto());
        return this;
    }

    public TokenFeeScheduleUpdateTransaction setCustomFees(List<CustomFee> fees) {
        for (CustomFee fee : fees) {
            this.builder.addCustomFees(fee.toProto());
        }
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return TokenServiceGrpc.getUpdateTokenFeeScheduleMethod();
    }

    @Override
    protected void doValidate() {
    }
}
