package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Associates the provided Hedera account with the provided Hedera token(s).
 * Hedera accounts must be associated with a fungible or non-fungible token
 * first before you can transfer tokens to that account. In the case of
 * NON_FUNGIBLE Type, once an account is associated, it can hold any number
 * of NFTs (serial numbers) of that token type. The Hedera account that is
 * being associated with a token is required to sign the transaction.
 *
 * {@link https://docs.hedera.com/guides/docs/sdks/tokens/associate-tokens-to-an-account}
 */
public class TokenAssociation {
    public final TokenId tokenId;
    public final AccountId accountId;

    /**
     * Constructor.
     *
     * @param tokenId                   the token id
     * @param accountId                 the account id
     */
    private TokenAssociation(TokenId tokenId, AccountId accountId) {
        this.tokenId = tokenId;
        this.accountId = accountId;
    }

    /**
     * Create a token association from a protobuf.
     *
     * @param tokenAssociation          the protobuf
     * @return                          the new token association
     */
    static TokenAssociation fromProtobuf(com.hedera.hashgraph.sdk.proto.TokenAssociation tokenAssociation) {
        return new TokenAssociation(
            tokenAssociation.hasTokenId() ? TokenId.fromProtobuf(tokenAssociation.getTokenId()) : new TokenId(0, 0, 0),
            tokenAssociation.hasAccountId() ? AccountId.fromProtobuf(tokenAssociation.getAccountId()) : new AccountId(0, 0, 0)
        );
    }

    /**
     * Create a token association from a byte array.
     *
     * @param bytes                     the byte array
     * @return                          the new token association
     * @throws InvalidProtocolBufferException
     */
    public static TokenAssociation fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.TokenAssociation.parseFrom(bytes));
    }

    /**
     * @return                          the protobuf representation
     */
    com.hedera.hashgraph.sdk.proto.TokenAssociation toProtobuf() {
        return com.hedera.hashgraph.sdk.proto.TokenAssociation.newBuilder()
            .setTokenId(tokenId.toProtobuf())
            .setAccountId(accountId.toProtobuf())
            .build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("tokenId", tokenId)
            .add("accountId", accountId)
            .toString();
    }

    /**
     * @return                          the byte array representation
     */
    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
