// SPDX-License-Identifier: Apache-2.0
package org.hiero.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.MethodDescriptor;
import java.util.LinkedHashMap;
import java.util.Objects;
import javax.annotation.Nullable;
import org.hiero.sdk.proto.SchedulableTransactionBody;
import org.hiero.sdk.proto.TokenRevokeKycTransactionBody;
import org.hiero.sdk.proto.TokenServiceGrpc;
import org.hiero.sdk.proto.TransactionBody;
import org.hiero.sdk.proto.TransactionResponse;

/**
 * Revokes the KYC flag to the Hedera account for the given Hedera token.
 * This transaction must be signed by the token's KYC Key. If this key is
 * not set, you can submit a TokenUpdateTransaction to provide the token
 * with this key.
 *
 * See <a href="https://docs.hedera.com/guides/docs/sdks/tokens/disable-kyc-account-flag">Hedera Documentation</a>
 */
public class TokenRevokeKycTransaction extends org.hiero.sdk.Transaction<TokenRevokeKycTransaction> {
    /**
     * The token ID that is associated with the account to remove the KYC flag for
     */
    @Nullable
    private TokenId tokenId = null;
    /**
     * The account ID that is associated with the account to remove the KYC flag
     */
    @Nullable
    private AccountId accountId = null;

    /**
     * Constructor.
     */
    public TokenRevokeKycTransaction() {}

    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction)
     *            records
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    TokenRevokeKycTransaction(
            LinkedHashMap<TransactionId, LinkedHashMap<AccountId, org.hiero.sdk.proto.Transaction>> txs)
            throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * Constructor.
     *
     * @param txBody protobuf TransactionBody
     */
    TokenRevokeKycTransaction(org.hiero.sdk.proto.TransactionBody txBody) {
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
    public TokenRevokeKycTransaction setTokenId(TokenId tokenId) {
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
    public TokenRevokeKycTransaction setAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId);
        requireNotFrozen();
        this.accountId = accountId;
        return this;
    }

    void initFromTransactionBody() {
        var body = sourceTransactionBody.getTokenRevokeKyc();
        if (body.hasToken()) {
            tokenId = TokenId.fromProtobuf(body.getToken());
        }

        if (body.hasAccount()) {
            accountId = AccountId.fromProtobuf(body.getAccount());
        }
    }

    /**
     * Build the transaction body.
     *
     * @return {@link
     *         org.hiero.sdk.proto.TokenRevokeKycTransactionBody}
     */
    TokenRevokeKycTransactionBody.Builder build() {
        var builder = TokenRevokeKycTransactionBody.newBuilder();
        if (tokenId != null) {
            builder.setToken(tokenId.toProtobuf());
        }

        if (accountId != null) {
            builder.setAccount(accountId.toProtobuf());
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
    MethodDescriptor<org.hiero.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getFreezeTokenAccountMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenRevokeKyc(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenRevokeKyc(build());
    }
}
