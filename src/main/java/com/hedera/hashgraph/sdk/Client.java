package com.hedera.hashgraph.sdk;

import com.google.gson.Gson;
import com.hedera.hashgraph.sdk.account.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.account.AccountInfo;
import com.hedera.hashgraph.sdk.account.AccountInfoQuery;
import com.hedera.hashgraph.sdk.crypto.PrivateKey;
import com.hedera.hashgraph.sdk.crypto.PublicKey;
import com.hedera.hashgraph.sdk.crypto.TransactionSigner;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PublicKey;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;

/**
 * The Hedera protocol wrapper for the SDK, used by all transaction and query types.
 *
 * <h3>Note: AutoCloseable</h3>
 * The gRPC channels used by this client must be explicitly shutdown before it is garbage
 * collected or error messages will be printed to the console. These messages are emitted by the
 * gRPC library itself; the SDK has no direct control over this.
 * <p>
 * These error messages are printed because the runtime may exit with pending calls if you do not
 * shutdown the channels properly which allows them to finish any pending calls.
 * <p>
 * To shutdown channels, this class implements {@link AutoCloseable} (allowing use
 * with the try-with-resources syntax) which waits at most a few seconds for channels to finish
 * their calls and terminate.
 * <p>
 * Alternatively, you may call {@link #close(long, TimeUnit)} with a custom timeout.
 * <p>
 * This is only necessary when you're completely finished with the Client; it can and should be
 * reused between multiple queries and transactions. The Client may not be reused once its
 * channels have been shut down.
 */
public final class Client implements AutoCloseable {
    final Random random = new Random();
    private Map<AccountId, Node> nodes;

    static final long DEFAULT_MAX_TXN_FEE = 100_000_000; // 1 hbar

    // todo: transaction fees should be defaulted to whatever the transaction fee schedule is
    private long maxTransactionFee = DEFAULT_MAX_TXN_FEE;

    // also 1 hbar
    private long maxQueryPayment = 100_000_000;

    @Nullable
    private AccountId operatorId;

    @Nullable
    private PublicKey operatorPublicKey;

    @Nullable
    private TransactionSigner operatorSigner;

