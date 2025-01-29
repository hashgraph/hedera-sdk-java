// SPDX-License-Identifier: Apache-2.0
package org.hiero.sdk.java;

import com.google.protobuf.ByteString;
import com.google.protobuf.BytesValue;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.MethodDescriptor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import org.hiero.sdk.java.proto.SchedulableTransactionBody;
import org.hiero.sdk.java.proto.TokenServiceGrpc;
import org.hiero.sdk.java.proto.TokenUpdateNftsTransactionBody;
import org.hiero.sdk.java.proto.TransactionBody;
import org.hiero.sdk.java.proto.TransactionResponse;

public class TokenUpdateNftsTransaction extends Transaction<TokenUpdateNftsTransaction> {

    /**
     * The token for which to update NFTs.
     */
    @Nullable
    private TokenId tokenId = null;

    /**
     * The list of serial numbers to be updated.
     */
    private List<Long> serials = new ArrayList<>();

    /**
     * The new metadata of the NFT(s)
     */
    private byte[] metadata = null;

    /**
     * Constructor.
     */
    public TokenUpdateNftsTransaction() {}

    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction) records
     * @throws InvalidProtocolBufferException when there is an issue with the protobuf
     */
    TokenUpdateNftsTransaction(
            LinkedHashMap<TransactionId, LinkedHashMap<AccountId, org.hiero.sdk.java.proto.Transaction>> txs)
            throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    TokenUpdateNftsTransaction(org.hiero.sdk.java.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    /**
     * Extract the token id.
     *
     * @return the token id
     */
    @Nullable
    public TokenId getTokenId() {
        return tokenId;
    }

    /**
     * Assign the token id.
     *
     * @param tokenId the token id
     * @return {@code this}
     */
    public TokenUpdateNftsTransaction setTokenId(@Nullable TokenId tokenId) {
        requireNotFrozen();
        Objects.requireNonNull(tokenId);
        this.tokenId = tokenId;
        return this;
    }

    /**
     * Extract the list of serial numbers.
     *
     * @return the list of serial numbers
     */
    public List<Long> getSerials() {
        return serials;
    }

    /**
     * Assign the list of serial numbers.
     *
     * @param serials the list of serial numbers
     * @return {@code this}
     */
    public TokenUpdateNftsTransaction setSerials(List<Long> serials) {
        requireNotFrozen();
        Objects.requireNonNull(serials);
        this.serials = new ArrayList<>(serials);
        return this;
    }

    /**
     * Add a serial number to the list of serial numbers.
     *
     * @param serial the serial number to add
     * @return {@code this}
     */
    public TokenUpdateNftsTransaction addSerial(@Nonnegative long serial) {
        requireNotFrozen();
        serials.add(serial);
        return this;
    }

    /**
     * Extract the metadata.
     *
     * @return the metadata
     */
    @Nullable
    public byte[] getMetadata() {
        return metadata;
    }

    /**
     * Assign the metadata.
     *
     * @param metadata the metadata
     * @return {@code this}
     */
    public TokenUpdateNftsTransaction setMetadata(byte[] metadata) {
        requireNotFrozen();
        this.metadata = metadata;
        return this;
    }

    /**
     * Initialize from the transaction body.
     */
    void initFromTransactionBody() {
        var body = sourceTransactionBody.getTokenUpdateNfts();
        if (body.hasToken()) {
            tokenId = TokenId.fromProtobuf(body.getToken());
        }
        serials = body.getSerialNumbersList();
        if (body.hasMetadata()) {
            metadata = body.getMetadata().getValue().toByteArray();
        }
    }

    /**
     * Build the transaction body.
     *
     * @return {@link org.hiero.sdk.java.proto.TokenUpdateNftsTransactionBody}
     */
    TokenUpdateNftsTransactionBody.Builder build() {
        var builder = TokenUpdateNftsTransactionBody.newBuilder();

        if (tokenId != null) {
            builder.setToken(tokenId.toProtobuf());
        }

        for (var serial : serials) {
            builder.addSerialNumbers(serial);
        }

        if (metadata != null) {
            builder.setMetadata(BytesValue.of(ByteString.copyFrom(metadata)));
        }

        return builder;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (tokenId != null) {
            tokenId.validateChecksum(client);
        }
    }

    @Override
    MethodDescriptor<org.hiero.sdk.java.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getUpdateNftsMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenUpdateNfts(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenUpdateNfts(build());
    }
}
