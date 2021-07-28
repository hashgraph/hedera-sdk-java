package com.hedera.hashgraph.sdk;

import com.google.errorprone.annotations.Var;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
//import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.SignatureMap;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.SignedTransaction;
import com.hedera.hashgraph.sdk.proto.TransactionList;
import java8.util.concurrent.CompletableFuture;
import java8.util.function.Function;
import org.bouncycastle.crypto.digests.SHA384Digest;
import org.threeten.bp.Duration;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.ArrayList;

/**
 * Base class for all transactions that may be built and submitted to Hedera.
 *
 * @param <T> The type of the transaction. Used to enable chaining.
 */
public abstract class Transaction<T extends Transaction<T>>
    extends Executable<T, com.hedera.hashgraph.sdk.proto.Transaction, com.hedera.hashgraph.sdk.proto.TransactionResponse, TransactionResponse> {

    // Default auto renew duration for accounts, contracts, topics, and files (entities)
    static final Duration DEFAULT_AUTO_RENEW_PERIOD = Duration.ofDays(90);

    // Default transaction duration
    private static final Duration DEFAULT_TRANSACTION_VALID_DURATION = Duration.ofSeconds(120);

    protected TransactionBody.Builder bodyBuilder;

    // A SDK [Transaction] is composed of multiple, raw protobuf transactions. These should be
    // functionally identical, with the exception of pointing to different nodes. When retrying a
    // transaction after a network error or retry-able status response, we try a
    // different transaction and thus a different node.
    protected List<com.hedera.hashgraph.sdk.proto.Transaction> transactions = Collections.emptyList();
    protected List<com.hedera.hashgraph.sdk.proto.SignedTransaction.Builder> signedTransactions = Collections.emptyList();
    protected List<SignatureMap.Builder> signatures = Collections.emptyList();
    protected List<TransactionId> transactionIds = Collections.emptyList();
    // For SDK Transactions that require multiple protobuf transaction ID's this variable keeps track of the current
    // execution group.
    // Example:
    // NodeIds: [ 3, 4 ]
    // Transactions: [
    //      { ID: 1, NodeAccountID: 3 }, // group = 0
    //      { ID: 1, NodeAccountID: 4 }, // group = 0
    //      { ID: 2, NodeAccountID: 3 }, // group = 1
    //      { ID: 2, NodeAccountID: 4 }  // group = 1
    // ]
    int nextTransactionIndex = 0;

    private List<PublicKey> publicKeys = new ArrayList<>();
    private List<Function<byte[], byte[]>> signers = new ArrayList<>();

    Transaction() {
        bodyBuilder = TransactionBody.newBuilder();

        // Cannot call `Transaction#setTranscationValidDuration()` because it calls `isFrozen()` and
        // causes a `NullPointerException` in `TopicMessageSubmitTransaction#isFrozen()`. I assume the private
        // fields are not being set before the `super()` call which is why that is happening.
        bodyBuilder.setTransactionValidDuration(DurationConverter.toProtobuf(DEFAULT_TRANSACTION_VALID_DURATION));

        // Default transaction fee is 2 Hbar
        bodyBuilder.setTransactionFee(new Hbar(2).toTinybars());
    }

    Transaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        bodyBuilder = txBody.toBuilder();

        // Cannot call `Transaction#setTranscationValidDuration()` because it calls `isFrozen()` and
        // causes a `NullPointerException` in `TopicMessageSubmitTransaction#isFrozen()`. I assume the private
        // fields are not being set before the `super()` call which is why that is happening.
        bodyBuilder.setTransactionValidDuration(DurationConverter.toProtobuf(DEFAULT_TRANSACTION_VALID_DURATION));

        // Default transaction fee is 2 Hbar
        bodyBuilder.setTransactionFee(new Hbar(2).toTinybars());
    }

    Transaction(com.hedera.hashgraph.sdk.proto.Transaction tx) throws InvalidProtocolBufferException {
        var transaction = SignedTransaction.parseFrom(tx.getSignedTransactionBytes());
        transactions.add(tx);
        signatures.add(transaction.getSigMap().toBuilder());
        signedTransactions.add(transaction.toBuilder());

        bodyBuilder = TransactionBody.parseFrom(signedTransactions.get(0).getBodyBytes()).toBuilder();

        // Cannot call `Transaction#setTranscationValidDuration()` because it calls `isFrozen()` and
        // causes a `NullPointerException` in `TopicMessageSubmitTransaction#isFrozen()`. I assume the private
        // fields are not being set before the `super()` call which is why that is happening.
        bodyBuilder.setTransactionValidDuration(DurationConverter.toProtobuf(DEFAULT_TRANSACTION_VALID_DURATION));

        // Default transaction fee is 2 Hbar
        bodyBuilder.setTransactionFee(new Hbar(2).toTinybars());
    }

    Transaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        var size = txs.values().iterator().next().size();

        nodeAccountIds = new ArrayList<>(size);
        signatures = new ArrayList<>(size * txs.keySet().size());
        transactions = new ArrayList<>(size * txs.keySet().size());
        signedTransactions = new ArrayList<>(size * txs.keySet().size());
        transactionIds = new ArrayList<>(txs.keySet().size());

        for (var transactionEntry : txs.entrySet()) {
            transactionIds.add(transactionEntry.getKey());

            for (var nodeEntry : transactionEntry.getValue().entrySet()) {
                if (nodeAccountIds.size() != size) {
                    nodeAccountIds.add(nodeEntry.getKey());
                }

                var transaction = SignedTransaction.parseFrom(nodeEntry.getValue().getSignedTransactionBytes());
                transactions.add(nodeEntry.getValue());
                signatures.add(transaction.getSigMap().toBuilder());
                signedTransactions.add(transaction.toBuilder());

                if (publicKeys.isEmpty()) {
                    for (var sigPair : transaction.getSigMap().getSigPairList()) {
                        publicKeys.add(PublicKey.fromBytes(sigPair.getPubKeyPrefix().toByteArray()));
                        signers.add(null);
                    }
                }
            }
        }

        nodeAccountIds.remove(new AccountId(0));

        bodyBuilder = TransactionBody.parseFrom(signedTransactions.get(0).getBodyBytes()).toBuilder();
    }

    public static Transaction<?> fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        var txs = new LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>>();
        @Var TransactionBody.DataCase dataCase = TransactionBody.DataCase.DATA_NOT_SET;

        var list = TransactionList.parseFrom(bytes);

        if (list.getTransactionListList().isEmpty()) {
            var transaction = com.hedera.hashgraph.sdk.proto.Transaction.parseFrom(bytes).toBuilder();

            TransactionBody txBody;
            if (transaction.getSignedTransactionBytes().isEmpty()) {
                txBody = TransactionBody.parseFrom(transaction.getBodyBytes());

                transaction.setSignedTransactionBytes(SignedTransaction.newBuilder()
                    .setBodyBytes(transaction.getBodyBytes())
                    .setSigMap(transaction.getSigMap())
                    .build()
                    .toByteString())
                    .clearBodyBytes()
                    .clearSigMap();
            } else {
                var signedTransaction = SignedTransaction.parseFrom(transaction.getSignedTransactionBytes());
                txBody = TransactionBody.parseFrom(signedTransaction.getBodyBytes());
            }

            dataCase = txBody.getDataCase();

            var account = AccountId.fromProtobuf(txBody.getNodeAccountID());
            var transactionId = TransactionId.fromProtobuf(txBody.getTransactionID());

            var linked = new LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>();
            linked.put(account, transaction.build());
            txs.put(transactionId, linked);
        } else {
            for (var transaction : list.getTransactionListList()) {
                var signedTransaction = SignedTransaction.parseFrom(transaction.getSignedTransactionBytes());
                var txBody = TransactionBody.parseFrom(signedTransaction.getBodyBytes());

                if (dataCase.getNumber() == TransactionBody.DataCase.DATA_NOT_SET.getNumber()) {
                    dataCase = txBody.getDataCase();
                }

                var account = AccountId.fromProtobuf(txBody.getNodeAccountID());
                var transactionId = TransactionId.fromProtobuf(txBody.getTransactionID());

                var linked = txs.containsKey(transactionId) ?
                    Objects.requireNonNull(txs.get(transactionId)) :
                    new LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>();

                linked.put(account, transaction);

                txs.put(transactionId, linked);
            }
        }

        switch (dataCase) {
            case CONTRACTCALL:
                return new ContractExecuteTransaction(txs);

            case CONTRACTCREATEINSTANCE:
                return new ContractCreateTransaction(txs);

            case CONTRACTUPDATEINSTANCE:
                return new ContractUpdateTransaction(txs);

            case CONTRACTDELETEINSTANCE:
                return new ContractDeleteTransaction(txs);

            case CRYPTOADDLIVEHASH:
                return new LiveHashAddTransaction(txs);

            case CRYPTOCREATEACCOUNT:
                return new AccountCreateTransaction(txs);

            case CRYPTODELETE:
                return new AccountDeleteTransaction(txs);

            case CRYPTODELETELIVEHASH:
                return new LiveHashDeleteTransaction(txs);

            case CRYPTOTRANSFER:
                return new TransferTransaction(txs);

            case CRYPTOUPDATEACCOUNT:
                return new AccountUpdateTransaction(txs);

            case FILEAPPEND:
                return new FileAppendTransaction(txs);

            case FILECREATE:
                return new FileCreateTransaction(txs);

            case FILEDELETE:
                return new FileDeleteTransaction(txs);

            case FILEUPDATE:
                return new FileUpdateTransaction(txs);

            case SYSTEMDELETE:
                return new SystemDeleteTransaction(txs);

            case SYSTEMUNDELETE:
                return new SystemUndeleteTransaction(txs);

            case FREEZE:
                return new FreezeTransaction(txs);

            case CONSENSUSCREATETOPIC:
                return new TopicCreateTransaction(txs);

            case CONSENSUSUPDATETOPIC:
                return new TopicUpdateTransaction(txs);

            case CONSENSUSDELETETOPIC:
                return new TopicDeleteTransaction(txs);

            case CONSENSUSSUBMITMESSAGE:
                return new TopicMessageSubmitTransaction(txs);

            case TOKENASSOCIATE:
                return new TokenAssociateTransaction(txs);

            case TOKENBURN:
                return new TokenBurnTransaction(txs);

            case TOKENCREATION:
                return new TokenCreateTransaction(txs);

            case TOKENDELETION:
                return new TokenDeleteTransaction(txs);

            case TOKENDISSOCIATE:
                return new TokenDissociateTransaction(txs);

            case TOKENFREEZE:
                return new TokenFreezeTransaction(txs);

            case TOKENGRANTKYC:
                return new TokenGrantKycTransaction(txs);

            case TOKENMINT:
                return new TokenMintTransaction(txs);

            case TOKENREVOKEKYC:
                return new TokenRevokeKycTransaction(txs);

            case TOKENUNFREEZE:
                return new TokenUnfreezeTransaction(txs);

            case TOKENUPDATE:
                return new TokenUpdateTransaction(txs);

            case TOKENWIPE:
                return new TokenWipeTransaction(txs);

            case TOKEN_FEE_SCHEDULE_UPDATE:
                return new TokenFeeScheduleUpdateTransaction(txs);

            case SCHEDULECREATE:
                return new ScheduleCreateTransaction(txs);

            case SCHEDULEDELETE:
                return new ScheduleDeleteTransaction(txs);

            case SCHEDULESIGN:
                return new ScheduleSignTransaction(txs);

            default:
                throw new IllegalArgumentException("parsed transaction body has no data");
        }
    }

    public static Transaction<?> fromScheduledTransaction(com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody scheduled) throws InvalidProtocolBufferException {
        var body = TransactionBody.newBuilder();

        switch (scheduled.getDataCase()) {
            case CONTRACTCALL:
                return new ContractExecuteTransaction(body.setContractCall(scheduled.getContractCall()).setMemo(scheduled.getMemo()).build());

            case CONTRACTCREATEINSTANCE:
                return new ContractCreateTransaction(body.setContractCreateInstance(scheduled.getContractCreateInstance()).setMemo(scheduled.getMemo()).build());

            case CONTRACTUPDATEINSTANCE:
                return new ContractUpdateTransaction(body.setContractUpdateInstance(scheduled.getContractUpdateInstance()).setMemo(scheduled.getMemo()).build());

            case CONTRACTDELETEINSTANCE:
                return new ContractDeleteTransaction(body.setContractDeleteInstance(scheduled.getContractDeleteInstance()).setMemo(scheduled.getMemo()).build());

            case CRYPTOCREATEACCOUNT:
                return new AccountCreateTransaction(body.setCryptoCreateAccount(scheduled.getCryptoCreateAccount()).setMemo(scheduled.getMemo()).build());

            case CRYPTODELETE:
                return new AccountDeleteTransaction(body.setCryptoDelete(scheduled.getCryptoDelete()).setMemo(scheduled.getMemo()).build());

            case CRYPTOTRANSFER:
                return new TransferTransaction(body.setCryptoTransfer(scheduled.getCryptoTransfer()).setMemo(scheduled.getMemo()).build());

            case CRYPTOUPDATEACCOUNT:
                return new AccountUpdateTransaction(body.setCryptoUpdateAccount(scheduled.getCryptoUpdateAccount()).setMemo(scheduled.getMemo()).build());

            case FILEAPPEND:
                return new FileAppendTransaction(body.setFileAppend(scheduled.getFileAppend()).setMemo(scheduled.getMemo()).build());

            case FILECREATE:
                return new FileCreateTransaction(body.setFileCreate(scheduled.getFileCreate()).setMemo(scheduled.getMemo()).build());

            case FILEDELETE:
                return new FileDeleteTransaction(body.setFileDelete(scheduled.getFileDelete()).setMemo(scheduled.getMemo()).build());

            case FILEUPDATE:
                return new FileUpdateTransaction(body.setFileUpdate(scheduled.getFileUpdate()).setMemo(scheduled.getMemo()).build());

            case SYSTEMDELETE:
                return new SystemUndeleteTransaction(body.setSystemDelete(scheduled.getSystemDelete()).setMemo(scheduled.getMemo()).build());

            case SYSTEMUNDELETE:
                return new SystemDeleteTransaction(body.setSystemUndelete(scheduled.getSystemUndelete()).setMemo(scheduled.getMemo()).build());

            case FREEZE:
                return new FreezeTransaction(body.setFreeze(scheduled.getFreeze()).setMemo(scheduled.getMemo()).build());

            case CONSENSUSCREATETOPIC:
                return new TopicCreateTransaction(body.setConsensusCreateTopic(scheduled.getConsensusCreateTopic()).setMemo(scheduled.getMemo()).build());

            case CONSENSUSUPDATETOPIC:
                return new TopicUpdateTransaction(body.setConsensusUpdateTopic(scheduled.getConsensusUpdateTopic()).setMemo(scheduled.getMemo()).build());

            case CONSENSUSDELETETOPIC:
                return new TopicDeleteTransaction(body.setConsensusDeleteTopic(scheduled.getConsensusDeleteTopic()).setMemo(scheduled.getMemo()).build());

            case CONSENSUSSUBMITMESSAGE:
                return new TopicMessageSubmitTransaction(body.setConsensusSubmitMessage(scheduled.getConsensusSubmitMessage()).setMemo(scheduled.getMemo()).build());

            case TOKENASSOCIATE:
                return new TokenAssociateTransaction(body.setTokenAssociate(scheduled.getTokenAssociate()).setMemo(scheduled.getMemo()).build());

            case TOKENBURN:
                return new TokenBurnTransaction(body.setTokenBurn(scheduled.getTokenBurn()).setMemo(scheduled.getMemo()).build());

            case TOKENCREATION:
                return new TokenCreateTransaction(body.setTokenCreation(scheduled.getTokenCreation()).setMemo(scheduled.getMemo()).build());

            case TOKENDELETION:
                return new TokenDeleteTransaction(body.setTokenDeletion(scheduled.getTokenDeletion()).setMemo(scheduled.getMemo()).build());

            case TOKENDISSOCIATE:
                return new TokenDissociateTransaction(body.setTokenDissociate(scheduled.getTokenDissociate()).setMemo(scheduled.getMemo()).build());

            case TOKENFREEZE:
                return new TokenFreezeTransaction(body.setTokenFreeze(scheduled.getTokenFreeze()).setMemo(scheduled.getMemo()).build());

            case TOKENGRANTKYC:
                return new TokenGrantKycTransaction(body.setTokenGrantKyc(scheduled.getTokenGrantKyc()).setMemo(scheduled.getMemo()).build());

            case TOKENMINT:
                return new TokenMintTransaction(body.setTokenMint(scheduled.getTokenMint()).setMemo(scheduled.getMemo()).build());

            case TOKENREVOKEKYC:
                return new TokenRevokeKycTransaction(body.setTokenRevokeKyc(scheduled.getTokenRevokeKyc()).setMemo(scheduled.getMemo()).build());

            case TOKENUNFREEZE:
                return new TokenUnfreezeTransaction(body.setTokenUnfreeze(scheduled.getTokenUnfreeze()).setMemo(scheduled.getMemo()).build());

            case TOKENUPDATE:
                return new TokenUpdateTransaction(body.setTokenUpdate(scheduled.getTokenUpdate()).setMemo(scheduled.getMemo()).build());

            case TOKENWIPE:
                return new TokenWipeTransaction(body.setTokenWipe(scheduled.getTokenWipe()).setMemo(scheduled.getMemo()).build());

            case SCHEDULEDELETE:
                return new ScheduleDeleteTransaction(body.setScheduleDelete(scheduled.getScheduleDelete()).setMemo(scheduled.getMemo()).build());

            default:
                throw new IllegalStateException("schedulable transaction did not have a transaction set");
        }
    }

    static byte[] hash(byte[] bytes) {
        var digest = new SHA384Digest();
        var hash = new byte[digest.getDigestSize()];

        digest.update(bytes, 0, bytes.length);
        digest.doFinal(hash, 0);

        return hash;
    }

    public ScheduleCreateTransaction schedule() {
        requireNotFrozen();

        if (!nodeAccountIds.isEmpty()) {
            throw new IllegalStateException(
                "The underlying transaction for a scheduled transaction cannot have node account IDs set"
            );
        }

        onFreeze(bodyBuilder);

        var schedulable = SchedulableTransactionBody.newBuilder()
            .setTransactionFee(bodyBuilder.getTransactionFee())
            .setMemo(bodyBuilder.getMemo());

        onScheduled(schedulable);

        var scheduled = new ScheduleCreateTransaction()
            .setScheduledTransactionBody(schedulable.build());

        if (!transactionIds.isEmpty()) {
            scheduled.setTransactionId(transactionIds.get(0).setScheduled(true));
        }

        return scheduled;
    }

    /**
     * Set the account IDs of the nodes that this transaction will be submitted to.
     * <p>
     * Providing an explicit node account ID interferes with client-side load balancing of the
     * network. By default, the SDK will pre-generate a transaction for 1/3 of the nodes on the
     * network. If a node is down, busy, or otherwise reports a fatal error, the SDK will try again
     * with a different node.
     *
     * @param nodeAccountIds The list of node AccountIds to be set
     * @return {@code this}
     */
    @Override
    public final T setNodeAccountIds(List<AccountId> nodeAccountIds) {
        requireNotFrozen();
        return super.setNodeAccountIds(nodeAccountIds);
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
        if (!this.isFrozen()) {
            throw new IllegalStateException("transaction must have been frozen before calculating the hash will be stable, try calling `freeze`");
        }

        buildAllTransactions();

        var list = TransactionList.newBuilder();

        for (var transaction : transactions) {
            list.addTransactionList(transaction);
        }

        return list.build().toByteArray();
    }

    public byte[] getTransactionHash() {
        if (!this.isFrozen()) {
            throw new IllegalStateException("transaction must have been frozen before calculating the hash will be stable, try calling `freeze`");
        }

        var index = nextTransactionIndex * nodeAccountIds.size() + nextNodeIndex;

        buildTransaction(index);

        return hash(transactions.get(index).getSignedTransactionBytes().toByteArray());
    }

    public Map<AccountId, byte[]> getTransactionHashPerNode() {
        if (!this.isFrozen()) {
            throw new IllegalStateException("transaction must have been frozen before calculating the hash will be stable, try calling `freeze`");
        }

        buildAllTransactions();

        var hashes = new HashMap<AccountId, byte[]>();

        for (var i = 0; i < transactions.size(); i++) {
            hashes.put(nodeAccountIds.get(i), hash(transactions.get(i).getSignedTransactionBytes().toByteArray()));
        }

        return hashes;
    }

    @Override
    public final TransactionId getTransactionId() {
        if (transactionIds.isEmpty() || !this.isFrozen()) {
            throw new IllegalStateException("transaction must have been frozen before getting the transaction ID, try calling `freeze`");
        }

        return transactionIds.get(nextTransactionIndex);
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
        transactionIds = Collections.singletonList(transactionId);

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

        if (keyAlreadySigned(publicKey)) {
            // noinspection unchecked
            return (T) this;
        }

        for(int i = 0; i < transactions.size(); i++) {
            transactions.set(i, null);
        }
        publicKeys.add(publicKey);
        signers.add(transactionSigner);

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
        for (var publicKey : publicKeys) {
            if (publicKey.toString().equals(key.toString())) {
                return true;
            }
        }

        return false;
    }

    public T addSignature(PublicKey publicKey, byte[] signature) {
        requireOneNodeAccountId();

        if (!isFrozen()) {
            freeze();
        }

        if (keyAlreadySigned(publicKey)) {
            // noinspection unchecked
            return (T) this;
        }

        for(int i = 0; i < transactions.size(); i++) {
            transactions.set(i, null);
        }
        publicKeys.add(publicKey);
        signers.add(null);
        signatures.get(0).addSigPair(publicKey.toSignaturePairProtobuf(signature));

        // noinspection unchecked
        return (T) this;
    }

    public Map<AccountId, Map<PublicKey, byte[]>> getSignatures() {
        var map = new HashMap<AccountId, Map<PublicKey, byte[]>>(nodeAccountIds.size());

        if (signatures.size() == 0) {
            return map;
        }

        for (int i = 0; i < nodeAccountIds.size(); i++) {
            var sigMap = signatures.get(i);
            var nodeAccountId = nodeAccountIds.get(i);

            var keyMap = map.containsKey(nodeAccountId) ?
                Objects.requireNonNull(map.get(nodeAccountId)) :
                new HashMap<PublicKey, byte[]>(sigMap.getSigPairCount());
            map.put(nodeAccountId, keyMap);

            for (var sigPair : sigMap.getSigPairList()) {
                keyMap.put(
                    PublicKey.fromBytes(sigPair.getPubKeyPrefix().toByteArray()),
                    sigPair.getEd25519().toByteArray()
                );
            }
        }

        return map;
    }

    protected boolean isFrozen() {
        return !signedTransactions.isEmpty();
    }

    protected void requireNotFrozen() {
        if (isFrozen()) {
            throw new IllegalStateException("transaction is immutable; it has at least one signature or has been explicitly frozen");
        }
    }

    protected void requireOneNodeAccountId() {
        if (nodeAccountIds.size() != 1) {
            throw new IllegalStateException("transaction did not have exactly one node ID set");
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
        if (isFrozen()) {
            // noinspection unchecked
            return (T) this;
        }

        if (client != null && bodyBuilder.getTransactionFee() == 0) {
            bodyBuilder.setTransactionFee(client.maxTransactionFee.toTinybars());
        }

        if (transactionIds.isEmpty()) {
            if(client != null) {
                var operator = client.getOperator();

                if (operator != null) {
                    // Set a default transaction ID, generated from the operator account ID
                    setTransactionId(TransactionId.generate(operator.accountId));
                } else {
                    // no client means there must be an explicitly set node ID and transaction ID
                    throw new IllegalStateException(
                        "`client` must have an `operator` or `transactionId` must be set");
                }
            } else {
                throw new IllegalStateException("Transaction ID must be set, or operator must be provided via freezeWith()");
            }
        }

        bodyBuilder.setTransactionID(transactionIds.get(0).toProtobuf());

        if (!onFreeze(bodyBuilder)) {
            // noinspection unchecked
            return (T) this;
        }

        if (nodeAccountIds.isEmpty()) {
            if (client == null) {
                throw new IllegalStateException(
                    "`client` must be provided or both `nodeId` and `transactionId` must be set");
            }

            try {
                nodeAccountIds = client.network.getNodeAccountIdsForExecute();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        transactions = new ArrayList<>(nodeAccountIds.size());
        signatures = new ArrayList<>(nodeAccountIds.size());
        signedTransactions = new ArrayList<>(nodeAccountIds.size());

        for (AccountId nodeId : nodeAccountIds) {
            signatures.add(SignatureMap.newBuilder());
            signedTransactions.add(com.hedera.hashgraph.sdk.proto.SignedTransaction.newBuilder()
                .setBodyBytes(bodyBuilder
                    .setNodeAccountID(nodeId.toProtobuf())
                    .build()
                    .toByteString()
                ));
            transactions.add(null);
        }

        // noinspection unchecked
        return (T) this;
    }

    void buildAllTransactions() {
        for (var i = 0; i < signedTransactions.size(); ++i) {
            buildTransaction(i);
        }
    }

    /**
     * Will build the specific transaction at {@code index} and will fill with `null` for any empty indices before it
     */
    void buildTransaction(int index) {
        if (
                transactions.get(index) != null &&
                !transactions.get(index).getSignedTransactionBytes().isEmpty()
        ) {
            return;
        }

        signTransaction(index);

        transactions.set(index, com.hedera.hashgraph.sdk.proto.Transaction.newBuilder()
            .setSignedTransactionBytes(
                signedTransactions.get(index)
                    .setSigMap(signatures.get(index))
                    .build()
                    .toByteString()
            ).build());
    }

    /**
     * Will sign the specific transaction at {@code index}
     */
    void signTransaction(int index) {
        if (signatures.get(index).getSigPairCount() != 0) {
            for (var i = 0; i < publicKeys.size(); i++) {
                var publicKey = publicKeys.get(i);
                var signer = signers.get(i);

                if (signer != null) {
                    for(var pair : signatures.get(index).getSigPairList()) {
                        if(pair.getPubKeyPrefix().equals(ByteString.copyFrom(publicKey.toBytes()))) {
                            return;
                        }
                    }
                }
            }
        }

        var bodyBytes = signedTransactions.get(index).getBodyBytes().toByteArray();

        for (var i = 0; i < publicKeys.size(); i++) {
            if (signers.get(i) == null) {
                continue;
            }

            var signatureBytes = signers.get(i).apply(bodyBytes);

            signatures
                .get(index)
                .addSigPair(publicKeys.get(i).toSignaturePairProtobuf(signatureBytes));
        }

    }

    /**
     * Called in {@link #freezeWith(Client)} just before the transaction
     * body is built. The intent is for the derived class to assign
     * their data variant to the transaction body.
     */
    abstract boolean onFreeze(TransactionBody.Builder bodyBuilder);

    /**
     * Called in {@link #schedule()} when convertin transaction into a scheduled version.
     */
    abstract void onScheduled(SchedulableTransactionBody.Builder scheduled);

    @Override
    final com.hedera.hashgraph.sdk.proto.Transaction makeRequest() {
        var index = nextNodeIndex + (nextTransactionIndex * nodeAccountIds.size());

        buildTransaction(index);

        return transactions.get(index);
    }

    @Override
    TransactionResponse mapResponse(
        com.hedera.hashgraph.sdk.proto.TransactionResponse transactionResponse,
        AccountId nodeId,
        com.hedera.hashgraph.sdk.proto.Transaction request
    ) {
        var transactionId = Objects.requireNonNull(getTransactionId());
        var hash = hash(request.getSignedTransactionBytes().toByteArray());
        nextTransactionIndex = (nextTransactionIndex + 1) % transactionIds.size();
        return new TransactionResponse(nodeId, transactionId, hash, null);
    }

    @Override
    final Status mapResponseStatus(com.hedera.hashgraph.sdk.proto.TransactionResponse transactionResponse) {
        return Status.valueOf(transactionResponse.getNodeTransactionPrecheckCode());
    }

    void validateChecksums(Client client) throws InvalidChecksumException {
        // Do nothing
    }

    @Override
    CompletableFuture<Void> onExecuteAsync(Client client) {
        if (!isFrozen()) {
            freezeWith(client);
        }

        var accountId = Objects.requireNonNull(Objects.requireNonNull(getTransactionId()).accountId);

        if(client.isAutoValidateChecksumsEnabled()) {
            try {
                accountId.validateChecksum(client);
                validateChecksums(client);
            } catch (InvalidChecksumException exc) {
                throw new IllegalArgumentException(
                    "Upon attempting to execute a transaction, automatic checksum validation found that an entity ID involved in that transaction had an invalid checksum: \"" +
                    exc.shard + "." + exc.realm + "." + exc.num + "\" had checksum \"" + exc.presentChecksum + "\", but the expected checksum was \"" + exc.expectedChecksum + "\""
                );
            }
        }

        var operatorId = client.getOperatorAccountId();
        if (operatorId != null && operatorId.equals(accountId)) {
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
        TransactionBody.Builder body = TransactionBody.newBuilder().mergeFrom(this.bodyBuilder.buildPartial());

        onFreeze(body);

        return body.buildPartial().toString().replaceAll("@[A-Za-z0-9]+", "");
    }
}
