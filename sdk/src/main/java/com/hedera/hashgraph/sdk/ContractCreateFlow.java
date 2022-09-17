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

import com.google.protobuf.ByteString;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java8.util.Lists;
import java8.util.concurrent.CompletableFuture;
import java8.util.function.BiConsumer;
import java8.util.function.Consumer;
import org.bouncycastle.util.encoders.Hex;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Start a new smart contract instance.
 * After the instance is created,
 * the ContractID for it is in the receipt.
 * <p>
 * The instance will exist for autoRenewPeriod seconds. When that is reached, it will renew itself for another
 * autoRenewPeriod seconds by charging its associated cryptocurrency account (which it creates here).
 * If it has insufficient cryptocurrency to extend that long, it will extend as long as it can.
 * If its balance is zero, the instance will be deleted.
 * <p>
 * A smart contract instance normally enforces rules, so "the code is law". For example, an
 * ERC-20 contract prevents a transfer from being undone without a signature by the recipient of the transfer.
 * This is always enforced if the contract instance was created with the adminKeys being null.
 * But for some uses, it might be desirable to create something like an ERC-20 contract that has a
 * specific group of trusted individuals who can act as a "supreme court" with the ability to override the normal
 * operation, when a sufficient number of them agree to do so. If adminKeys is not null, then they can
 * sign a transaction that can change the state of the smart contract in arbitrary ways, such as to reverse
 * a transaction that violates some standard of behavior that is not covered by the code itself.
 * The admin keys can also be used to change the autoRenewPeriod, and change the adminKeys field itself.
 * The API currently does not implement this ability. But it does allow the adminKeys field to be set and
 * queried, and will in the future implement such admin abilities for any instance that has a non-null adminKeys.
 * <p>
 * If this constructor stores information, it is charged gas to store it. There is a fee in hbars to
 * maintain that storage until the expiration time, and that fee is added as part of the transaction fee.
 * <p>
 * An entity (account, file, or smart contract instance) must be created in a particular realm.
 * If the realmID is left null, then a new realm will be created with the given admin key. If a new realm has
 * a null adminKey, then anyone can create/modify/delete entities in that realm. But if an admin key is given,
 * then any transaction to create/modify/delete an entity in that realm must be signed by that key,
 * though anyone can still call functions on smart contract instances that exist in that realm.
 * A realm ceases to exist when everything within it has expired and no longer exists.
 * <p>
 * The current API ignores shardID, realmID, and newRealmAdminKey, and creates everything in shard 0 and realm 0,
 * with a null key. Future versions of the API will support multiple realms and multiple shards.
 * <p>
 * The optional memo field can contain a string whose length is up to 100 bytes. That is the size after Unicode
 * NFD then UTF-8 conversion. This field can be used to describe the smart contract. It could also be used for
 * other purposes. One recommended purpose is to hold a hexadecimal string that is the SHA-384 hash of a
 * PDF file containing a human-readable legal contract. Then, if the admin keys are the
 * public keys of human arbitrators, they can use that legal document to guide their decisions during a binding
 * arbitration tribunal, convened to consider any changes to the smart contract in the future. The memo field can only
 * be changed using the admin keys. If there are no admin keys, then it cannot be
 * changed after the smart contract is created.
 */

// Re-use the WithExecute interface that was generated for Executable
public class ContractCreateFlow {
    static final int FILE_CREATE_MAX_BYTES = 2048;

    private String bytecode = "";
    @Nullable
    private Integer maxChunks = null;
    @Nullable
    private Key adminKey = null;
    private long gas = 0;
    private Hbar initialBalance = Hbar.ZERO;
    @Nullable
    private AccountId proxyAccountId = null;
    private int maxAutomaticTokenAssociations = 0;
    @Nullable
    private Duration autoRenewPeriod = null;
    @Nullable
    private AccountId autoRenewAccountId = null;
    private byte[] constructorParameters = {};
    @Nullable
    private String contractMemo = null;
    @Nullable
    private List<AccountId> nodeAccountIds = null;
    private String createBytecode = "";
    private String appendBytecode = "";

    @Nullable
    private AccountId stakedAccountId = null;

    @Nullable
    private Long stakedNodeId = null;

    private boolean declineStakingReward = false;

    public ContractCreateFlow() {
    }

