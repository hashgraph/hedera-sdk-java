package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.SignatureMap;
import com.hedera.hashgraph.sdk.proto.SignaturePair;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import java8.util.concurrent.CompletableFuture;
import java8.util.function.Function;
import org.bouncycastle.crypto.digests.SHA384Digest;
import org.threeten.bp.Duration;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Base class for all transactions that may be built and submitted to Hedera.
 *
 * @param <T> The type of the transaction. Used to enable chaining.
 */
public abstract class Transaction<T extends Transaction<T>>
    extends Executable<com.hedera.hashgraph.sdk.proto.Transaction, com.hedera.hashgraph.sdk.proto.TransactionResponse, TransactionResponse> {

    // Default auto renew duration for accounts, contracts, topics, and files (entities)
    static final Duration DEFAULT_AUTO_RENEW_PERIOD = Duration.ofDays(90);

    // Default transaction duration
    private static final Duration DEFAULT_TRANSACTION_VALID_DURATION = Duration.ofSeconds(120);

    protected TransactionBody.Builder bodyBuilder;

    // A SDK [Transaction] is composed of multiple, raw protobuf transactions. These should be
    // functionally identical, with the exception of pointing to different nodes. When retrying a
    // transaction after a network error or retry-able status response, we try a
    // different transaction and thus a different node.
    protected List<com.hedera.hashgraph.sdk.proto.Transaction.Builder> transactions = Collections.emptyList();
    protected List<SignatureMap.Builder> signatures = Collections.emptyList();
    protected List<AccountId> nodeIds = Collections.emptyList();

    // The index of the _next_ transaction to be built and executed.
    // Each time `buildNext` is invoked, this should be incremented by 1 and wrapped around with the
    // size of the transaction array.
    private int nextTransactionIndex = 0;

    Transaction() {
        bodyBuilder = TransactionBody.newBuilder();

        // Cannot call `Transaction#setTranscationValidDuration()` because it calls `isFrozen()` and
        // causes a `NullPointerException` in `TopicMessageSubmitTransaction#isFrozen()`. I assume the private
        // fields are not being set before the `super()` call which is why that is happening.
        bodyBuilder.setTransactionValidDuration(DurationConverter.toProtobuf(DEFAULT_TRANSACTION_VALID_DURATION));
    }

    Transaction(TransactionBody body) {
        this(body.toBuilder());
    }

    Transaction(TransactionBody.Builder bodyBuilder) {
        this.bodyBuilder = bodyBuilder;
    }

    public static Transaction<?> fromBytes(byte[] bytes) {
        com.hedera.hashgraph.sdk.proto.Transaction tx;

        try {
            tx = com.hedera.hashgraph.sdk.proto.Transaction.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            throw new IllegalArgumentException(e);
        }

        var isFrozen = tx.getSigMap().getSigPairCount() > 0;

        TransactionBody txBody;

        try {
            txBody = TransactionBody.parseFrom(tx.getBodyBytes());
        } catch (InvalidProtocolBufferException e) {
            throw new IllegalArgumentException(e);
        }

        Transaction<?> instance;

        switch (txBody.getDataCase()) {
            case CONTRACTCALL:
                instance = new ContractExecuteTransaction(txBody);
                break;

            case CONTRACTCREATEINSTANCE:
                instance = new ContractCreateTransaction(txBody);
                break;

            case CONTRACTUPDATEINSTANCE:
                instance = new ContractUpdateTransaction(txBody);
                break;

            case CONTRACTDELETEINSTANCE:
                instance = new ContractDeleteTransaction(txBody);
                break;

            case CRYPTOADDLIVEHASH:
                instance = new LiveHashAddTransaction(txBody);
                break;

            case CRYPTOCREATEACCOUNT:
                instance = new AccountCreateTransaction(txBody);
                break;

            case CRYPTODELETE:
                instance = new AccountDeleteTransaction(txBody);
                break;

            case CRYPTODELETELIVEHASH:
                instance = new LiveHashDeleteTransaction(txBody);
                break;

            case CRYPTOTRANSFER:
                instance = new CryptoTransferTransaction(txBody);
                break;

            case CRYPTOUPDATEACCOUNT:
                instance = new AccountUpdateTransaction(txBody);
                break;

            case FILEAPPEND:
                instance = new FileAppendTransaction(txBody);
                break;

            case FILECREATE:
                instance = new FileCreateTransaction(txBody);
                break;

            case FILEDELETE:
                instance = new FileDeleteTransaction(txBody);
                break;

            case FILEUPDATE:
                instance = new FileUpdateTransaction(txBody);
                break;

            case SYSTEMDELETE:
                instance = new SystemDeleteTransaction(txBody);
                break;

            case SYSTEMUNDELETE:
                instance = new SystemUndeleteTransaction(txBody);
                break;

            case FREEZE:
                instance = new FreezeTransaction(txBody);
                break;

            case CONSENSUSCREATETOPIC:
                instance = new TopicCreateTransaction(txBody);
                break;

            case CONSENSUSUPDATETOPIC:
                instance = new TopicUpdateTransaction(txBody);
                break;

            case CONSENSUSDELETETOPIC:
                instance = new TopicDeleteTransaction(txBody);
                break;

            case CONSENSUSSUBMITMESSAGE:
                // a chunked transaction does not need the same handling
                return new TopicMessageSubmitTransaction(txBody, tx.getSigMap());

            default:
                throw new IllegalArgumentException("parsed transaction body has no data");
        }

        if (isFrozen) {
            instance.signatures = Collections.singletonList(tx.getSigMap().toBuilder());
            instance.transactions = Collections.singletonList(tx.toBuilder());
        }

        return instance;
    }

    private static byte[] hash(byte[] bytes) {
        var digest = new SHA384Digest();
        var hash = new byte[digest.getDigestSize()];

        digest.update(bytes, 0, bytes.length);
        digest.doFinal(hash, 0);

        return hash;
    }

    @Nullable
    public final AccountId getNodeAccountId() {
        if (!nodeIds.isEmpty()) {
            return nodeIds.get(nextTransactionIndex);
        }

        if (bodyBuilder.hasNodeAccountID()) {
            return AccountId.fromProtobuf(bodyBuilder.getNodeAccountID());
        }

        return null;
    }

    @Override
    final AccountId getNodeAccountId(@Nullable Client client) {
        return Objects.requireNonNull(getNodeAccountId());
    }

    /**
     * Set the account ID of the node that this transaction will be submitted to.
     * <p>
     * Providing an explicit node account ID interferes with client-side load balancing of the
     * network. By default, the SDK will pre-generate a transaction for 1/3 of the nodes on the
     * network. If a node is down, busy, or otherwise reports a fatal error, the SDK will try again
     * with a different node.
     *
     * @param nodeAccountId The node AccountId to be set
     * @return {@code this}
     */
    public final T setNodeAccountId(AccountId nodeAccountId) {
        requireNotFrozen();
        bodyBuilder.setNodeAccountID(nodeAccountId.toProtobuf());

        // noinspection unchecked
        return (T) this;
    }

    @Nullable
    public final Duration getTransactionValidDuration() {
        return bodyBuilder.hasTransactionValidDuration() ? DurationConverter.fromProtobuf(bodyBuilder.getTransactionValidDuration()) : null;
    }

    /**
     * Sets the duration that this transaction is valid for.
     * <p>
     * This is defaulted by the SDK to 120 seconds (or two minutes).
     *
     * @param validDuration The duration to be set
     * @return {@code this}
     */
    public final T setTransactionValidDuration(Duration validDuration) {
        requireNotFrozen();
        bodyBuilder.setTransactionValidDuration(DurationConverter.toProtobuf(validDuration));

        // noinspection unchecked
        return (T) this;
    }

    @Nullable
    public final Hbar getMaxTransactionFee() {
        var transactionFee = bodyBuilder.getTransactionFee();

        if (transactionFee == 0) {
            // a zero max fee is assumed to be _no_
            // max fee has been set
            return null;
        }

        return Hbar.fromTinybars(transactionFee);
    }

    /**
     * Set the maximum transaction fee the operator (paying account) is willing to pay.
     *
     * @param maxTransactionFee the maximum transaction fee, in tinybars.
     * @return {@code this}
     */
    public final T setMaxTransactionFee(Hbar maxTransactionFee) {
        requireNotFrozen();
        bodyBuilder.setTransactionFee(maxTransactionFee.toTinybars());

        // noinspection unchecked
        return (T) this;
    }

    public final String getTransactionMemo() {
        return bodyBuilder.getMemo();
    }

    /**
     * Set a note or description that should be recorded in the transaction record (maximum length
     * of 100 characters).
     *
     * @param memo any notes or descriptions for this transaction.
     * @return {@code this}
     */
    public final T setTransactionMemo(String memo) {
        requireNotFrozen();
        bodyBuilder.setMemo(memo);

        // noinspection unchecked
        return (T) this;
    }

    public byte[] toBytes() {
        return transactions.get(0).setSigMap(signatures.get(0)).buildPartial().toByteArray();
    }

    public byte[] getTransactionHash() {
        if (!this.isFrozen()) {
            throw new IllegalStateException("transaction must have been frozen before calculating the hash will be stable, try calling `freeze`");
        }

        if (this.transactions.size() != 1) {
            throw new IllegalStateException("transaction must have an explicit node ID set, try calling `setNodeAccountId`");
        }

        return hash(makeRequest().toByteArray());
    }

    @Override
    public final TransactionId getTransactionId() {
        if (!bodyBuilder.hasTransactionID() && !this.isFrozen()) {
            throw new IllegalStateException("transaction must have been frozen before getting the transaction ID, try calling `freeze`");
        }

        return TransactionId.fromProtobuf(bodyBuilder.getTransactionID());
    }

    /**
     * Set the ID for this transaction.
     * <p>
     * The transaction ID includes the operator's account ( the account paying the transaction
     * fee). If two transactions have the same transaction ID, they won't both have an effect. One
     * will complete normally and the other will fail with a duplicate transaction status.
     * <p>
     * Normally, you should not use this method. Just before a transaction is executed, a
     * transaction ID will be generated from the operator on the client.
     *
     * @param transactionId The TransactionId to be set
     * @return {@code this}
     * @see TransactionId
     */
    public final T setTransactionId(TransactionId transactionId) {
        requireNotFrozen();
        bodyBuilder.setTransactionID(transactionId.toProtobuf());

        // noinspection unchecked
        return (T) this;
    }

    public final T sign(PrivateKey privateKey) {
        return signWith(privateKey.getPublicKey(), privateKey::sign);
    }

    public T signWith(PublicKey publicKey, Function<byte[], byte[]> transactionSigner) {
        if (!isFrozen()) {
            throw new IllegalStateException("Signing requires transaction to be frozen");
        }

        for (var index = 0; index < transactions.size(); ++index) {
            var bodyBytes = transactions.get(index).getBodyBytes().toByteArray();

            // NOTE: Yes the transactionSigner is invoked N times
            //  However for a verified/pin signature system it is reasonable to allow it to sign multiple
            //  transactions with identical details apart from the node ID
            var signatureBytes = transactionSigner.apply(bodyBytes);

            signatures
                .get(index)
                .addSigPair(publicKey.toSignaturePairProtobuf(signatureBytes));
        }

        // noinspection unchecked
        return (T) this;
    }

    public T signWithOperator(Client client) {
        var operator = client.getOperator();

        if (operator == null) {
            throw new IllegalStateException(
                "`client` must have an `operator` to sign with the operator");
        }

        if (!isFrozen()) {
            freezeWith(client);
        }

        if (keyAlreadySigned(operator.publicKey)) {
            // noinspection unchecked
            return (T) this;
        }

        return signWith(operator.publicKey, operator.transactionSigner);
    }

    protected boolean keyAlreadySigned(PublicKey key) {
        if (!signatures.isEmpty()) {
            for (var sigPair : signatures.get(0).getSigPairList()) {
                if (ByteString.copyFrom(key.toBytes()).startsWith(sigPair.getPubKeyPrefix())) {
                    // transaction already signed with the operator
                    // noinspection unchecked
                    return true;
                }
            }
        }

        return false;
    }

    public Transaction addSignature(PublicKey publicKey, byte[] signature) {
        this.requireExactNode();

        if (!isFrozen()) {
            freeze();
        }

        if (keyAlreadySigned(publicKey)) {
            // noinspection unchecked
            return (T) this;
        }

        this.signatures.get(0).addSigPair(publicKey.toSignaturePairProtobuf(signature));

        return this;
    }

    protected boolean isFrozen() {
        return !transactions.isEmpty();
    }

    protected void requireNotFrozen() {
        if (isFrozen()) {
            throw new IllegalStateException("transaction is immutable; it has at least one signature or has been explicitly frozen");
        }
    }

    protected void requireExactNode() {
        if (isFrozen()) {
            throw new IllegalStateException("transaction did not have an exact node ID set");
        }
    }

    /**
     * Freeze this transaction from further modification to prepare for
     * signing or serialization.
     *
     * @return {@code this}
     */
    public T freeze() {
        return freezeWith(null);
    }

    /**
     * Freeze this transaction from further modification to prepare for
     * signing or serialization.
     * <p>
     * Will use the `Client`, if available, to generate a default Transaction ID and select 1/3
     * nodes to prepare this transaction for.
     *
     * @return {@code this}
     */
    public T freezeWith(@Nullable Client client) {
        if (client != null && bodyBuilder.getTransactionFee() == 0) {
            bodyBuilder.setTransactionFee(client.maxTransactionFee.toTinybars());
        }

        if (!bodyBuilder.hasTransactionID() && client != null) {
            var operator = client.getOperator();

            if (operator != null) {
                // Set a default transaction ID, generated from the operator account ID
                setTransactionId(TransactionId.generate(operator.accountId));
            } else {
                // no client means there must be an explicitly set node ID and transaction ID
                throw new IllegalStateException(
                    "`client` must have an `operator` or `transactionId` must be set");
            }
        }

        if (!onFreeze(bodyBuilder)) {
            // noinspection unchecked
            return (T) this;
        }

        if (bodyBuilder.hasTransactionID() && bodyBuilder.hasNodeAccountID()) {
            // build exactly one transaction
            // this is all we need if there is a node account ID set
            signatures = Collections.singletonList(SignatureMap.newBuilder());
            transactions = Collections.singletonList(com.hedera.hashgraph.sdk.proto.Transaction.newBuilder()
                .setBodyBytes(bodyBuilder.build().toByteString()));

            // noinspection unchecked
            return (T) this;
        }

        if (bodyBuilder.hasTransactionID() && !nodeIds.isEmpty()) {
            // if the node IDs have been pre-selected but we have not yet frozen
            // are transaction

            transactions = new ArrayList<>(nodeIds.size());
            signatures = new ArrayList<>(nodeIds.size());

            for (AccountId nodeId : nodeIds) {
                signatures.add(SignatureMap.newBuilder());
                transactions.add(com.hedera.hashgraph.sdk.proto.Transaction.newBuilder()
                    .setBodyBytes(bodyBuilder
                        .setNodeAccountID(nodeId.toProtobuf())
                        .build()
                        .toByteString()
                    ));
            }

            // noinspection unchecked
            return (T) this;
        }

        if (bodyBuilder.hasTransactionID() && client != null) {
            // Pick N / 3 nodes from the client and build that many transactions
            // This is for fail-over so we can cycle through nodes

            var size = client.getNumberOfNodesForTransaction();
            transactions = new ArrayList<>(size);
            nodeIds = new ArrayList<>(size);
            signatures = new ArrayList<>(size);

            for (var i = 0; i < size; ++i) {
                var nodeId = client.getNextNodeId();

                nodeIds.add(nodeId);
                signatures.add(SignatureMap.newBuilder());
                transactions.add(com.hedera.hashgraph.sdk.proto.Transaction.newBuilder()
                    .setBodyBytes(bodyBuilder
                        .setNodeAccountID(nodeId.toProtobuf())
                        .build()
                        .toByteString()
                    ));
            }

            // noinspection unchecked
            return (T) this;
        }

        throw new IllegalStateException(
            "`client` must be provided or both `nodeId` and `transactionId` must be set");
    }

    /**
     * Called in {@link #freezeWith(Client)} just before the transaction
     * body is built. The intent is for the derived class to assign
     * their data variant to the transaction body.
     */
    abstract boolean onFreeze(TransactionBody.Builder bodyBuilder);

    @Override
    final com.hedera.hashgraph.sdk.proto.Transaction makeRequest() {
        return transactions.get(nextTransactionIndex).setSigMap(signatures.get(nextTransactionIndex)).build();
    }

    @Override
    final TransactionResponse mapResponse(
        com.hedera.hashgraph.sdk.proto.TransactionResponse transactionResponse,
        AccountId nodeId,
        com.hedera.hashgraph.sdk.proto.Transaction request
    ) {
        return new TransactionResponse(nodeId, Objects.requireNonNull(getTransactionId()), hash(request.toByteArray()));
    }

    @Override
    final Status mapResponseStatus(com.hedera.hashgraph.sdk.proto.TransactionResponse transactionResponse) {
        return Status.valueOf(transactionResponse.getNodeTransactionPrecheckCode());
    }

    @Override
    void advanceRequest() {
        // each time buildNext is called we move our cursor to the next transaction
        // wrapping around to ensure we are cycling
        nextTransactionIndex = (nextTransactionIndex + 1) % transactions.size();
    }

    @Override
    CompletableFuture<Void> onExecuteAsync(Client client) {
        if (!isFrozen()) {
            freezeWith(client);
        }

        var operatorId = client.getOperatorAccountId();

        if (operatorId != null && operatorId.equals(getTransactionId().accountId)) {
            // on execute, sign each transaction with the operator, if present
            // and we are signing a transaction that used the default transaction ID
            signWithOperator(client);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @SuppressWarnings("LiteProtoToString")
    public String toString() {
        // NOTE: regex is for removing the instance address from the default debug output
        return bodyBuilder.buildPartial().toString().replaceAll("@[A-Za-z0-9]+", "");
    }
}
