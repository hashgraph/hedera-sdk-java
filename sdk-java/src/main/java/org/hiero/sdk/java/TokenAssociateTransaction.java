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
import org.hiero.sdk.java.proto.TokenAssociateTransactionBody;
import org.hiero.sdk.java.proto.TokenServiceGrpc;
import org.hiero.sdk.java.proto.TransactionBody;
import org.hiero.sdk.java.proto.TransactionResponse;

/**
 * The transaction that will associate accounts to a token id.
 */
public class TokenAssociateTransaction extends Transaction<TokenAssociateTransaction> {
    @Nullable
    private AccountId accountId = null;

    private List<TokenId> tokenIds = new ArrayList<>();

    /**
     * Constructor.
     */
    public TokenAssociateTransaction() {
        defaultMaxTransactionFee = new Hbar(5);
    }

    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction)
     *            records
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    TokenAssociateTransaction(
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
    TokenAssociateTransaction(org.hiero.sdk.java.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
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
    public TokenAssociateTransaction setAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId);
        requireNotFrozen();
        this.accountId = accountId;
        return this;
    }

    /**
     * Extract the list of token id's.
     *
     * @return                          the list of token id's
     */
    public List<TokenId> getTokenIds() {
        return new ArrayList<>(tokenIds);
    }

    /**
     * Assign a new list of token id's.
     *
     * @param tokens                    the list of token id's
     * @return {@code this}
     */
    public TokenAssociateTransaction setTokenIds(List<TokenId> tokens) {
        Objects.requireNonNull(tokens);
        requireNotFrozen();
        this.tokenIds = new ArrayList<>(tokens);
        return this;
    }

    /**
     * Build the transaction body.
     *
     * @return {@link org.hiero.sdk.java.proto.TokenAssociateTransactionBody}
     */
    TokenAssociateTransactionBody.Builder build() {
        var builder = TokenAssociateTransactionBody.newBuilder();
        if (accountId != null) {
            builder.setAccount(accountId.toProtobuf());
        }

        for (var token : tokenIds) {
            if (token != null) {
                builder.addTokens(token.toProtobuf());
            }
        }

        return builder;
    }

    /**
     * Initialize from the transaction body.
     */
    void initFromTransactionBody() {
        var body = sourceTransactionBody.getTokenAssociate();
        if (body.hasAccount()) {
            accountId = AccountId.fromProtobuf(body.getAccount());
        }

        for (var token : body.getTokensList()) {
            tokenIds.add(TokenId.fromProtobuf(token));
        }
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        Objects.requireNonNull(client);
        if (accountId != null) {
            accountId.validateChecksum(client);
        }

        for (var token : tokenIds) {
            if (token != null) {
                token.validateChecksum(client);
            }
        }
    }

    @Override
    MethodDescriptor<org.hiero.sdk.java.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getAssociateTokensMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenAssociate(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenAssociate(build());
    }
}
