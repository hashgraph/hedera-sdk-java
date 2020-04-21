package com.hedera.hashgraph.sdk;

import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.errorprone.annotations.Var;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java8.util.function.Consumer;
import java8.util.function.Function;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Managed client for use on the Hedera Hashgraph network.
 */
public final class Client implements AutoCloseable {
    private static final Hbar DEFAULT_MAX_QUERY_PAYMENT = new Hbar(1);

    private static final Hbar DEFAULT_MAX_TRANSACTION_FEE = new Hbar(1);

    final ExecutorService executor;

    private final Iterator<AccountId> nodes;

    private final Map<AccountId, String> network;

    private Map<AccountId, ManagedChannel> channels;

    Hbar maxTransactionFee = DEFAULT_MAX_QUERY_PAYMENT;

    Hbar maxQueryPayment = DEFAULT_MAX_TRANSACTION_FEE;

    @Nullable
    private Operator operator;

    Client(Map<AccountId, String> network) {
        var threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("hedera-sdk-%d")
            .setDaemon(true)
            .build();

        this.executor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            threadFactory);

        this.network = network;
        this.channels = new HashMap<>(network.size());

        // Take all given node account IDs, shuffle, and prepare an infinite iterator for use in [getNextNodeId]
        var allNodes = new ArrayList<>(network.keySet());
        Collections.shuffle(allNodes, ThreadLocalSecureRandom.current());
        nodes = Iterables.cycle(allNodes).iterator();
    }

    /**
     * Construct a client given a set of nodes.
     *
     * <p>It is the responsibility of the caller to ensure that all nodes in the map are part of the
     * same Hedera network. Failure to do so will result in undefined behavior.
     *
     * <p>The client will load balance all requests to Hedera using a simple round-robin scheme to
     * chose nodes to send transactions to. For one transaction, at most 1/3 of the nodes will be
     * tried.
     *
     * @param network the map of node IDs to node addresses that make up the network.
     */
    public static Client forNetwork(Map<AccountId, String> network) {
        return new Client(network);
    }

    /**
     * Construct a Hedera client pre-configured for <a
     * href="https://docs.hedera.com/guides/mainnet/address-book#mainnet-address-book">Mainnet
     * access</a>.
     */
    public static Client forMainnet() {
        var network = new HashMap<AccountId, String>();
        network.put(new AccountId(3), "35.237.200.180:50211");
        network.put(new AccountId(4), "35.186.191.247:50211");
        network.put(new AccountId(5), "35.192.2.25:50211");
        network.put(new AccountId(6), "35.199.161.108:50211");
        network.put(new AccountId(7), "35.203.82.240:50211");
        network.put(new AccountId(8), "35.236.5.219:50211");
        network.put(new AccountId(9), "35.197.192.225:50211");
        network.put(new AccountId(10), "35.242.233.154:50211");
        network.put(new AccountId(11), "35.240.118.96:50211");
        network.put(new AccountId(12), "35.204.86.32:50211");

        return Client.forNetwork(network);
    }

    /**
     * Construct a Hedera client pre-configured for <a
     * href="https://docs.hedera.com/guides/testnet/nodes">Testnet access</a>.
     */
    public static Client forTestnet() {
        var network = new HashMap<AccountId, String>();
        network.put(new AccountId(3), "0.testnet.hedera.com:50211");
        network.put(new AccountId(4), "1.testnet.hedera.com:50211");
        network.put(new AccountId(5), "2.testnet.hedera.com:50211");
        network.put(new AccountId(6), "3.testnet.hedera.com:50211");

        return Client.forNetwork(network);
    }

    /**
     * Set the account that will, by default, be paying for transactions and queries built with
     * this client.
     * <p>
     * The operator account ID is used to generate the default transaction ID for all transactions executed with
     * this client.
     * <p>
     * The operator private key is used to sign all transactions executed by this client.
     *
     * @return {@code this}.
     */
    public Client setOperator(AccountId accountId, PrivateKey privateKey) {
        return setOperatorWith(accountId, privateKey.getPublicKey(), privateKey::sign);
    }

    public Client setOperatorWith(AccountId accountId, PublicKey publicKey, Function<byte[], byte[]> transactionSigner) {
        this.operator = new Operator(accountId, publicKey, transactionSigner);
        return this;
    }

    /**
     * Set the maximum fee to be paid for transactions executed by this client.
     * <p>
     * Because transaction fees are always maximums, this will simply add a call to
     * {@link TransactionBuilder#setMaxTransactionFee(Hbar)} on every new transaction. The actual
     * fee assessed for a given transaction may be less than this value, but never greater.
     *
     * @return {@code this}.
     */
    public Client setMaxTransactionFee(Hbar maxTransactionFee) {
        if (maxTransactionFee.asTinybar() < 0) {
            throw new IllegalArgumentException("maxTransactionFee must be non-negative");
        }

        this.maxTransactionFee = maxTransactionFee;
        return this;
    }

    /**
     * Set the maximum default payment allowable for queries.
     * <p>
     * When a query is executed without an explicit {@link QueryBuilder#setQueryPayment(Hbar)} call,
     * the client will first request the cost
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
     * @return {@code this}.
     */
    public Client setMaxQueryPayment(Hbar maxQueryPayment) {
        if (maxQueryPayment.asTinybar() < 0) {
            throw new IllegalArgumentException("maxQueryPayment must be non-negative");
        }

        this.maxQueryPayment = maxQueryPayment;
        return this;
    }

    @Nullable
    Operator getOperator() {
        return this.operator;
    }

    /**
     * Initiates an orderly shutdown of all channels (to the Hedera network) in which preexisting
     * transactions or queries continue but more would be immediately cancelled.
     *
     * <p>After this method returns, this client can be re-used. Channels will be re-established as
     * needed.
     */
    @Override
    public synchronized void close() {
        var channels = this.channels;
        this.channels = new HashMap<>(network.size());

        // initialize shutdown for all channels
        // this should not block
        for (var channel : channels.values()) {
            channel.shutdown();
        }

        // wait for all channels to shutdown
        for (var channel : channels.values()) {
            try {
                channel.awaitTermination(0, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // preemptively clear memory for the channels map
        channels.clear();
    }

    // Get the next node ID, following a round-robin distribution with a randomized start point
    synchronized AccountId getNextNodeId() {
        return nodes.next();
    }

    int getNumberOfNodesForTransaction() {
        return (network.size() + 3 - 1) / 3;
    }

    // Return or establish a channel for a given node ID
    synchronized ManagedChannel getChannel(AccountId nodeId) {
        @Var var channel = channels.get(nodeId);

        if (channel != null) {
            return channel;
        }

        var address = network.get(nodeId);

        // Build a user agent that species our SDK version for Hedera
        var thePackage = getClass().getPackage();
        var implementationVersion = thePackage != null ? thePackage.getImplementationVersion() : null;
        var userAgent = "hedera-sdk-java/" + ((implementationVersion != null) ? ("v" + implementationVersion) : "DEV");

        channel = ManagedChannelBuilder.forTarget(address)
            .usePlaintext()
            .userAgent(userAgent)
            .executor(executor)
            .build();

        channels.put(nodeId, channel);

        return channel;
    }

    static class Operator {
        final AccountId accountId;
        final PublicKey publicKey;
        final Function<byte[], byte[]> transactionSigner;

        Operator(AccountId accountId, PublicKey publicKey, Function<byte[], byte[]> transactionSigner) {
            this.accountId = accountId;
            this.publicKey = publicKey;
            this.transactionSigner = transactionSigner;
        }
    }
}