    /**
     * Extract the hex-encoded bytecode of the contract.
     *
     * @return the hex-encoded bytecode of the contract.
     */
    public String getBytecode() {
        return bytecode;
    }

    /**
     * Sets the bytecode of the contract in hex.
     *
     * @param bytecode                  the string to assign
     * @return {@code this}
     */
    public ContractCreateFlow setBytecode(String bytecode) {
        Objects.requireNonNull(bytecode);
        this.bytecode = bytecode;
        return this;
    }

    /**
     * Sets the bytecode of the contract in raw bytes.
     *
     * @param bytecode                  the byte array
     * @return {@code this}
     */
    public ContractCreateFlow setBytecode(byte[] bytecode) {
        Objects.requireNonNull(bytecode);
        this.bytecode = Hex.toHexString(bytecode);
        return this;
    }

    /**
     * Sets the bytecode of the contract in raw bytes.
     *
     * @param bytecode                  the byte string
     * @return                          the contract in raw bytes
     */
    public ContractCreateFlow setBytecode(ByteString bytecode) {
        Objects.requireNonNull(bytecode);
        return setBytecode(bytecode.toByteArray());
    }

    @Nullable
    public Integer getMaxChunks() {
        return maxChunks;
    }

    public ContractCreateFlow setMaxChunks(int maxChunks) {
        this.maxChunks = maxChunks;
        return this;
    }

    /**
     * Extract the admin key.
     *
     * @return                          the admin key
     */
    @Nullable
    public Key getAdminKey() {
        return adminKey;
    }

    /**
     * Sets the state of the instance and its fields can be modified arbitrarily if this key signs a transaction
     * to modify it. If this is null, then such modifications are not possible, and there is no administrator
     * that can override the normal operation of this smart contract instance. Note that if it is created with no
     * admin keys, then there is no administrator to authorize changing the admin keys, so
     * there can never be any admin keys for that instance.
     *
     * @param adminKey The Key to be set
     * @return {@code this}
     */
    public ContractCreateFlow setAdminKey(Key adminKey) {
        Objects.requireNonNull(adminKey);
        this.adminKey = adminKey;
        return this;
    }

    /**
     * Extract the gas.
     *
     * @return                          the gas
     */
    public long getGas() {
        return gas;
    }

    /**
     * Sets the gas to run the constructor.
     *
     * @param gas The long to be set as gas
     * @return {@code this}
     */
    public ContractCreateFlow setGas(long gas) {
        this.gas = gas;
        return this;
    }

    /**
     * Extract the initial balance in hbar.
     *
     * @return                          the initial balance in hbar
     */
    public Hbar getInitialBalance() {
        return initialBalance;
    }

    /**
     * Sets the initial number of hbars to put into the cryptocurrency account
     * associated with and owned by the smart contract.
     *
     * @param initialBalance The Hbar to be set as the initial balance
     * @return {@code this}
     */
    public ContractCreateFlow setInitialBalance(Hbar initialBalance) {
        Objects.requireNonNull(initialBalance);
        this.initialBalance = initialBalance;
        return this;
    }

    /**
     * @deprecated with no replacement
     *
     * Extract the proxy account id.
     *
     * @return                          the proxy account id
     */
    @Nullable
    @Deprecated
    public AccountId getProxyAccountId() {
        return proxyAccountId;
    }

    /**
     * @deprecated with no replacement
     *
     * Sets the ID of the account to which this account is proxy staked.
     * <p>
     * If proxyAccountID is null, or is an invalid account, or is an account that isn't a node,
     * then this account is automatically proxy staked to a node chosen by the network, but without earning payments.
     * <p>
     * If the proxyAccountID account refuses to accept proxy staking , or if it is not currently running a node,
     * then it will behave as if  proxyAccountID was null.
     *
     * @param proxyAccountId The AccountId to be set
     * @return {@code this}
     */
    @Deprecated
    public ContractCreateFlow setProxyAccountId(AccountId proxyAccountId) {
        Objects.requireNonNull(proxyAccountId);
        this.proxyAccountId = proxyAccountId;
        return this;
    }

    public int getMaxAutomaticTokenAssociations() {
        return maxAutomaticTokenAssociations;
    }

    public ContractCreateFlow setMaxAutomaticTokenAssociations(int maxAutomaticTokenAssociations) {
        this.maxAutomaticTokenAssociations = maxAutomaticTokenAssociations;
        return this;
    }

