package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import java.util.LinkedHashMap;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;

public class TokenCustomFeeUpdateTransaction extends Transaction<TokenCustomFeeUpdateTransaction> {
    private final TokenFeeScheduleUpdateTransactionBody.Builder builder;

    @Nullable
    TokenId tokenId = null;
    List<CustomFee> customFees = new ArrayList<>();

    public TokenCustomFeeUpdateTransaction() {
        builder = TokenFeeScheduleUpdateTransactionBody.newBuilder();
    }

    TokenCustomFeeUpdateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getTokenFeeScheduleUpdate().toBuilder();

        if (builder.hasTokenId()) {
            tokenId = TokenId.fromProtobuf(builder.getTokenId());
        }

        for(var fee : builder.getCustomFeesList()) {
            customFees.add(CustomFee.fromProtobuf(fee));
        }
    }

    TokenCustomFeeUpdateTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);

        builder = bodyBuilder.getTokenFeeScheduleUpdate().toBuilder();

        if (builder.hasTokenId()) {
            tokenId = TokenId.fromProtobuf(builder.getTokenId());
        }

        for(var fee : builder.getCustomFeesList()) {
            customFees.add(CustomFee.fromProtobuf(fee));
        }
    }

    @Nullable
    public TokenId getTokenId() {
        return tokenId;
    }

    public TokenCustomFeeUpdateTransaction setTokenId(TokenId tokenId) {
        Objects.requireNonNull(tokenId);
        requireNotFrozen();
        this.tokenId = tokenId;
        return this;
    }

    public List<CustomFee> getCustomFees() {
        return CustomFee.deepCloneList(customFees);
    }

    public TokenCustomFeeUpdateTransaction setCustomFees(List<CustomFee> customFees) {
        Objects.requireNonNull(customFees);
        requireNotFrozen();
        this.customFees = customFees;
        return this;
    }

    public TokenCustomFeeUpdateTransaction addCustomFee(CustomFee customFee) {
        Objects.requireNonNull(customFee);
        requireNotFrozen();
        customFees.add(customFee);
        return this;
    }

    TokenFeeScheduleUpdateTransactionBody.Builder build() {
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
    void validateNetworkOnIds(Client client) {
        if (tokenId != null) {
            tokenId.validate(client);
        }

        for(var fee : customFees) {
            fee.validate(client);
        }
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getUpdateTokenFeeScheduleMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenFeeScheduleUpdate(build());
        return true;
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        throw new IllegalStateException("TokenCustomFeeUpdateTransaction cannot be scheduled");
    }
}
