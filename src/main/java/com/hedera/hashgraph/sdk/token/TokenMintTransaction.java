package com.hedera.hashgraph.sdk.token;

import com.google.common.annotations.Beta;
import com.google.protobuf.ByteString;
import com.hedera.hashgraph.proto.*;
import com.hedera.hashgraph.sdk.SingleTransactionBuilder;

import io.grpc.MethodDescriptor;
import org.bouncycastle.util.encoders.Hex;

import java.util.List;

/**
 * Mints tokens to the Token's treasury Account. If no Supply Key is defined, the transaction will resolve to
 * TOKEN_HAS_NO_SUPPLY_KEY. The operation increases the Total Supply of the Token. The maximum total supply a token
 * can have is 2^63-1. The amount provided must be in the lowest denomination possible. Example:
 * Token A has 2 decimals. In order to mint 100 tokens, one must provide amount of 10000. In order to mint 100.55
 * tokens, one must provide amount of 10055.
 */
public final class TokenMintTransaction extends SingleTransactionBuilder<TokenMintTransaction> {
    private final TokenMintTransactionBody.Builder builder = bodyBuilder.getTokenMintBuilder();

    public TokenMintTransaction() {
        super();
    }

    /**
     * The token for which to mint tokens. If token does not exist, transaction results in INVALID_TOKEN_ID
     *
     * @param token
     * @return TokenMintTransaction
     */
    public TokenMintTransaction setTokenId(TokenId token) {
        builder.setToken(token.toProto());
        return this;
    }

    /**
     * The amount to mint to the Treasury Account. Amount must be a positive non-zero number represented in the lowest
     * denomination of the token. The new supply must be lower than 2^63.
     *
     * @param amount
     * @return TokenMintTransaction
     */
    public TokenMintTransaction setAmount(long amount) {
        builder.setAmount(amount);
        return this;
    }

    @Beta
    public TokenMintTransaction addMetadata(String metadata) {
        builder.addMetadata(ByteString.copyFrom(Hex.decode(metadata)));
        return this;
    }

    @Beta
    public TokenMintTransaction addMetadata(byte[] metadata) {
        builder.addMetadata(ByteString.copyFrom(metadata));
        return this;
    }

    @Beta
    public TokenMintTransaction addMetadata(List<byte[]> metadatas) {
        for(byte[] metadata : metadatas) {
            builder.addMetadata(ByteString.copyFrom(metadata));
        }
        return this;
    }


    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return TokenServiceGrpc.getMintTokenMethod();
    }

    @Override
    protected void doValidate() {
    }
}
