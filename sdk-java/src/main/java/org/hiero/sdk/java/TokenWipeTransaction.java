// SPDX-License-Identifier: Apache-2.0
package org.hiero.sdk.java;

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
import org.hiero.sdk.java.proto.TokenWipeAccountTransactionBody;
import org.hiero.sdk.java.proto.TransactionBody;
import org.hiero.sdk.java.proto.TransactionResponse;

/**
 * Wipes the provided amount of fungible or non-fungible tokens from the
 * specified Hedera account. This transaction does not delete tokens from the
 * treasury account. This transaction must be signed by the token's Wipe Key.
 * Wiping an account's tokens burns the tokens and decreases the total supply.
 *
 * See <a href="https://docs.hedera.com/guides/docs/sdks/tokens/wipe-a-token">Hedera Documentation</a>
 */
public class TokenWipeTransaction extends org.hiero.sdk.java.Transaction<TokenWipeTransaction> {
    /**
     * The ID of the token to wipe from the account
     */
    @Nullable
    private TokenId tokenId = null;
    /**
     * Applicable to tokens of type NON_FUNGIBLE_UNIQUE.
     * The account ID to wipe the NFT from.
     */
    @Nullable
    private AccountId accountId = null;
    /**
     * Applicable to tokens of type  FUNGIBLE_COMMON.The amount of token
     * to wipe from the specified account. The amount must be a positive
     * non-zero number in the lowest denomination possible, not bigger
     * than the token balance of the account.
     */
    private long amount = 0;
    /**
     * Applicable to tokens of type NON_FUNGIBLE_UNIQUE.
     * The list of NFTs to wipe.
     */
    private List<Long> serials = new ArrayList<>();

    /**
     * Constructor.
     */
    public TokenWipeTransaction() {}

    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction)
     *            records
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    TokenWipeTransaction(
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
    TokenWipeTransaction(org.hiero.sdk.java.proto.TransactionBody txBody) {
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
    public TokenWipeTransaction setTokenId(TokenId tokenId) {
        Objects.requireNonNull(tokenId);
        requireNotFrozen();
        this.tokenId = tokenId;
        return this;
    }

    /**
     * Extract the account id.
     *
     * @return                          the account id
     */
    @Nullable
    public AccountId getAccountId() {
        return accountId;
    }

    /**
     * Assign the account id.
     *
     * @param accountId                 the account id
     * @return {@code this}
     */
    public TokenWipeTransaction setAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId);
        requireNotFrozen();
        this.accountId = accountId;
        return this;
    }

    /**
     * Extract the amount.
     *
     * @return                          the amount
     */
    public long getAmount() {
        return amount;
    }

    /**
     * Assign the amount.
     *
     * @param amount                    the amount
     * @return {@code this}
     */
    public TokenWipeTransaction setAmount(@Nonnegative long amount) {
        requireNotFrozen();
        this.amount = amount;
        return this;
    }

    /**
     * Extract the list of serial numbers.
     *
     * @return                          the list of serial numbers
     */
    public List<Long> getSerials() {
        return new ArrayList<>(serials);
    }

    /**
     * Assign the list of serial numbers.
     *
     * @param serials                   the list of serial numbers
     * @return {@code this}
     */
    public TokenWipeTransaction setSerials(List<Long> serials) {
        requireNotFrozen();
        Objects.requireNonNull(serials);
        this.serials = new ArrayList<>(serials);
        return this;
    }

    /**
     * Add a serial number to the list of serial numbers.
     *
     * @param serial                    the serial number to add
     * @return {@code this}
     */
    public TokenWipeTransaction addSerial(@Nonnegative long serial) {
        requireNotFrozen();
        serials.add(serial);
        return this;
    }

    /**
     * Initialize from the transaction body.
     */
    void initFromTransactionBody() {
        var body = sourceTransactionBody.getTokenWipe();
        if (body.hasToken()) {
            tokenId = TokenId.fromProtobuf(body.getToken());
        }

        if (body.hasAccount()) {
            accountId = AccountId.fromProtobuf(body.getAccount());
        }
        amount = body.getAmount();
        serials = body.getSerialNumbersList();
    }

    /**
     * Build the transaction body.
     *
     * @return {@link
     *         org.hiero.sdk.java.proto.TokenWipeAccountTransactionBody}
     */
    TokenWipeAccountTransactionBody.Builder build() {
        var builder = TokenWipeAccountTransactionBody.newBuilder();
        if (tokenId != null) {
            builder.setToken(tokenId.toProtobuf());
        }

        if (accountId != null) {
            builder.setAccount(accountId.toProtobuf());
        }
        builder.setAmount(amount);
        for (var serial : serials) {
            builder.addSerialNumbers(serial);
        }

        return builder;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (tokenId != null) {
            tokenId.validateChecksum(client);
        }

        if (accountId != null) {
            accountId.validateChecksum(client);
        }
    }

    @Override
    MethodDescriptor<org.hiero.sdk.java.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getWipeTokenAccountMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenWipe(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenWipe(build());
    }
}
