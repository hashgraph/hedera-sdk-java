package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.hedera.hashgraph.sdk.proto.FixedFee;

import javax.annotation.Nullable;

/**
 * Custom fixed fee utility class.
 * {@link https://docs.hedera.com/guides/docs/sdks/tokens/custom-token-fees#fixed-fee}
 */
public class CustomFixedFee extends CustomFee {
    private long amount = 0;
    /**
     * The shard, realm, number of the tokens.
     */
    @Nullable
    private TokenId denominatingTokenId = null;

    /**
     * Constructor.
     */
    public CustomFixedFee() {
    }

    /**
     * Clone the custom fixed fee object.
     *
     * @param source                    the source fee object
     * @return                          the new custom fee object
     */
    static CustomFixedFee clonedFrom(CustomFixedFee source) {
        var returnFee = new CustomFixedFee();
        returnFee.amount = source.amount;
        returnFee.denominatingTokenId = source.denominatingTokenId;
        returnFee.feeCollectorAccountId = source.feeCollectorAccountId;
        return returnFee;
    }

    /**
     * Create a custom fixed fee from a fixed fee protobuf.
     *
     * @param fixedFee                  the fixed fee protobuf
     * @return                          the new custom fixed fee object
     */
    static CustomFixedFee fromProtobuf(FixedFee fixedFee) {
        var returnFee = new CustomFixedFee()
            .setAmount(fixedFee.getAmount());
        if (fixedFee.hasDenominatingTokenId()) {
            returnFee.setDenominatingTokenId(TokenId.fromProtobuf(fixedFee.getDenominatingTokenId()));
        }
        return returnFee;
    }

    /**
     * Create a custom fixed fee from a custom fee protobuf.
     *
     * @param customFee                 the custom fee protobuf
     * @return                          the new custom fixed fee object
     */
    static CustomFixedFee fromProtobuf(com.hedera.hashgraph.sdk.proto.CustomFee customFee) {
        var returnFee = fromProtobuf(customFee.getFixedFee());
        if (customFee.hasFeeCollectorAccountId()) {
            returnFee.setFeeCollectorAccountId(AccountId.fromProtobuf(customFee.getFeeCollectorAccountId()));
        }
        return returnFee;
    }

    /**
     * Assign the fee collector account id.
     *
     * @param feeCollectorAccountId     the account id of the fee collector
     * @return {@code this}
     */
    public CustomFixedFee setFeeCollectorAccountId(AccountId feeCollectorAccountId) {
        doSetFeeCollectorAccountId(feeCollectorAccountId);
        return this;
    }

    /**
     * @return                          the amount of the fee in tiny bar
     */
    public long getAmount() {
        return amount;
    }

    /**
     * Assign the fee amount in tiny bar.
     *
     * @param amount                    the amount of the fee in tiny bar
     * @return {@code this}
     */
    public CustomFixedFee setAmount(long amount) {
        this.amount = amount;
        return this;
    }

    /**
     * @return                          the fee amount in hbar
     */
    public Hbar getHbarAmount() {
        return Hbar.fromTinybars(amount);
    }

    /**
     * Assign the fee amount in hbar.
     *
     * @param amount                    the fee amount in hbar
     * @return {@code this}
     */
    public CustomFixedFee setHbarAmount(Hbar amount) {
        denominatingTokenId = null;
        this.amount = amount.toTinybars();
        return this;
    }

    /**
     * @return                          the token id object
     */
    @Nullable
    public TokenId getDenominatingTokenId() {
        return denominatingTokenId;
    }

    /**
     * Assign the desired token id.
     *
     * @param tokenId                   the token id
     * @return {@code this}
     */
    public CustomFixedFee setDenominatingTokenId(@Nullable TokenId tokenId) {
        denominatingTokenId = tokenId;
        return this;
    }

    /**
     * Assign the default token 0.0.0.
     *
     * @return {@code this}
     */
    public CustomFixedFee setDenominatingTokenToSameToken() {
        denominatingTokenId = new TokenId(0, 0, 0);
        return this;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        super.validateChecksums(client);
        if (denominatingTokenId != null) {
            denominatingTokenId.validateChecksum(client);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("feeCollectorAccountId", getFeeCollectorAccountId())
            .add("amount", getAmount())
            .add("demoninatingTokenId", getDenominatingTokenId())
            .toString();
    }

    /**
     * Convert to a protobuf.
     *
     * @return                          the protobuf converted object
     */
    FixedFee toFixedFeeProtobuf() {
        var fixedFeeBuilder = FixedFee.newBuilder()
            .setAmount(getAmount());
        if (getDenominatingTokenId() != null) {
            fixedFeeBuilder.setDenominatingTokenId(getDenominatingTokenId().toProtobuf());
        }
        return fixedFeeBuilder.build();
    }

    @Override
    com.hedera.hashgraph.sdk.proto.CustomFee toProtobuf() {
        var customFeeBuilder = com.hedera.hashgraph.sdk.proto.CustomFee.newBuilder()
            .setFixedFee(toFixedFeeProtobuf());
        return finishToProtobuf(customFeeBuilder);
    }
}
