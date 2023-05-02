/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.sdk;

import com.google.errorprone.annotations.Var;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.SignatureMap;
import com.hedera.hashgraph.sdk.proto.SignaturePair;
import com.hedera.hashgraph.sdk.proto.SignedTransaction;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionList;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java8.util.concurrent.CompletableFuture;
import java8.util.function.Function;
import org.bouncycastle.crypto.digests.SHA384Digest;
import org.threeten.bp.Duration;

import javax.annotation.Nullable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Base class for all transactions that may be built and submitted to Hedera.
 *
 * @param <T> The type of the transaction. Used to enable chaining.
 */
public abstract class Transaction<T extends Transaction<T>>
    extends Executable<T, com.hedera.hashgraph.sdk.proto.Transaction, com.hedera.hashgraph.sdk.proto.TransactionResponse, TransactionResponse> {

    /**
     * Default auto renew duration for accounts, contracts, topics, and files (entities)
     */
    static final Duration DEFAULT_AUTO_RENEW_PERIOD = Duration.ofDays(90);

    /**
     * Default transaction duration
     */
    private static final Duration DEFAULT_TRANSACTION_VALID_DURATION = Duration.ofSeconds(120);

    /**
     * Transaction constructors end their work by setting sourceTransactionBody.
     * The expectation is that the Transaction subclass constructor
     * will pick up where the Transaction superclass constructor left off,
     * and will unpack the data in the transaction body.
     */
    protected final TransactionBody sourceTransactionBody;
    /**
     * The builder that gets re-used to build each outer transaction.
     * freezeWith() will create the frozenBodyBuilder.
     * The presence of frozenBodyBuilder indicates that this transaction is frozen.
     */
    @Nullable
    protected TransactionBody.Builder frozenBodyBuilder = null;

    /**
     * An SDK [Transaction] is composed of multiple, raw protobuf transactions. These should be
     * functionally identical, except pointing to different nodes. When retrying a
     * transaction after a network error or retry-able status response, we try a
     * different transaction and thus a different node.
     */
    protected List<com.hedera.hashgraph.sdk.proto.Transaction> outerTransactions = Collections.emptyList();

    /**
     * An SDK [Transaction] is composed of multiple, raw protobuf transactions. These should be
     * functionally identical, except pointing to different nodes. When retrying a
     * transaction after a network error or retry-able status response, we try a
     * different transaction and thus a different node.
     */
    protected List<com.hedera.hashgraph.sdk.proto.SignedTransaction.Builder> innerSignedTransactions = Collections.emptyList();

    /**
     *  A set of signatures corresponding to every unique public key used to sign the transaction.
     */
    protected List<SignatureMap.Builder> sigPairLists = Collections.emptyList();

    /**
     * List of IDs for the transaction based on the operator because the transaction ID includes the operator's account
     */
    protected LockableList<TransactionId> transactionIds = new LockableList<>();

    /**
     * publicKeys and signers are parallel arrays.
     * If the signer associated with a public key is null, that means that the private key
     * associated with that public key has already contributed a signature to sigPairListBuilders, but
     * the signer is not available (likely because this came from fromBytes())
     */
    protected List<PublicKey> publicKeys = new ArrayList<>();

    /**
     * publicKeys and signers are parallel arrays.
     * If the signer associated with a public key is null, that means that the private key
     * associated with that public key has already contributed a signature to sigPairListBuilders, but
     * the signer is not available (likely because this came from fromBytes())
     */
    protected List<Function<byte[], byte[]>> signers = new ArrayList<>();

    /**
     * The maximum transaction fee the client is willing to pay
     */
    protected Hbar defaultMaxTransactionFee = new Hbar(2);

    private Duration transactionValidDuration;
    @Nullable
    private Hbar maxTransactionFee = null;
    private String memo = "";

    /**
     * Should the transaction id be regenerated
     */
    protected Boolean regenerateTransactionId = null;

    /**
     * Constructor.
     */
    Transaction() {
        setTransactionValidDuration(DEFAULT_TRANSACTION_VALID_DURATION);

        sourceTransactionBody = TransactionBody.getDefaultInstance();
    }

    // This constructor is used to construct from a scheduled transaction body
    /**
     * Constructor.
     *
     * @param txBody protobuf TransactionBody
     */
    Transaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        setTransactionValidDuration(DEFAULT_TRANSACTION_VALID_DURATION);
        setMaxTransactionFee(Hbar.fromTinybars(txBody.getTransactionFee()));
        setTransactionMemo(txBody.getMemo());

        sourceTransactionBody = txBody;
    }

    // This constructor is used to construct via fromBytes
    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction)
     *            records
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    Transaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        var txCount = txs.keySet().size();
        var nodeCount = txs.values().iterator().next().size();

        nodeAccountIds.ensureCapacity(nodeCount);
        sigPairLists = new ArrayList<>(nodeCount * txCount);
        outerTransactions = new ArrayList<>(nodeCount * txCount);
        innerSignedTransactions = new ArrayList<>(nodeCount * txCount);
        transactionIds.ensureCapacity(txCount);

        for (var transactionEntry : txs.entrySet()) {
            transactionIds.add(transactionEntry.getKey());

            for (var nodeEntry : transactionEntry.getValue().entrySet()) {
                if (nodeAccountIds.size() != nodeCount) {
                    nodeAccountIds.add(nodeEntry.getKey());
                }

                var transaction = SignedTransaction.parseFrom(nodeEntry.getValue().getSignedTransactionBytes());
                outerTransactions.add(nodeEntry.getValue());
                sigPairLists.add(transaction.getSigMap().toBuilder());
                innerSignedTransactions.add(transaction.toBuilder());

                if (publicKeys.isEmpty()) {
                    for (var sigPair : transaction.getSigMap().getSigPairList()) {
                        publicKeys.add(PublicKey.fromBytes(sigPair.getPubKeyPrefix().toByteArray()));
                        signers.add(null);
                    }
                }
            }
        }

        nodeAccountIds.remove(new AccountId(0)).setLocked(true);
        transactionIds.setLocked(true);

        // Verify that transaction bodies match
        for (@Var int i = 0; i < txCount; i++) {
            @Var TransactionBody firstTxBody = null;
            for (@Var int j = 0; j < nodeCount; j++) {
                int k = i*nodeCount + j;
                var txBody = TransactionBody.parseFrom(innerSignedTransactions.get(k).getBodyBytes());
                if (firstTxBody == null) {
                    firstTxBody = txBody;
                } else {
                    requireProtoMatches(
                        firstTxBody,
                        txBody,
                        new HashSet<>(Arrays.asList("NodeAccountID")),
                        "TransactionBody"
                    );
                }
            }
        }

        sourceTransactionBody = TransactionBody.parseFrom(innerSignedTransactions.get(0).getBodyBytes());

        setTransactionValidDuration(DurationConverter.fromProtobuf(sourceTransactionBody.getTransactionValidDuration()));
        setMaxTransactionFee(Hbar.fromTinybars(sourceTransactionBody.getTransactionFee()));
        setTransactionMemo(sourceTransactionBody.getMemo());

        // This constructor is used in fromBytes(), which means we're reconstructing
        // a transaction that was frozen and then serialized via toBytes(),
        // so this transaction should be constructed as frozen.
        frozenBodyBuilder = sourceTransactionBody.toBuilder();
    }

    /**
     * Create the correct transaction from a byte array.
     *
     * @param bytes                     the byte array
     * @return                          the new transaction
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
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

        return switch (dataCase) {
            case CONTRACTCALL -> new ContractExecuteTransaction(txs);
            case CONTRACTCREATEINSTANCE -> new ContractCreateTransaction(txs);
            case CONTRACTUPDATEINSTANCE -> new ContractUpdateTransaction(txs);
            case CONTRACTDELETEINSTANCE -> new ContractDeleteTransaction(txs);
            case ETHEREUMTRANSACTION -> new EthereumTransaction(txs);
            case CRYPTOADDLIVEHASH -> new LiveHashAddTransaction(txs);
            case CRYPTOCREATEACCOUNT -> new AccountCreateTransaction(txs);
            case CRYPTODELETE -> new AccountDeleteTransaction(txs);
            case CRYPTODELETELIVEHASH -> new LiveHashDeleteTransaction(txs);
            case CRYPTOTRANSFER -> new TransferTransaction(txs);
            case CRYPTOUPDATEACCOUNT -> new AccountUpdateTransaction(txs);
            case FILEAPPEND -> new FileAppendTransaction(txs);
            case FILECREATE -> new FileCreateTransaction(txs);
            case FILEDELETE -> new FileDeleteTransaction(txs);
            case FILEUPDATE -> new FileUpdateTransaction(txs);
            case SYSTEMDELETE -> new SystemDeleteTransaction(txs);
            case SYSTEMUNDELETE -> new SystemUndeleteTransaction(txs);
            case FREEZE -> new FreezeTransaction(txs);
            case CONSENSUSCREATETOPIC -> new TopicCreateTransaction(txs);
            case CONSENSUSUPDATETOPIC -> new TopicUpdateTransaction(txs);
            case CONSENSUSDELETETOPIC -> new TopicDeleteTransaction(txs);
            case CONSENSUSSUBMITMESSAGE -> new TopicMessageSubmitTransaction(txs);
            case TOKENASSOCIATE -> new TokenAssociateTransaction(txs);
            case TOKENBURN -> new TokenBurnTransaction(txs);
            case TOKENCREATION -> new TokenCreateTransaction(txs);
            case TOKENDELETION -> new TokenDeleteTransaction(txs);
            case TOKENDISSOCIATE -> new TokenDissociateTransaction(txs);
            case TOKENFREEZE -> new TokenFreezeTransaction(txs);
            case TOKENGRANTKYC -> new TokenGrantKycTransaction(txs);
            case TOKENMINT -> new TokenMintTransaction(txs);
            case TOKENREVOKEKYC -> new TokenRevokeKycTransaction(txs);
            case TOKENUNFREEZE -> new TokenUnfreezeTransaction(txs);
            case TOKENUPDATE -> new TokenUpdateTransaction(txs);
            case TOKENWIPE -> new TokenWipeTransaction(txs);
            case TOKEN_FEE_SCHEDULE_UPDATE -> new TokenFeeScheduleUpdateTransaction(txs);
            case SCHEDULECREATE -> new ScheduleCreateTransaction(txs);
            case SCHEDULEDELETE -> new ScheduleDeleteTransaction(txs);
            case SCHEDULESIGN -> new ScheduleSignTransaction(txs);
            case TOKEN_PAUSE -> new TokenPauseTransaction(txs);
            case TOKEN_UNPAUSE -> new TokenUnpauseTransaction(txs);
            case CRYPTOAPPROVEALLOWANCE -> new AccountAllowanceApproveTransaction(txs);
            case CRYPTODELETEALLOWANCE -> new AccountAllowanceDeleteTransaction(txs);
            default -> throw new IllegalArgumentException("parsed transaction body has no data");
        };
    }

    /**
     * Create the correct transaction from a scheduled transaction.
     *
     * @param scheduled                 the scheduled transaction
     * @return                          the new transaction
     */
    public static Transaction<?> fromScheduledTransaction(com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody scheduled) {
        var body = TransactionBody.newBuilder()
            .setMemo(scheduled.getMemo())
            .setTransactionFee(scheduled.getTransactionFee());

        switch (scheduled.getDataCase()) {
            case CONTRACTCALL:
                return new ContractExecuteTransaction(body.setContractCall(scheduled.getContractCall()).build());

            case CONTRACTCREATEINSTANCE:
                return new ContractCreateTransaction(body.setContractCreateInstance(scheduled.getContractCreateInstance()).build());

            case CONTRACTUPDATEINSTANCE:
                return new ContractUpdateTransaction(body.setContractUpdateInstance(scheduled.getContractUpdateInstance()).build());

            case CONTRACTDELETEINSTANCE:
                return new ContractDeleteTransaction(body.setContractDeleteInstance(scheduled.getContractDeleteInstance()).build());

            case CRYPTOAPPROVEALLOWANCE:
                return new AccountAllowanceApproveTransaction(body.setCryptoApproveAllowance(scheduled.getCryptoApproveAllowance()).build());

            case CRYPTODELETEALLOWANCE:
                return new AccountAllowanceDeleteTransaction(body.setCryptoDeleteAllowance(scheduled.getCryptoDeleteAllowance()).build());

            case CRYPTOCREATEACCOUNT:
                return new AccountCreateTransaction(body.setCryptoCreateAccount(scheduled.getCryptoCreateAccount()).build());

            case CRYPTODELETE:
                return new AccountDeleteTransaction(body.setCryptoDelete(scheduled.getCryptoDelete()).build());

            case CRYPTOTRANSFER:
                return new TransferTransaction(body.setCryptoTransfer(scheduled.getCryptoTransfer()).build());

            case CRYPTOUPDATEACCOUNT:
                return new AccountUpdateTransaction(body.setCryptoUpdateAccount(scheduled.getCryptoUpdateAccount()).build());

            case FILEAPPEND:
                return new FileAppendTransaction(body.setFileAppend(scheduled.getFileAppend()).build());

            case FILECREATE:
                return new FileCreateTransaction(body.setFileCreate(scheduled.getFileCreate()).build());

            case FILEDELETE:
                return new FileDeleteTransaction(body.setFileDelete(scheduled.getFileDelete()).build());

            case FILEUPDATE:
                return new FileUpdateTransaction(body.setFileUpdate(scheduled.getFileUpdate()).build());

            case SYSTEMDELETE:
                return new SystemDeleteTransaction(body.setSystemDelete(scheduled.getSystemDelete()).build());

            case SYSTEMUNDELETE:
                return new SystemUndeleteTransaction(body.setSystemUndelete(scheduled.getSystemUndelete()).build());

            case FREEZE:
                return new FreezeTransaction(body.setFreeze(scheduled.getFreeze()).build());

            case CONSENSUSCREATETOPIC:
                return new TopicCreateTransaction(body.setConsensusCreateTopic(scheduled.getConsensusCreateTopic()).build());

            case CONSENSUSUPDATETOPIC:
                return new TopicUpdateTransaction(body.setConsensusUpdateTopic(scheduled.getConsensusUpdateTopic()).build());

            case CONSENSUSDELETETOPIC:
                return new TopicDeleteTransaction(body.setConsensusDeleteTopic(scheduled.getConsensusDeleteTopic()).build());

            case CONSENSUSSUBMITMESSAGE:
                return new TopicMessageSubmitTransaction(body.setConsensusSubmitMessage(scheduled.getConsensusSubmitMessage()).build());

            case TOKENCREATION:
                return new TokenCreateTransaction(body.setTokenCreation(scheduled.getTokenCreation()).build());

            case TOKENFREEZE:
                return new TokenFreezeTransaction(body.setTokenFreeze(scheduled.getTokenFreeze()).build());

            case TOKENUNFREEZE:
                return new TokenUnfreezeTransaction(body.setTokenUnfreeze(scheduled.getTokenUnfreeze()).build());

            case TOKENGRANTKYC:
                return new TokenGrantKycTransaction(body.setTokenGrantKyc(scheduled.getTokenGrantKyc()).build());

            case TOKENREVOKEKYC:
                return new TokenRevokeKycTransaction(body.setTokenRevokeKyc(scheduled.getTokenRevokeKyc()).build());

            case TOKENDELETION:
                return new TokenDeleteTransaction(body.setTokenDeletion(scheduled.getTokenDeletion()).build());

            case TOKENUPDATE:
                return new TokenUpdateTransaction(body.setTokenUpdate(scheduled.getTokenUpdate()).build());

            case TOKENMINT:
                return new TokenMintTransaction(body.setTokenMint(scheduled.getTokenMint()).build());

            case TOKENBURN:
                return new TokenBurnTransaction(body.setTokenBurn(scheduled.getTokenBurn()).build());

            case TOKENWIPE:
                return new TokenWipeTransaction(body.setTokenWipe(scheduled.getTokenWipe()).build());

            case TOKENASSOCIATE:
                return new TokenAssociateTransaction(body.setTokenAssociate(scheduled.getTokenAssociate()).build());

            case TOKENDISSOCIATE:
                return new TokenDissociateTransaction(body.setTokenDissociate(scheduled.getTokenDissociate()).build());

            case TOKEN_FEE_SCHEDULE_UPDATE:
                return new TokenFeeScheduleUpdateTransaction(body.setTokenFeeScheduleUpdate(scheduled.getTokenFeeScheduleUpdate()).build());

            case TOKEN_PAUSE:
                return new TokenPauseTransaction(body.setTokenPause(scheduled.getTokenPause()).build());

            case TOKEN_UNPAUSE:
                return new TokenUnpauseTransaction(body.setTokenUnpause(scheduled.getTokenUnpause()).build());

            case SCHEDULEDELETE:
                return new ScheduleDeleteTransaction(body.setScheduleDelete(scheduled.getScheduleDelete()).build());

            default:
                throw new IllegalStateException("schedulable transaction did not have a transaction set");
        }
    }

    private static void throwProtoMatchException(String fieldName, String aWas, String bWas) {
        throw new IllegalArgumentException(
            "fromBytes() failed because " + fieldName +
                " fields in TransactionBody protobuf messages in the TransactionList did not match: A was " +
                aWas + ", B was " + bWas
        );
    }

    private static void requireProtoMatches(Object protoA, Object protoB, Set<String> ignoreSet, String thisFieldName) {
        var aIsNull = protoA == null;
        var bIsNull = protoB == null;
        if (aIsNull != bIsNull) {
            throwProtoMatchException(thisFieldName, aIsNull ? "null" : "not null", bIsNull ? "null" : "not null");
        }
        if (aIsNull) {
            return;
        }
        var protoAClass = protoA.getClass();
        var protoBClass = protoB.getClass();
        if (!protoAClass.equals(protoBClass)) {
            throwProtoMatchException(thisFieldName, "of class " + protoAClass, "of class " + protoBClass);
        }
        if (protoA instanceof Boolean ||
            protoA instanceof Integer ||
            protoA instanceof Long ||
            protoA instanceof Float ||
            protoA instanceof Double ||
            protoA instanceof String ||
            protoA instanceof ByteString
        ) {
            // System.out.println("values A = " + protoA.toString() + ", B = " + protoB.toString());
            if (!protoA.equals(protoB)) {
                throwProtoMatchException(thisFieldName, protoA.toString(), protoB.toString());
            }
        }
        for (var method : protoAClass.getDeclaredMethods()) {
            if (method.getParameterCount() != 0) {
                continue;
            }
            int methodModifiers = method.getModifiers();
            if ((!Modifier.isPublic(methodModifiers)) || Modifier.isStatic(methodModifiers)) {
                continue;
            }
            var methodName = method.getName();
            if (!methodName.startsWith("get")) {
                continue;
            }
            var isList = methodName.endsWith("List") && List.class.isAssignableFrom(method.getReturnType());
            var methodFieldName = methodName.substring(3, methodName.length() - (isList ? 4 : 0));
            if (ignoreSet.contains(methodFieldName) || methodFieldName.equals("DefaultInstance")) {
                continue;
            }
            if (!isList) {
                try {
                    var hasMethod = protoAClass.getMethod("has" + methodFieldName);
                    var hasA = (Boolean) hasMethod.invoke(protoA);
                    var hasB = (Boolean) hasMethod.invoke(protoB);
                    if (!hasA.equals(hasB)) {
                        throwProtoMatchException(methodFieldName, hasA ? "present" : "not present", hasB ? "present" : "not present");
                    }
                    if (!hasA) {
                        continue;
                    }
                } catch (NoSuchMethodException ignored) {
                    // pass if there is no has method
                } catch (IllegalArgumentException error) {
                    throw error;
                } catch (Throwable error) {
                    throw new IllegalArgumentException("fromBytes() failed due to error", error);
                }
            }
            try {
                var retvalA = method.invoke(protoA);
                var retvalB = method.invoke(protoB);
                if (isList) {
                    var listA = (List<?>) retvalA;
                    var listB = (List<?>) retvalB;
                    if (listA.size() != listB.size()) {
                        throwProtoMatchException(methodFieldName, "of size " + listA.size(), "of size " + listB.size());
                    }
                    for (@Var int i = 0; i < listA.size(); i++) {
                        // System.out.println("comparing " + thisFieldName + "." + methodFieldName + "[" + i + "]");
                        requireProtoMatches(listA.get(i), listB.get(i), ignoreSet, methodFieldName + "[" + i + "]");
                    }
                } else {
                    // System.out.println("comparing " + thisFieldName + "." + methodFieldName);
                    requireProtoMatches(retvalA, retvalB, ignoreSet, methodFieldName);
                }
            } catch (IllegalArgumentException error) {
                throw error;
            } catch (Throwable error) {
                throw new IllegalArgumentException("fromBytes() failed due to error", error);
            }
        }
    }

    /**
     * Generate a hash from a byte array.
     *
     * @param bytes                     the byte array
     * @return                          the hash
     */
    static byte[] hash(byte[] bytes) {
        var digest = new SHA384Digest();
        var hash = new byte[digest.getDigestSize()];

        digest.update(bytes, 0, bytes.length);
        digest.doFinal(hash, 0);

        return hash;
    }

    /**
     * Converts transaction into a scheduled version
     *
     * @param bodyBuilder   the transaction's body builder
     * @return the scheduled transaction
     */
    protected ScheduleCreateTransaction doSchedule(TransactionBody.Builder bodyBuilder) {
        var schedulable = SchedulableTransactionBody.newBuilder()
            .setTransactionFee(bodyBuilder.getTransactionFee())
            .setMemo(bodyBuilder.getMemo());

        onScheduled(schedulable);

        var scheduled = new ScheduleCreateTransaction()
            .setScheduledTransactionBody(schedulable.build());

        if (!transactionIds.isEmpty()) {
            scheduled.setTransactionId(transactionIds.get(0));
        }

        return scheduled;
    }

    /**
     * Extract the scheduled transaction.
     *
     * @return                          the scheduled transaction
     */
    public ScheduleCreateTransaction schedule() {
        requireNotFrozen();
        if (!nodeAccountIds.isEmpty()) {
            throw new IllegalStateException(
                "The underlying transaction for a scheduled transaction cannot have node account IDs set"
            );
        }

        var bodyBuilder = spawnBodyBuilder(null);

        onFreeze(bodyBuilder);

        return doSchedule(bodyBuilder);
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
        Objects.requireNonNull(nodeAccountIds);
        return super.setNodeAccountIds(nodeAccountIds);
    }

    /**
     * Extract the valid transaction duration.
     *
     * @return                          the transaction valid duration
     */
    @Nullable
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "A Duration can't actually be mutated"
    )
    public final Duration getTransactionValidDuration() {
        return transactionValidDuration;
    }

    /**
     * Sets the duration that this transaction is valid for.
     * <p>
     * This is defaulted by the SDK to 120 seconds (or two minutes).
     *
     * @param validDuration The duration to be set
     * @return {@code this}
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "A Duration can't actually be mutated"
    )
    public final T setTransactionValidDuration(Duration validDuration) {
        requireNotFrozen();
        Objects.requireNonNull(validDuration);
        transactionValidDuration = validDuration;
        // noinspection unchecked
        return (T) this;
    }

    /**
     * Extract the maximum transaction fee.
     *
     * @return                          the maximum transaction fee
     */
    @Nullable
    public final Hbar getMaxTransactionFee() {
        return maxTransactionFee;
    }

    /**
     * Set the maximum transaction fee the operator (paying account) is willing to pay.
     *
     * @param maxTransactionFee the maximum transaction fee, in tinybars.
     * @return {@code this}
     */
    public final T setMaxTransactionFee(Hbar maxTransactionFee) {
        requireNotFrozen();
        Objects.requireNonNull(maxTransactionFee);
        this.maxTransactionFee = maxTransactionFee;
        // noinspection unchecked
        return (T) this;
    }

    /**
     * Extract the default maximum transaction fee.
     *
     * @return                          the default maximum transaction fee
     */
    public final Hbar getDefaultMaxTransactionFee() {
        return defaultMaxTransactionFee;
    }

    /**
     * Extract the memo for the transaction.
     *
     * @return                          the memo for the transaction
     */
    public final String getTransactionMemo() {
        return memo;
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
        Objects.requireNonNull(memo);
        this.memo = memo;
        // noinspection unchecked
        return (T) this;
    }

    /**
     * Extract a byte array representation.
     *
     * @return                          the byte array representation
     */
    public byte[] toBytes() {
        if (!this.isFrozen()) {
            throw new IllegalStateException("transaction must have been frozen before conversion to bytes will be stable, try calling `freeze`");
        }

        buildAllTransactions();

        var list = TransactionList.newBuilder();

        for (var transaction : outerTransactions) {
            list.addTransactionList(transaction);
        }

        return list.build().toByteArray();
    }

    /**
     * Extract a byte array of the transaction hash.
     *
     * @return                          the transaction hash
     */
    public byte[] getTransactionHash() {
        if (!this.isFrozen()) {
            throw new IllegalStateException("transaction must have been frozen before calculating the hash will be stable, try calling `freeze`");
        }

        transactionIds.setLocked(true);
        nodeAccountIds.setLocked(true);

        var index = transactionIds.getIndex() * nodeAccountIds.size() + nodeAccountIds.getIndex();

        buildTransaction(index);

        return hash(outerTransactions.get(index).getSignedTransactionBytes().toByteArray());
    }

    /**
     * Extract the list of account id and hash records.
     *
     * @return                          the list of account id and hash records
     */
    public Map<AccountId, byte[]> getTransactionHashPerNode() {
        if (!this.isFrozen()) {
            throw new IllegalStateException("transaction must have been frozen before calculating the hash will be stable, try calling `freeze`");
        }

        buildAllTransactions();

        var hashes = new HashMap<AccountId, byte[]>();

        for (var i = 0; i < outerTransactions.size(); i++) {
            hashes.put(nodeAccountIds.get(i), hash(outerTransactions.get(i).getSignedTransactionBytes().toByteArray()));
        }

        return hashes;
    }

    @Override
    final TransactionId getTransactionIdInternal() {
        return transactionIds.getCurrent();
    }

    /**
     * Extract the transaction id.
     *
     * @return                          the transaction id
     */
    public final TransactionId getTransactionId() {
        if (transactionIds.isEmpty() || !this.isFrozen()) {
            throw new IllegalStateException("transaction must have been frozen before getting the transaction ID, try calling `freeze`");
        }

        return transactionIds.setLocked(true).getCurrent();
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
        transactionIds.setList(Collections.singletonList(transactionId)).setLocked(true);

        // noinspection unchecked
        return (T) this;
    }

    /**
     * Should the transaction id be regenerated.
     *
     * @return                          should the transaction id be regenerated
     */
    public final Boolean getRegenerateTransactionId() {
        return regenerateTransactionId;
    }

    /**
     * Regenerate the transaction id.
     *
     * @param regenerateTransactionId   should the transaction id be regenerated
     * @return {@code this}
     */
    public final T setRegenerateTransactionId(boolean regenerateTransactionId) {
        this.regenerateTransactionId = regenerateTransactionId;

        // noinspection unchecked
        return (T) this;
    }

    /**
     * Sign the transaction.
     *
     * @param privateKey                the private key
     * @return                          the signed transaction
     */
    public final T sign(PrivateKey privateKey) {
        return signWith(privateKey.getPublicKey(), privateKey::sign);
    }

    /**
     * Sign the transaction.
     *
     * @param publicKey                 the public key
     * @param transactionSigner         the key list
     * @return {@code this}
     */
    public T signWith(PublicKey publicKey, Function<byte[], byte[]> transactionSigner) {
        if (!isFrozen()) {
            throw new IllegalStateException("Signing requires transaction to be frozen");
        }

        if (keyAlreadySigned(publicKey)) {
            // noinspection unchecked
            return (T) this;
        }

        for (int i = 0; i < outerTransactions.size(); i++) {
            outerTransactions.set(i, null);
        }
        publicKeys.add(publicKey);
        signers.add(transactionSigner);

        // noinspection unchecked
        return (T) this;
    }

    /**
     * Sign the transaction with the configured client.
     *
     * @param client                    the configured client
     * @return                          the signed transaction
     */
    public T signWithOperator(Client client) {
        var operator = client.getOperator();

        if (operator == null) {
            throw new IllegalStateException(
                "`client` must have an `operator` to sign with the operator");
        }

        if (!isFrozen()) {
            freezeWith(client);
        }

        return signWith(operator.publicKey, operator.transactionSigner);
    }

    /**
     * Checks if a public key is already added to the transaction
     *
     * @param key   the public key
     * @return if the public key is already added
     */
    protected boolean keyAlreadySigned(PublicKey key) {
        return publicKeys.contains(key);
    }

    /**
     * Add a signature to the transaction.
     *
     * @param publicKey                 the public key
     * @param signature                 the signature
     * @return {@code this}
     */
    public T addSignature(PublicKey publicKey, byte[] signature) {
        requireOneNodeAccountId();
        if (!isFrozen()) {
            throw new IllegalStateException("Adding signature requires transaction to be frozen");
        }

        if (keyAlreadySigned(publicKey)) {
            // noinspection unchecked
            return (T) this;
        }

        transactionIds.setLocked(true);
        nodeAccountIds.setLocked(true);

        for (int i = 0; i < outerTransactions.size(); i++) {
            outerTransactions.set(i, null);
        }
        publicKeys.add(publicKey);
        signers.add(null);
        sigPairLists.get(0).addSigPair(publicKey.toSignaturePairProtobuf(signature));

        // noinspection unchecked
        return (T) this;
    }

    protected Map<AccountId, Map<PublicKey, byte[]>> getSignaturesAtOffset(int offset) {
        var map = new HashMap<AccountId, Map<PublicKey, byte[]>>(nodeAccountIds.size());

        for (int i = 0; i < nodeAccountIds.size(); i++) {
            var sigMap = sigPairLists.get(i + offset);
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

    /**
     * Extract list of account id and public keys.
     *
     * @return                          the list of account id and public keys
     */
    public Map<AccountId, Map<PublicKey, byte[]>> getSignatures() {
        if (publicKeys.isEmpty()) {
            return Collections.emptyMap();
        }

        buildAllTransactions();

        return getSignaturesAtOffset(0);
    }

    /**
     * Check if transaction is frozen.
     *
     * @return                          is the transaction frozen
     */
    protected boolean isFrozen() {
        return frozenBodyBuilder != null;
    }

    /**
     * Throw an exception if the transaction is frozen.
     */
    protected void requireNotFrozen() {
        if (isFrozen()) {
            throw new IllegalStateException("transaction is immutable; it has at least one signature or has been explicitly frozen");
        }
    }

    /**
     * Throw an exception if there is not exactly one node id set.
     */
    protected void requireOneNodeAccountId() {
        if (nodeAccountIds.size() != 1) {
            throw new IllegalStateException("transaction did not have exactly one node ID set");
        }
    }

    protected TransactionBody.Builder spawnBodyBuilder(@Nullable Client client) {
        var clientDefaultFee = client != null ? client.getDefaultMaxTransactionFee() : null;

        var defaultFee = clientDefaultFee != null ? clientDefaultFee : defaultMaxTransactionFee;

        var feeHbars = maxTransactionFee != null ? maxTransactionFee : defaultFee;

        return TransactionBody.newBuilder()
            .setTransactionFee(feeHbars.toTinybars())
            .setTransactionValidDuration(DurationConverter.toProtobuf(transactionValidDuration).toBuilder())
            .setMemo(memo);
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
     * @param client                    the configured client
     * @return {@code this}
     */
    public T freezeWith(@Nullable Client client) {
        if (isFrozen()) {
            // noinspection unchecked
            return (T) this;
        }

        if (transactionIds.isEmpty()) {
            if (client != null) {
                var operator = client.getOperator();

                if (operator != null) {
                    // Set a default transaction ID, generated from the operator account ID

                    transactionIds.setList(Collections.singletonList(TransactionId.generate(operator.accountId)));
                } else {
                    // no client means there must be an explicitly set node ID and transaction ID
                    throw new IllegalStateException(
                        "`client` must have an `operator` or `transactionId` must be set");
                }
            } else {
                throw new IllegalStateException("Transaction ID must be set, or operator must be provided via freezeWith()");
            }
        }

        if (nodeAccountIds.isEmpty()) {
            if (client == null) {
                throw new IllegalStateException(
                    "`client` must be provided or both `nodeId` and `transactionId` must be set");
            }

            try {
                nodeAccountIds.setList(client.network.getNodeAccountIdsForExecute());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        frozenBodyBuilder = spawnBodyBuilder(client).setTransactionID(transactionIds.get(0).toProtobuf());
        onFreeze(frozenBodyBuilder);

        int requiredChunks = getRequiredChunks();
        generateTransactionIds(transactionIds.get(0), requiredChunks);
        wipeTransactionLists(requiredChunks);

        var clientDefaultRegenerateTransactionId = client != null ? client.getDefaultRegenerateTransactionId() : null;
        regenerateTransactionId = regenerateTransactionId != null ? regenerateTransactionId : clientDefaultRegenerateTransactionId;

        // noinspection unchecked
        return (T) this;
    }

    /**
     * There must be at least one chunk.
     *
     * @return                          there is 1 required chunk
     */
    int getRequiredChunks() {
        return 1;
    }

    /**
     * Generate transaction id's.
     *
     * @param initialTransactionId      the initial transaction id
     * @param count                     the number of id's to generate.
     */
    void generateTransactionIds(TransactionId initialTransactionId, int count) {
        var locked = transactionIds.isLocked();
        transactionIds.setLocked(false);

        if (count == 1) {
            transactionIds.setList(Collections.singletonList(initialTransactionId));
            return;
        }

        var nextTransactionId = initialTransactionId.toProtobuf().toBuilder();
        transactionIds.ensureCapacity(count);
        transactionIds.clear();
        for (int i = 0; i < count; i++) {
            transactionIds.add(TransactionId.fromProtobuf(nextTransactionId.build()));

            // add 1 ns to the validStart to make cascading transaction IDs
            var nextValidStart = nextTransactionId.getTransactionValidStart().toBuilder();
            nextValidStart.setNanos(nextValidStart.getNanos() + 1);

            nextTransactionId.setTransactionValidStart(nextValidStart);
        }

        transactionIds.setLocked(locked);
    }

    /**
     * Wipe / reset the transaction list.
     *
     * @param requiredChunks            the number of required chunks
     */
    void wipeTransactionLists(int requiredChunks) {
        Objects.requireNonNull(frozenBodyBuilder).setTransactionID(getTransactionIdInternal().toProtobuf());

        outerTransactions = new ArrayList<>(nodeAccountIds.size());
        sigPairLists = new ArrayList<>(nodeAccountIds.size());
        innerSignedTransactions = new ArrayList<>(nodeAccountIds.size());

        for (AccountId nodeId : nodeAccountIds) {
            sigPairLists.add(SignatureMap.newBuilder());
            innerSignedTransactions.add(SignedTransaction.newBuilder()
                .setBodyBytes(Objects.requireNonNull(frozenBodyBuilder)
                    .setNodeAccountID(nodeId.toProtobuf())
                    .build()
                    .toByteString()
                ));
            outerTransactions.add(null);
        }
    }

    /**
     * Build all the transactions.
     */
    void buildAllTransactions() {
        transactionIds.setLocked(true);
        nodeAccountIds.setLocked(true);

        for (var i = 0; i < innerSignedTransactions.size(); ++i) {
            buildTransaction(i);
        }
    }

    /**
     * Will build the specific transaction at {@code index}
     * This function is only ever called after the transaction is frozen.
     *
     * @param index                     the index of the transaction to be built
     */
    void buildTransaction(int index) {
        // Check if transaction is already built.
        // Every time a signer is added via sign() or signWith(), all outerTransactions are nullified.
        if (
            outerTransactions.get(index) != null &&
                !outerTransactions.get(index).getSignedTransactionBytes().isEmpty()
        ) {
            return;
        }

        signTransaction(index);

        outerTransactions.set(index, com.hedera.hashgraph.sdk.proto.Transaction.newBuilder()
            .setSignedTransactionBytes(
                innerSignedTransactions.get(index)
                    .setSigMap(sigPairLists.get(index))
                    .build()
                    .toByteString()
            ).build());
    }

    private static boolean publicKeyIsInSigPairList(ByteString publicKeyBytes, List<SignaturePair> sigPairList) {
        for (var pair : sigPairList) {
            if (pair.getPubKeyPrefix().equals(publicKeyBytes)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Will sign the specific transaction at {@code index}
     * This function is only ever called after the transaction is frozen.
     *
     * @param index                     the index of the transaction to sign
     */
    void signTransaction(int index) {
        var bodyBytes = innerSignedTransactions.get(index).getBodyBytes().toByteArray();
        var thisSigPairList = sigPairLists.get(index).getSigPairList();

        for (var i = 0; i < publicKeys.size(); i++) {
            if (signers.get(i) == null) {
                continue;
            }
            if (publicKeyIsInSigPairList(ByteString.copyFrom(publicKeys.get(i).toBytesRaw()), thisSigPairList)) {
                continue;
            }

            var signatureBytes = signers.get(i).apply(bodyBytes);

            sigPairLists
                .get(index)
                .addSigPair(publicKeys.get(i).toSignaturePairProtobuf(signatureBytes));
        }
    }

    /**
     * Called in {@link #freezeWith(Client)} just before the transaction
     * body is built. The intent is for the derived class to assign
     * their data variant to the transaction body.
     */
    abstract void onFreeze(TransactionBody.Builder bodyBuilder);

    /**
     * Called in {@link #schedule()} when converting transaction into a scheduled version.
     */
    abstract void onScheduled(SchedulableTransactionBody.Builder scheduled);

    @Override
    final com.hedera.hashgraph.sdk.proto.Transaction makeRequest() {
        var index = nodeAccountIds.getIndex() + (transactionIds.getIndex() * nodeAccountIds.size());

        buildTransaction(index);

        return outerTransactions.get(index);
    }

    @Override
    TransactionResponse mapResponse(
        com.hedera.hashgraph.sdk.proto.TransactionResponse transactionResponse,
        AccountId nodeId,
        com.hedera.hashgraph.sdk.proto.Transaction request
    ) {
        var transactionId = Objects.requireNonNull(getTransactionIdInternal());
        var hash = hash(request.getSignedTransactionBytes().toByteArray());
        transactionIds.advance();
        return new TransactionResponse(nodeId, transactionId, hash, null);
    }

    @Override
    final Status mapResponseStatus(com.hedera.hashgraph.sdk.proto.TransactionResponse transactionResponse) {
        return Status.valueOf(transactionResponse.getNodeTransactionPrecheckCode());
    }

    abstract void validateChecksums(Client client) throws BadEntityIdException;

    /**
     * Prepare the transactions to be executed.
     *
     * @param client                    the configured client
     */
    void onExecute(Client client) {
        if (!isFrozen()) {
            freezeWith(client);
        }

        var accountId = Objects.requireNonNull(Objects.requireNonNull(transactionIds.get(0)).accountId);

        if (client.isAutoValidateChecksumsEnabled()) {
            try {
                accountId.validateChecksum(client);
                validateChecksums(client);
            } catch (BadEntityIdException exc) {
                throw new IllegalArgumentException(exc.getMessage());
            }
        }

        var operatorId = client.getOperatorAccountId();
        if (operatorId != null && operatorId.equals(accountId)) {
            // on execute, sign each transaction with the operator, if present
            // and we are signing a transaction that used the default transaction ID
            signWithOperator(client);
        }
    }

    @Override
    CompletableFuture<Void> onExecuteAsync(Client client) {
        onExecute(client);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    ExecutionState getExecutionState(Status status, com.hedera.hashgraph.sdk.proto.TransactionResponse response) {
        switch (status) {
            case TRANSACTION_EXPIRED:
                if ((regenerateTransactionId != null && !regenerateTransactionId) || transactionIds.isLocked()) {
                    return ExecutionState.REQUEST_ERROR;
                } else {
                    var firstTransactionId = Objects.requireNonNull(transactionIds.get(0));
                    var accountId = Objects.requireNonNull(firstTransactionId.accountId);
                    generateTransactionIds(TransactionId.generate(accountId), transactionIds.size());
                    wipeTransactionLists(transactionIds.size());
                    return ExecutionState.RETRY;
                }
            default:
                return super.getExecutionState(status, response);
        }
    }

    @Override
    @SuppressWarnings("LiteProtoToString")
    public String toString() {
        // NOTE: regex is for removing the instance address from the default debug output
        TransactionBody.Builder body = spawnBodyBuilder(null);

        if (!transactionIds.isEmpty()) {
            body.setTransactionID(transactionIds.get(0).toProtobuf());
        }
        if (!nodeAccountIds.isEmpty()) {
            body.setNodeAccountID(nodeAccountIds.get(0).toProtobuf());
        }

        onFreeze(body);

        return body.buildPartial().toString().replaceAll("@[A-Za-z0-9]+", "");
    }
}
