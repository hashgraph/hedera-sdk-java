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
import java8.util.concurrent.CompletableFuture;
import org.bouncycastle.util.encoders.Hex;
import org.threeten.bp.Duration;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

// Re-use the WithExecute interface that was generated for Executable
public class ContractCreateFlow implements WithExecute<TransactionResponse> {
    static final int FILE_CREATE_MAX_BYTES = 2048;

    private String bytecode = "";
    @Nullable
    private Key adminKey = null;
    private long gas = 0;
    private Hbar initialBalance = Hbar.ZERO;
    @Nullable
    private AccountId proxyAccountId = null;
    private int maxAutomaticTokenAssociations = 0;
    @Nullable
    private Duration autoRenewPeriod = null;
    private byte[] constructorParameters = {};
    @Nullable
    private String contractMemo = null;
    @Nullable
    private List<AccountId> nodeAccountIds = null;
    private String createBytecode = "";
    private String appendBytecode = "";

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
     * Extract the proxy account id.
     *
     * @return                          the proxy account id
     */
    @Nullable
    public AccountId getProxyAccountId() {
        return proxyAccountId;
    }

    /**
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
    public ContractCreateFlow setProxyAccountId(AccountId proxyAccountId) {
        Objects.requireNonNull(proxyAccountId);
        this.proxyAccountId = proxyAccountId;
        return this;
    }

    public int getMaxAutomaticTokenAssociations() {
        return maxAutomaticTokenAssociations;
    }

    public ContractCreateFlow setMaxAutomaticTokenAssociations(int maxAutomaticTokenAssociations) {
        Objects.requireNonNull(proxyAccountId);
        this.maxAutomaticTokenAssociations = maxAutomaticTokenAssociations;
        return this;
    }
  
    /**
     * Extract the auto renew period.
     *
     * @return                          the auto renew period
     */
    @Nullable
    public Duration getAutoRenewPeriod() {
        return autoRenewPeriod;
    }

    /**
     * Sets the period that the instance will charge its account every this many seconds to renew.
     *
     * @param autoRenewPeriod The Duration to be set for auto renewal
     * @return {@code this}
     */
    public ContractCreateFlow setAutoRenewPeriod(Duration autoRenewPeriod) {
        Objects.requireNonNull(autoRenewPeriod);
        this.autoRenewPeriod = autoRenewPeriod;
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
        this.constructorParameters = constructorParameters;
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
     * Extract the list of node account id's.
     *
     * @return                          the list of node account id's
     */
    @Nullable
    public List<AccountId> getNodeAccountIds() {
        return nodeAccountIds;
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
        this.nodeAccountIds = nodeAccountIds;
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
            .setMaxAutomaticTokenAssociations(maxAutomaticTokenAssociations);
        if (adminKey != null) {
            contractCreateTx.setAdminKey(adminKey);
        }
        if (proxyAccountId != null) {
            contractCreateTx.setProxyAccountId(proxyAccountId);
        }
        if (autoRenewPeriod != null) {
            contractCreateTx.setAutoRenewPeriod(autoRenewPeriod);
        }
        if (contractMemo != null) {
            contractCreateTx.setContractMemo(contractMemo);
        }
        if (nodeAccountIds != null) {
            contractCreateTx.setNodeAccountIds(nodeAccountIds);
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
     * Execute the transaction from the passed in client.
     *
     * @param client                    the client with the transaction to execute
     * @return                          the response
     * @throws PrecheckStatusException  when the precheck fails
     * @throws TimeoutException         when the transaction times out
     */
    @Override
    public TransactionResponse execute(Client client) throws PrecheckStatusException, TimeoutException {
        try {
            splitBytecode();
            var fileId = createFileCreateTransaction(client)
                .execute(client)
                .getReceipt(client)
                .fileId;
            Objects.requireNonNull(fileId);
            if (!appendBytecode.isEmpty()) {
                createFileAppendTransaction(fileId)
                    .execute(client);
            }
            var response = createContractCreateTransaction(fileId).execute(client);
            response.getReceipt(client);
            new FileDeleteTransaction()
                .setFileId(fileId)
                .execute(client);
            return response;
        } catch (ReceiptStatusException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompletableFuture<TransactionResponse> executeAsync(Client client) {
        splitBytecode();
        return createFileCreateTransaction(client).executeAsync(client).thenCompose(fileCreateResponse -> {
            return createTransactionReceiptQuery(fileCreateResponse)
                .executeAsync(client)
                .thenApply(receipt -> receipt.fileId);
        }).thenCompose(fileId -> {
            CompletableFuture<Void> appendFuture =  appendBytecode.isEmpty() ? CompletableFuture.completedFuture(null) :
                createFileAppendTransaction(fileId).executeAsync(client).thenApply(ignored -> null);
            return appendFuture.thenCompose(ignored -> {
                return createContractCreateTransaction(fileId).executeAsync(client).thenApply(contractCreateResponse -> {
                    contractCreateResponse.getReceiptAsync(client).thenRun(() -> {
                        new FileDeleteTransaction()
                            .setFileId(fileId)
                            .executeAsync(client);
                    });
                    return contractCreateResponse;
                });
            });
        });
    }
}
