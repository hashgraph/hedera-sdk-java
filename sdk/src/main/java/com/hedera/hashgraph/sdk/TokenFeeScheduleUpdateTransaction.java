package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenFeeScheduleUpdateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.util.LinkedHashMap;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;

public class TokenFeeScheduleUpdateTransaction extends Transaction<TokenFeeScheduleUpdateTransaction> {
    @Nullable
    private TokenId tokenId = null;
    private List<CustomFee> customFees = new ArrayList<>();

    public TokenFeeScheduleUpdateTransaction() {
    }

    TokenFeeScheduleUpdateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    TokenFeeScheduleUpdateTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    @Nullable
    public TokenId getTokenId() {
        return tokenId;
    }

    public TokenFeeScheduleUpdateTransaction setTokenId(TokenId tokenId) {
        Objects.requireNonNull(tokenId);
        requireNotFrozen();
        this.tokenId = tokenId;
        return this;
    }

    public List<CustomFee> getCustomFees() {
        return CustomFee.deepCloneList(customFees);
    }

    public TokenFeeScheduleUpdateTransaction setCustomFees(List<CustomFee> customFees) {
        Objects.requireNonNull(customFees);
        requireNotFrozen();
        this.customFees = CustomFee.deepCloneList(customFees);
        return this;
    }

    void initFromTransactionBody() {
        var body = txBody.getTokenFeeScheduleUpdate();
        if (body.hasTokenId()) {
            tokenId = TokenId.fromProtobuf(body.getTokenId());
        }

        for(var fee : body.getCustomFeesList()) {
            customFees.add(CustomFee.fromProtobuf(fee));
        }
    }

    TokenFeeScheduleUpdateTransactionBody.Builder build() {
        var builder = TokenFeeScheduleUpdateTransactionBody.newBuilder();
        if (tokenId != null) {
            builder.setTokenId(tokenId.toProtobuf());
        }

        builder.clearCustomFees();
        for(var fee : customFees) {
            builder.addCustomFees(fee.toProtobuf());
        }

        return builder;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (tokenId != null) {
            tokenId.validateChecksum(client);
        }

        for(CustomFee fee : customFees) {
            fee.validateChecksums(client);
        }
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getUpdateTokenFeeScheduleMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenFeeScheduleUpdate(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        throw new IllegalStateException("TokenFeeScheduleUpdateTransaction cannot be scheduled");
    }
}