    public Client(Map<AccountId, String> nodes) {
        if (nodes.isEmpty()) {
            throw new IllegalArgumentException("List of nodes must not be empty");
        }

        this.nodes = nodes.entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, t -> new Node(t.getKey(), t.getValue())));
    }

    /**
     * Get a Client configured for Hedera mainnet access.
     * <p>
     * Most users will also want to set an operator account with
     * {@link #setOperator(AccountId, PrivateKey)} so transactions can be automatically
     * given {@link TransactionId}s and signed.
     *
     * @return a Client configured for Hedera mainnet access
     */
    public static Client forMainnet() {
        // connect to all known nodes
        final HashMap<AccountId, String> nodes = new HashMap<>();
        nodes.put(new AccountId(3), "35.237.200.180:50211");
        nodes.put(new AccountId(4), "35.186.191.247:50211");
        nodes.put(new AccountId(5), "35.192.2.25:50211");
        nodes.put(new AccountId(6), "35.199.161.108:50211");
        nodes.put(new AccountId(7), "35.203.82.240:50211");
        nodes.put(new AccountId(8), "35.236.5.219:50211");
        nodes.put(new AccountId(9), "35.197.192.225:50211");
        nodes.put(new AccountId(10), "35.242.233.154:50211");
        nodes.put(new AccountId(11), "35.240.118.96:50211");
        nodes.put(new AccountId(12), "35.204.86.32:50211");

        return new Client(nodes);
    }

    /**
     * Get a Client configured for Hedera public testnet access.
     * <p>
     * Most users will also want to set an operator account with
     * {@link #setOperator(AccountId, PrivateKey)} so transactions can be automatically
     * given {@link TransactionId}s and signed.
     *
     * @return a Client configured for Hedera testnet access
     */
    public static Client forTestnet() {
        final HashMap<AccountId, String> nodes = new HashMap<>();
        nodes.put(new AccountId(3), "0.testnet.hedera.com:50211");
        nodes.put(new AccountId(4), "1.testnet.hedera.com:50211");
        nodes.put(new AccountId(5), "2.testnet.hedera.com:50211");
        nodes.put(new AccountId(6), "3.testnet.hedera.com:50211");

        return new Client(nodes);
    }

    /**
     * Configure a client based off the given JSON string.
     *
     * @param json
     * @return
     */
    public static Client fromJson(String json) {
        return fromJson(new StringReader(json));
    }

    /**
     * Configure a client based off the given JSON reader.
     *
     * @param json
     * @return
     */
    public static Client fromJson(Reader json) {
        Config config = new Gson().fromJson(json, Config.class);

        Map<AccountId, String> nodes = config.network.entrySet().stream()
            .collect(Collectors.toMap(entry -> AccountId.fromString(entry.getKey()), Map.Entry::getValue));

        final Client client = new Client(nodes);

        if (config.operator != null) {
            final AccountId operatorAccount = AccountId.fromString(config.operator.accountId);
            final Ed25519PrivateKey privateKey = Ed25519PrivateKey.fromString(config.operator.privateKey);

            client.setOperator(operatorAccount, privateKey);
        }

        return client;
    }

    /**
     * Configure a client based on a JSON file at the given path.
     *
     * @param fileName
     * @return
     * @throws FileNotFoundException
     */
    public static Client fromFile(String fileName) throws FileNotFoundException {
        return fromFile(new File(fileName));
    }

    /**
     * Configure a client based on a JSON file.
     *
     * @param file
     * @return
     * @throws FileNotFoundException
     */
    public static Client fromFile(File file) throws FileNotFoundException {
        return fromJson(new FileReader(file));
    }

    /**
     * Replace all nodes in this Client with a new set of nodes (e.g. for an Address Book update).
     * <p>
     * If a node URL for a given account ID is the same, it is not replaced.
     *
     * @param nodes a map of node account ID to node URL.
     * @return {@code this} for fluent API usage.
     */
    public Client replaceNodes(Map<AccountId, String> nodes) {
        this.nodes.replaceAll((nodeAcct, node) -> {
            String newNodeUrl = nodes.get(nodeAcct);

            // node hasn't changed
            if (node.address.equals(newNodeUrl)) {
                return node;
            }

            // close node channel
            node.closeChannel();

            // replace node
            if (newNodeUrl != null) {
                return new Node(nodeAcct, newNodeUrl);
            }

            // make null for removal
            return null;
        });

        // remove
        this.nodes.values().removeAll(Collections.singleton(null));

        // add new nodes
        for (Map.Entry<AccountId, String> node : nodes.entrySet()) {
            this.nodes.put(node.getKey(), new Node(node.getKey(), node.getValue()));
        }

        return this;
    }

    /**
     * Set the maximum fee to be paid for transactions executed by this client.
     * <p>
     * Because transaction fees are always maximums, this will simply add a call to
     * {@link TransactionBuilder#setMaxTransactionFee(long)} on every new transaction. The actual
     * fee assessed for a given transaction may be less than this value, but never greater.
     *
     * @param maxTransactionFee
     * @return {@code this} for fluent usage.
     */
    public Client setMaxTransactionFee(Hbar maxTransactionFee) {
        return setMaxTransactionFee(maxTransactionFee.asTinybar());
    }

    /**
     * Set the maximum fee to be paid for transactions executed by this client.
     * <p>
     * Because transaction fees are always maximums, this will simply add a call to
     * {@link TransactionBuilder#setMaxTransactionFee(long)} on every new transaction. The actual
     * fee assessed for a given transaction may be less than this value, but never greater.
     *
     * @param maxTransactionFee
     * @return {@code this} for fluent usage.
     */
    public Client setMaxTransactionFee(@Nonnegative long maxTransactionFee) {
        if (maxTransactionFee <= 0) {
            throw new IllegalArgumentException("maxTransactionFee must be > 0");
        }

        this.maxTransactionFee = maxTransactionFee;
        return this;
    }

    /**
     * Set the maximum default payment allowable for queries.
     * <p>
     * When a query is executed without an explicit
     * {@link QueryBuilder#setPaymentTransaction(Transaction)}
     * or {@link QueryBuilder#setQueryPayment(Hbar)} call, the client will first request the cost
     * of the given query from the node it will be submitted to and attach a payment for that amount
     * from the operator account on the client.
     * <p>
     * If the returned value is greater than this value, a
     * {@link MaxQueryPaymentExceededException} will be thrown from
     * {@link QueryBuilder#execute(Client)} or returned in the second callback of
     * {@link QueryBuilder#executeAsync(Client, Consumer, Consumer)}.
     * <p>
     * Set to 0 to disable automatic implicit payments.
     *
     * @param maxQueryPayment
     * @return
     */
    public Client setMaxQueryPayment(Hbar maxQueryPayment) {
        return setMaxQueryPayment(maxQueryPayment.asTinybar());
    }

    /**
     * Set the maximum default payment, in tinybar, allowable for queries.
     * <p>
     * When a query is executed without an explicit
     * {@link QueryBuilder#setPaymentTransaction(Transaction)}
     * or {@link QueryBuilder#setQueryPayment(Hbar)} call, the client will first request the cost
     * of the given query from the node it will be submitted to and attach a payment for that amount
     * from the operator account on the client.
     * <p>
     * If the returned value is greater than this value, a
     * {@link MaxQueryPaymentExceededException} will be thrown from
     * {@link QueryBuilder#execute(Client)} or returned in the second callback of
     * {@link QueryBuilder#executeAsync(Client, Consumer, Consumer)}.
     * <p>
     * Set to 0 to disable automatic implicit payments.
     *
     * @param maxQueryPayment
     * @return
     */
    public Client setMaxQueryPayment(@Nonnegative long maxQueryPayment) {
        if (maxQueryPayment <= 0) {
            throw new IllegalArgumentException("maxQueryPayment must be > 0");
        }

        this.maxQueryPayment = maxQueryPayment;
        return this;
    }

    /**
     * Set the account that will, by default, be paying for transactions and queries built with
     * this client, and the key with which to automatically sign transactions.
     */
    public Client setOperator(AccountId operatorId, PrivateKey<? extends PublicKey> operatorKey) {
        this.operatorId = operatorId;
        this.operatorPublicKey = operatorKey.publicKey;
        this.operatorSigner = operatorKey::sign;

        return this;
    }

    /**
     * Set the account that will, by default, be paying for transactions and queries built with
     * this client, and a callback that will be invoked when a transaction needs to be signed.
     *
     * Currently only {@link Ed25519PublicKey} is allowed.
     *
     * @see TransactionSigner
     */
    public Client setOperatorWith(AccountId accountId, PublicKey publicKey, TransactionSigner signer) {
        this.operatorId = accountId;
        this.operatorPublicKey = publicKey;
        this.operatorSigner = signer;

        return this;
    }

    public long getMaxTransactionFee() {
        return maxTransactionFee;
    }

    public long getMaxQueryPayment() {
        return maxQueryPayment;
    }

    @Nullable
    AccountId getOperatorId() {
        return operatorId;
    }

    @Nullable
    PublicKey getOperatorPublicKey() {
        return operatorPublicKey;
    }

    @Nullable
    TransactionSigner getOperatorSigner() {
        return operatorSigner;
    }

    Node pickNode() {
        if (nodes.isEmpty()) {
            throw new IllegalStateException("List of channels has become empty");
        }

        int r = random.nextInt(nodes.size());
        Iterator<Node> channelIter = nodes.values()
            .iterator();

        for (int i = 0; i < r; i++) {
            channelIter.next();
        }

        return channelIter.next();
    }

    Node getNodeForId(AccountId node) {
        Node selectedChannel = nodes.get(node);

        if (selectedChannel == null) {
            throw new IllegalArgumentException("Node Id does not exist");
        }

        return selectedChannel;
    }

    @Deprecated
    public AccountInfo getAccount(AccountId id) throws HederaStatusException, HederaNetworkException {
        return new AccountInfoQuery()
            .setAccountId(id)
            .execute(this);
    }

    @Deprecated
    public void getAccountAsync(AccountId id, Consumer<AccountInfo> onSuccess, Consumer<HederaThrowable> onError) {
        new AccountInfoQuery()
            .setAccountId(id)
            .executeAsync(this, onSuccess, onError);
    }

    @Deprecated
    public Hbar getAccountBalance(AccountId id) throws HederaStatusException, HederaNetworkException {
        return new AccountBalanceQuery()
            .setAccountId(id)
            .execute(this);
    }

    @Deprecated
    public void getAccountBalanceAsync(AccountId id, Consumer<Hbar> onSuccess, Consumer<HederaThrowable> onError) {
        new AccountBalanceQuery()
            .setAccountId(id)
            .executeAsync(this, onSuccess, onError);
    }

    /**
     * Waits 10 seconds for all channels to finish their calls to their respective nodes.
     * <p>
     * Any new transactions or queries executed with this client after this call will return an
     * error.
     * <p>
     * If you want to adjust the timeout, use {@link #close(long, TimeUnit)} instead.
     *
     * @throws InterruptedException if the thread is interrupted during shutdown.
     * @throws TimeoutException     if 10 seconds elapses before the channels are closed.
     */
    @Override
    public void close() throws InterruptedException, TimeoutException {
        close(10, TimeUnit.SECONDS);
    }

    /**
     * Wait for all channels to finish their calls to their respective nodes.
     * <p>
     * Any new transactions or queries executed with this client after this call will return an
     * error.
     *
     * @param timeout  the timeout amount for the entire shutdown operation (not per channel).
     * @param timeUnit the unit of the timeout amount.
     * @throws InterruptedException if the thread is interrupted during shutdown.
     * @throws TimeoutException     if the timeout elapses before all channels are shutdown.
     */
    public void close(long timeout, TimeUnit timeUnit) throws InterruptedException, TimeoutException {
        final long startMs = System.currentTimeMillis();
        final long timeoutAtMs = startMs + timeUnit.toMillis(timeout);

        // go through and initiate shutdown for all channels; this shouldn't block
        for (final Node node : nodes.values()) {
            node.closeChannel();
        }

        // wait for all nodes to shutdown
        for (final Node node : nodes.values()) {
            if (timeoutAtMs <= System.currentTimeMillis()) {
                throw new TimeoutException("Hedera Client timed out waiting for all node channels to shutdown");
            }

            final long nextTimeoutMs = timeoutAtMs - System.currentTimeMillis();

            // this also calls `.shutdown()` which should be safe to call multiple times
            node.awaitChannelTermination(nextTimeoutMs, TimeUnit.MILLISECONDS);
        }
    }

    private static class Config {
        private HashMap<String, String> network;
        @Nullable
        private Operator operator;

        private static class Operator {
            private String accountId;
            private String privateKey;
        }
    }
}