    /**
     * Extract the auto renew period.
     *
     * @return                          the auto renew period
     */
    @Nullable
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "A Duration can't actually be mutated"
    )
    public Duration getAutoRenewPeriod() {
        return autoRenewPeriod;
    }

    /**
     * Sets the period that the instance will charge its account every this many seconds to renew.
     *
     * @param autoRenewPeriod The Duration to be set for auto renewal
     * @return {@code this}
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "A Duration can't actually be mutated"
    )
    public ContractCreateFlow setAutoRenewPeriod(Duration autoRenewPeriod) {
        Objects.requireNonNull(autoRenewPeriod);
        this.autoRenewPeriod = autoRenewPeriod;
        return this;
    }

    @Nullable
    public AccountId getAutoRenewAccountId() {
        return autoRenewAccountId;
    }

    public ContractCreateFlow setAutoRenewAccountId(AccountId autoRenewAccountId) {
        Objects.requireNonNull(autoRenewAccountId);
        this.autoRenewAccountId = autoRenewAccountId;
        return this;
    }

    /**
     * Extract the byte string representation.
     *
     * @return                          the byte string representation
     */
    public ByteString getConstructorParameters() {
        return ByteString.copyFrom(constructorParameters);
    }

    /**
     * Sets the constructor parameters as their raw bytes.
     * <p>
     * Use this instead of {@link #setConstructorParameters(ContractFunctionParameters)} if you have already
     * pre-encoded a solidity function call.
     *
     * @param constructorParameters The constructor parameters
     * @return {@code this}
     */
    public ContractCreateFlow setConstructorParameters(byte[] constructorParameters) {
        this.constructorParameters = Arrays.copyOf(constructorParameters, constructorParameters.length);
        return this;
    }

    /**
     * Sets the parameters to pass to the constructor.
     *
     * @param constructorParameters The contructor parameters
     * @return {@code this}
     */
    public ContractCreateFlow setConstructorParameters(ContractFunctionParameters constructorParameters) {
        Objects.requireNonNull(constructorParameters);
        return setConstructorParameters(constructorParameters.toBytes(null).toByteArray());
    }

    /**
     * Extract the contract memo.
     *
     * @return                          the contract memo
     */
    public String getContractMemo() {
        return contractMemo;
    }

    /**
     * Sets the memo to be associated with this contract.
     *
     * @param memo The String to be set as the memo
     * @return {@code this}
     */
    public ContractCreateFlow setContractMemo(String memo) {
        Objects.requireNonNull(memo);
        contractMemo = memo;
        return this;
    }

    /**
     * ID of the account to which this contract will stake
     *
     * @return ID of the account to which this contract will stake.
     */
    @Nullable
    public AccountId getStakedAccountId() {
        return stakedAccountId;
    }

    /**
     * Set the account to which this contract will stake
     *
     * @param stakedAccountId ID of the account to which this contract will stake.
     * @return {@code this}
     */
    public ContractCreateFlow setStakedAccountId(@Nullable AccountId stakedAccountId) {
        this.stakedAccountId = stakedAccountId;
        this.stakedNodeId = null;
        return this;
    }

    /**
     * The node to which this contract will stake
     *
     * @return ID of the node this contract will be staked to.
     */
    @Nullable
    public Long getStakedNodeId() {
        return stakedNodeId;
    }

    /**
     * Set the node to which this contract will stake
     *
     * @param stakedNodeId ID of the node this contract will be staked to.
     * @return {@code this}
     */
    public ContractCreateFlow setStakedNodeId(@Nullable Long stakedNodeId) {
        this.stakedNodeId = stakedNodeId;
        this.stakedAccountId = null;
        return this;
    }

    /**
     * If true, the contract declines receiving a staking reward. The default value is false.
     *
     * @return If true, the contract declines receiving a staking reward. The default value is false.
     */
    public boolean getDeclineStakingReward() {
        return declineStakingReward;
    }

    /**
     * If true, the contract declines receiving a staking reward. The default value is false.
     *
     * @param declineStakingReward - If true, the contract declines receiving a staking reward. The default value is false.
     * @return {@code this}
     */
    public ContractCreateFlow setDeclineStakingReward(boolean declineStakingReward) {
        this.declineStakingReward = declineStakingReward;
        return this;
    }

    /**
     * Extract the list of node account id's.
     *
     * @return                          the list of node account id's
     */
    @Nullable
    public List<AccountId> getNodeAccountIds() {
        return nodeAccountIds != null ? Collections.unmodifiableList(nodeAccountIds) : null;
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
    public ContractCreateFlow setNodeAccountIds(List<AccountId> nodeAccountIds) {
        Objects.requireNonNull(nodeAccountIds);
        this.nodeAccountIds = new ArrayList(nodeAccountIds);
        return this;
    }

    private void splitBytecode() {
        if(bytecode.length() > FILE_CREATE_MAX_BYTES) {
            createBytecode = bytecode.substring(0, FILE_CREATE_MAX_BYTES);
            appendBytecode = bytecode.substring(FILE_CREATE_MAX_BYTES);
        } else {
            createBytecode = bytecode;
            appendBytecode = "";
        }
    }

    private FileCreateTransaction createFileCreateTransaction(Client client) {
        var fileCreateTx = new FileCreateTransaction()
            .setKeys(Objects.requireNonNull(client.getOperatorPublicKey()))
            .setContents(createBytecode);
        if (nodeAccountIds != null) {
            fileCreateTx.setNodeAccountIds(nodeAccountIds);
        }
        return fileCreateTx;
    }

    private FileAppendTransaction createFileAppendTransaction(FileId fileId) {
        var fileAppendTx = new FileAppendTransaction()
            .setFileId(fileId)
            .setContents(appendBytecode);
        if (maxChunks != null) {
            fileAppendTx.setMaxChunks(maxChunks);
        }
        if (nodeAccountIds != null) {
            fileAppendTx.setNodeAccountIds(nodeAccountIds);
        }
        return fileAppendTx;
    }

    private ContractCreateTransaction createContractCreateTransaction(FileId fileId) {
        var contractCreateTx = new ContractCreateTransaction()
            .setBytecodeFileId(fileId)
            .setConstructorParameters(constructorParameters)
            .setGas(gas)
            .setInitialBalance(initialBalance)
            .setMaxAutomaticTokenAssociations(maxAutomaticTokenAssociations)
            .setDeclineStakingReward(declineStakingReward);
        if (adminKey != null) {
            contractCreateTx.setAdminKey(adminKey);
        }
        if (proxyAccountId != null) {
            contractCreateTx.setProxyAccountId(proxyAccountId);
        }
        if (autoRenewPeriod != null) {
            contractCreateTx.setAutoRenewPeriod(autoRenewPeriod);
        }
        if (autoRenewAccountId != null) {
            contractCreateTx.setAutoRenewAccountId(autoRenewAccountId);
        }
        if (contractMemo != null) {
            contractCreateTx.setContractMemo(contractMemo);
        }
        if (nodeAccountIds != null) {
            contractCreateTx.setNodeAccountIds(nodeAccountIds);
        }
        if (stakedAccountId != null) {
            contractCreateTx.setStakedAccountId(stakedAccountId);
        } else if (stakedNodeId != null) {
            contractCreateTx.setStakedNodeId(stakedNodeId);
        }
        return contractCreateTx;
    }

    /**
     * Create a new transaction receipt query.
     *
     * @param response                  the transaction response
     * @return                          the receipt query
     */
    TransactionReceiptQuery createTransactionReceiptQuery(TransactionResponse response) {
        return new TransactionReceiptQuery()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .setTransactionId(response.transactionId);
    }

    /**
     * Execute the transactions in the flow with the passed in client.
     *
     * @param client                    the client with the transaction to execute
     * @return                          the response
     * @throws PrecheckStatusException  when the precheck fails
     * @throws TimeoutException         when the transaction times out
     */
    public TransactionResponse execute(Client client) throws PrecheckStatusException, TimeoutException {
        return execute(client, client.getRequestTimeout());
    }

    /**
     * Execute the transactions in the flow with the passed in client.
     *
     * @param client                    the client with the transaction to execute
     * @param timeoutPerTransaction The timeout after which each transaction's execution attempt will be cancelled.
     * @return                          the response
     * @throws PrecheckStatusException  when the precheck fails
     * @throws TimeoutException         when the transaction times out
     */
    public TransactionResponse execute(Client client, Duration timeoutPerTransaction) throws PrecheckStatusException, TimeoutException {
        try {
            splitBytecode();
            var fileId = createFileCreateTransaction(client)
                .execute(client, timeoutPerTransaction)
                .getReceipt(client, timeoutPerTransaction)
                .fileId;
            Objects.requireNonNull(fileId);
            if (!appendBytecode.isEmpty()) {
                createFileAppendTransaction(fileId)
                    .execute(client, timeoutPerTransaction);
            }
            var response = createContractCreateTransaction(fileId).execute(client, timeoutPerTransaction);
            response.getReceipt(client, timeoutPerTransaction);
            new FileDeleteTransaction()
                .setFileId(fileId)
                .execute(client, timeoutPerTransaction);
            return response;
        } catch (ReceiptStatusException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Execute the transactions in the flow with the passed in client asynchronously.
     *
     * @param client                    the client with the transaction to execute
     * @return                          the response
     */
    public CompletableFuture<TransactionResponse> executeAsync(Client client) {
        return executeAsync(client, client.getRequestTimeout());
    }

    /**
     * Execute the transactions in the flow with the passed in client asynchronously.
     *
     * @param client                    the client with the transaction to execute
     * @param timeoutPerTransaction The timeout after which each transaction's execution attempt will be cancelled.
     * @return                          the response
     */
    public CompletableFuture<TransactionResponse> executeAsync(Client client, Duration timeoutPerTransaction) {
        splitBytecode();
        return createFileCreateTransaction(client).executeAsync(client, timeoutPerTransaction).thenCompose(fileCreateResponse -> {
            return createTransactionReceiptQuery(fileCreateResponse)
                .executeAsync(client, timeoutPerTransaction)
                .thenApply(receipt -> receipt.fileId);
        }).thenCompose(fileId -> {
            CompletableFuture<Void> appendFuture =  appendBytecode.isEmpty() ? CompletableFuture.completedFuture(null) :
                createFileAppendTransaction(fileId).executeAsync(client, timeoutPerTransaction).thenApply(ignored -> null);
            return appendFuture.thenCompose(ignored -> {
                return createContractCreateTransaction(fileId).executeAsync(client, timeoutPerTransaction).thenApply(contractCreateResponse -> {
                    createTransactionReceiptQuery(contractCreateResponse).executeAsync(client, timeoutPerTransaction).thenRun(() -> {
                        new FileDeleteTransaction()
                            .setFileId(fileId)
                            .executeAsync(client, timeoutPerTransaction);
                    });
                    return contractCreateResponse;
                });
            });
        });
    }

    /**
     * Execute the transactions in the flow with the passed in client asynchronously.
     *
     * @param client                    the client with the transaction to execute
     * @param callback a BiConsumer which handles the result or error.
     */
    public void executeAsync(Client client, BiConsumer<TransactionResponse, Throwable> callback) {
        ConsumerHelper.biConsumer(executeAsync(client), callback);
    }

    /**
     * Execute the transactions in the flow with the passed in client asynchronously.
     *
     * @param client                    the client with the transaction to execute
     * @param timeoutPerTransaction The timeout after which each transaction's execution attempt will be cancelled.
     * @param callback a BiConsumer which handles the result or error.
     */
    public void executeAsync(Client client, Duration timeoutPerTransaction, BiConsumer<TransactionResponse, Throwable> callback) {
        ConsumerHelper.biConsumer(executeAsync(client, timeoutPerTransaction), callback);
    }

    /**
     * Execute the transactions in the flow with the passed in client asynchronously.
     *
     * @param client                    the client with the transaction to execute
     * @param onSuccess a Consumer which consumes the result on success.
     * @param onFailure a Consumer which consumes the error on failure.
     */
    public void executeAsync(Client client, Consumer<TransactionResponse> onSuccess, Consumer<Throwable> onFailure) {
        ConsumerHelper.twoConsumers(executeAsync(client), onSuccess, onFailure);
    }

    /**
     * Execute the transactions in the flow with the passed in client asynchronously.
     *
     * @param client                    the client with the transaction to execute
     * @param timeoutPerTransaction The timeout after which each transaction's execution attempt will be cancelled.
     * @param onSuccess a Consumer which consumes the result on success.
     * @param onFailure a Consumer which consumes the error on failure.
     */
    public void executeAsync(Client client, Duration timeoutPerTransaction, Consumer<TransactionResponse> onSuccess, Consumer<Throwable> onFailure) {
        ConsumerHelper.twoConsumers(executeAsync(client, timeoutPerTransaction), onSuccess, onFailure);
    }
}
