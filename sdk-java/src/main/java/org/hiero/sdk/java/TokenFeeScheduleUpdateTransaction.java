// SPDX-License-Identifier: Apache-2.0
package org.hiero.sdk.java;

import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.MethodDescriptor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import org.hiero.sdk.java.proto.SchedulableTransactionBody;
import org.hiero.sdk.java.proto.TokenFeeScheduleUpdateTransactionBody;
import org.hiero.sdk.java.proto.TokenServiceGrpc;
import org.hiero.sdk.java.proto.TransactionBody;
import org.hiero.sdk.java.proto.TransactionResponse;

/**
 * Update the custom fees for a given token. If the token does not have a
 * fee schedule, the network response returned will be
 * CUSTOM_SCHEDULE_ALREADY_HAS_NO_FEES. You will need to sign the transaction
 * with the fee schedule key to update the fee schedule for the token. If you
 * do not have a fee schedule key set for the token, you will not be able to
 * update the fee schedule.
 *
 * See <a href="https://docs.hedera.com/guides/docs/sdks/tokens/update-a-fee-schedule">Hedera Documentation</a>
 */
public class TokenFeeScheduleUpdateTransaction extends Transaction<TokenFeeScheduleUpdateTransaction> {
    @Nullable
    private TokenId tokenId = null;

    private List<CustomFee> customFees = new ArrayList<>();

    /**
     * Constructor.
     */
    public TokenFeeScheduleUpdateTransaction() {}

    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction)
     *            records
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    TokenFeeScheduleUpdateTransaction(
            LinkedHashMap<TransactionId, LinkedHashMap<AccountId, org.hiero.sdk.java.proto.Transaction>> txs)
            throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * Constructor.
     *
     * @param txBody protobuf TransactionBody
     */
    TokenFeeScheduleUpdateTransaction(org.hiero.sdk.java.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    /**
     * Extract the token id.
     *
     * @return                          the token id
     */
    @Nullable
    public TokenId getTokenId() {
        return tokenId;
    }

    /**
     * Assign the token id.
     *
     * @param tokenId                   the token id
     * @return {@code this}
     */
    public TokenFeeScheduleUpdateTransaction setTokenId(TokenId tokenId) {
        Objects.requireNonNull(tokenId);
        requireNotFrozen();
        this.tokenId = tokenId;
        return this;
    }

    /**
     * Extract the list of custom fees.
     *
     * @return                          the list of custom fees
     */
    public List<CustomFee> getCustomFees() {
        return CustomFee.deepCloneList(customFees);
    }

    /**
     * Assign the list of custom fees.
     *
     * @param customFees               the list of custom fees
     * @return {@code this}
     */
    public TokenFeeScheduleUpdateTransaction setCustomFees(List<CustomFee> customFees) {
        Objects.requireNonNull(customFees);
        requireNotFrozen();
        this.customFees = CustomFee.deepCloneList(customFees);
        return this;
    }

    /**
     * Initialize from the transaction body.
     */
    void initFromTransactionBody() {
        var body = sourceTransactionBody.getTokenFeeScheduleUpdate();
        if (body.hasTokenId()) {
            tokenId = TokenId.fromProtobuf(body.getTokenId());
        }

        for (var fee : body.getCustomFeesList()) {
            customFees.add(CustomFee.fromProtobuf(fee));
        }
    }

    /**
     * Build the transaction body.
     *
     * @return {@link
     *         org.hiero.sdk.java.proto.TokenFeeScheduleUpdateTransactionBody}
     */
    TokenFeeScheduleUpdateTransactionBody.Builder build() {
        var builder = TokenFeeScheduleUpdateTransactionBody.newBuilder();
        if (tokenId != null) {
            builder.setTokenId(tokenId.toProtobuf());
        }

        builder.clearCustomFees();
        for (var fee : customFees) {
            builder.addCustomFees(fee.toProtobuf());
        }

        return builder;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (tokenId != null) {
            tokenId.validateChecksum(client);
        }

        for (CustomFee fee : customFees) {
            fee.validateChecksums(client);
        }
    }

    @Override
    MethodDescriptor<org.hiero.sdk.java.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getUpdateTokenFeeScheduleMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenFeeScheduleUpdate(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        throw new UnsupportedOperationException("Cannot schedule TokenFeeScheduleUpdateTransaction");
    }
}
