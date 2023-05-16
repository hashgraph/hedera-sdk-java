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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import java.time.Instant;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static com.hedera.hashgraph.sdk.BaseNodeAddress.PORT_NODE_PLAIN;
import static com.hedera.hashgraph.sdk.BaseNodeAddress.PORT_NODE_TLS;

/**
 * Managed client for use on the Hedera Hashgraph network.
 */
public final class Client implements AutoCloseable {
    static final int DEFAULT_MAX_ATTEMPTS = 10;
    static final Duration DEFAULT_MAX_BACKOFF = Duration.ofSeconds(8L);
    static final Duration DEFAULT_MIN_BACKOFF = Duration.ofMillis(250L);
    static final Duration DEFAULT_MAX_NODE_BACKOFF = Duration.ofHours(1L);
    static final Duration DEFAULT_MIN_NODE_BACKOFF = Duration.ofSeconds(8L);
    static final Duration DEFAULT_CLOSE_TIMEOUT = Duration.ofSeconds(30L);
    static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofMinutes(2L);
    static final Duration DEFAULT_GRPC_DEADLINE = Duration.ofSeconds(10L);
    static final Duration DEFAULT_NETWORK_UPDATE_PERIOD = Duration.ofHours(24);
    // Initial delay of 10 seconds before we update the network for the first time,
    // so that this doesn't happen in unit tests.
    static final Duration NETWORK_UPDATE_INITIAL_DELAY = Duration.ofSeconds(10);

    /**
     * The logger
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final Hbar DEFAULT_MAX_QUERY_PAYMENT = new Hbar(1);

    private static final String MAINNET = "mainnet";
    private static final String TESTNET = "testnet";
    private static final String PREVIEWNET = "previewnet";

    final ExecutorService executor;

    @Nullable
    Hbar defaultMaxTransactionFee = null;
    Hbar defaultMaxQueryPayment = DEFAULT_MAX_QUERY_PAYMENT;

    Network network;
    MirrorNetwork mirrorNetwork;

    @Nullable
    private Operator operator;

    private Duration requestTimeout = DEFAULT_REQUEST_TIMEOUT;
    private Duration closeTimeout = DEFAULT_CLOSE_TIMEOUT;
    private final AtomicReference<Duration> grpcDeadline = new AtomicReference(DEFAULT_GRPC_DEADLINE);

    private int maxAttempts = DEFAULT_MAX_ATTEMPTS;

    private volatile Duration maxBackoff = DEFAULT_MAX_BACKOFF;

    private volatile Duration minBackoff = DEFAULT_MIN_BACKOFF;

    private boolean autoValidateChecksums = false;

    private boolean defaultRegenerateTransactionId = true;

    // If networkUpdatePeriod is null, any network updates in progress will not complete
    @Nullable
    private Duration networkUpdatePeriod;

    @Nullable
    private CompletableFuture<Void> networkUpdateFuture;

    private Set<SubscriptionHandle> subscriptions = ConcurrentHashMap.newKeySet();

    /**
     * Constructor.
     *
     * @param executor                   the executor
     * @param network                    the network
     * @param mirrorNetwork              the mirror network
     */
    @VisibleForTesting
    Client(
        ExecutorService executor,
        Network network,
        MirrorNetwork mirrorNetwork,
        @Nullable Duration networkUpdateInitialDelay,
        @Nullable Duration networkUpdatePeriod
    ) {
        this.executor = executor;
        this.network = network;
        this.mirrorNetwork = mirrorNetwork;
        this.networkUpdatePeriod = networkUpdatePeriod;
        scheduleNetworkUpdate(networkUpdateInitialDelay);
    }

    /**
     * Extract the executor.
     *
     * @return                          the executor service
     */
    static ExecutorService createExecutor() {
        var threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("hedera-sdk-%d")
            .setDaemon(true)
            .build();

        int nThreads = Runtime.getRuntime().availableProcessors();
        return new ThreadPoolExecutor(nThreads, nThreads,
                        0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<>(),
                        threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * Set the mirror network nodes.
     *
     * @param network                   list of network nodes
     * @return                          {@code this}
     * @throws InterruptedException     when a thread is interrupted while it's waiting, sleeping, or otherwise occupied
     */
    public synchronized Client setMirrorNetwork(List<String> network) throws InterruptedException {
        try {
            this.mirrorNetwork.setNetwork(network);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    /**
     * Extract the mirror network node list.
     *
     * @return                          the list of mirror nodes
     */
    synchronized public List<String> getMirrorNetwork() {
        return mirrorNetwork.getNetwork();
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
     * @param networkMap the map of node IDs to node addresses that make up the network.
     * @return {@link com.hedera.hashgraph.sdk.Client}
     */
    public static Client forNetwork(Map<String, AccountId> networkMap) {
        var executor = createExecutor();
        var network = Network.forNetwork(executor, networkMap);
        var mirrorNetwork = MirrorNetwork.forNetwork(executor, new ArrayList<>());

        return new Client(executor, network, mirrorNetwork, null, null);
    }

    /**
     * Set up the client for the selected network.
     *
     * @param name                      the selected network
     * @return                          the configured client
     */
    public static Client forName(String name) {
        return switch (name) {
            case MAINNET -> Client.forMainnet();
            case TESTNET -> Client.forTestnet();
            case PREVIEWNET -> Client.forPreviewnet();
            default -> throw new IllegalArgumentException("Name must be one-of `mainnet`, `testnet`, or `previewnet`");
        };
    }

    /**
     * Construct a Hedera client pre-configured for <a
     * href="https://docs.hedera.com/guides/mainnet/address-book#mainnet-address-book">Mainnet
     * access</a>.
     *
     * @return {@link com.hedera.hashgraph.sdk.Client}
     */
    public static Client forMainnet() {
        var executor = createExecutor();
        var network = Network.forMainnet(executor);
        var mirrorNetwork = MirrorNetwork.forMainnet(executor);

        return new Client(executor, network, mirrorNetwork, NETWORK_UPDATE_INITIAL_DELAY, DEFAULT_NETWORK_UPDATE_PERIOD);
    }

    /**
     * Construct a Hedera client pre-configured for <a
     * href="https://docs.hedera.com/guides/testnet/nodes">Testnet access</a>.
     *
     * @return {@link com.hedera.hashgraph.sdk.Client}
     */
    public static Client forTestnet() {
        var executor = createExecutor();
        var network = Network.forTestnet(executor);
        var mirrorNetwork = MirrorNetwork.forTestnet(executor);

        return new Client(executor, network, mirrorNetwork, NETWORK_UPDATE_INITIAL_DELAY, DEFAULT_NETWORK_UPDATE_PERIOD);
    }

    /**
     * Construct a Hedera client pre-configured for <a
     * href="https://docs.hedera.com/guides/testnet/testnet-nodes#previewnet-node-public-keys">Preview Testnet nodes</a>.
     *
     * @return {@link com.hedera.hashgraph.sdk.Client}
     */
    public static Client forPreviewnet() {
        var executor = createExecutor();
        var network = Network.forPreviewnet(executor);
        var mirrorNetwork = MirrorNetwork.forPreviewnet(executor);

        return new Client(executor, network, mirrorNetwork, NETWORK_UPDATE_INITIAL_DELAY, DEFAULT_NETWORK_UPDATE_PERIOD);
    }

    /**
     * Configure a client based off the given JSON string.
     *
     * @param json The json string containing the client configuration
     * @return {@link com.hedera.hashgraph.sdk.Client}
     * @throws Exception if the config is incorrect
     */
    public static Client fromConfig(String json) throws Exception {
        return fromConfig(new StringReader(json));
    }

    /**
     * Configure a client based off the given JSON reader.
     *
     * @param json The Reader containing the client configuration
     * @return {@link com.hedera.hashgraph.sdk.Client}
     * @throws Exception if the config is incorrect
     */
    public static Client fromConfig(Reader json) throws Exception {
        Config config = new Gson().fromJson(json, Config.class);
        Client client;

        if (config.network == null) {
            throw new Exception("Network is not set in provided json object");
        } else if (config.network.isJsonObject()) {
            var networks = config.network.getAsJsonObject();
            Map<String, AccountId> nodes = new HashMap<>(networks.size());
            for (Map.Entry<String, JsonElement> entry : networks.entrySet()) {
                nodes.put(entry.getValue().toString().replace("\"", ""), AccountId.fromString(entry.getKey().replace("\"", "")));
            }
            client = Client.forNetwork(nodes);
            if (config.networkName != null) {
                var networkNameString = config.networkName.getAsString();
                try {
                    client.setNetworkName(NetworkName.fromString(networkNameString));
                } catch (Exception ignored) {
                    throw new IllegalArgumentException("networkName in config was \"" + networkNameString + "\", expected either \"mainnet\", \"testnet\" or \"previewnet\"");
                }
            }
        } else {
            String networks = config.network.getAsString();
            client = switch (networks) {
                case MAINNET -> Client.forMainnet();
                case TESTNET -> Client.forTestnet();
                case PREVIEWNET -> Client.forPreviewnet();
                default -> throw new JsonParseException("Illegal argument for network.");
            };
        }

        if (config.operator != null) {
            AccountId operatorAccount = AccountId.fromString(config.operator.accountId);
            PrivateKey privateKey = PrivateKey.fromString(config.operator.privateKey);

            client.setOperator(operatorAccount, privateKey);
        }

        //already set in previous set network if?
        if (config.mirrorNetwork != null) {
            if (config.mirrorNetwork.isJsonArray()) {
                var mirrors = config.mirrorNetwork.getAsJsonArray();
                List<String> listMirrors = new ArrayList<>(mirrors.size());
                for (var i = 0; i < mirrors.size(); i++) {
                    listMirrors.add(mirrors.get(i).getAsString().replace("\"", ""));
                }
                client.setMirrorNetwork(listMirrors);
            } else {
                String mirror = config.mirrorNetwork.getAsString();
                switch (mirror) {
                    case MAINNET -> client.mirrorNetwork = MirrorNetwork.forMainnet(client.executor);
                    case TESTNET -> client.mirrorNetwork = MirrorNetwork.forTestnet(client.executor);
                    case PREVIEWNET -> client.mirrorNetwork = MirrorNetwork.forPreviewnet(client.executor);
                    default -> throw new JsonParseException("Illegal argument for mirrorNetwork.");
                }
            }
        }

        return client;
    }

    /**
     * Configure a client based on a JSON file at the given path.
     *
     * @param fileName The string containing the file path
     * @return {@link com.hedera.hashgraph.sdk.Client}
     * @throws IOException if IO operations fail
     */
    public static Client fromConfigFile(String fileName) throws Exception {
        return fromConfigFile(new File(fileName));
    }

    /**
     * Configure a client based on a JSON file.
     *
     * @param file The file containing the client configuration
     * @return {@link com.hedera.hashgraph.sdk.Client}
     * @throws IOException if IO operations fail
     */
    public static Client fromConfigFile(File file) throws Exception {
        return fromConfig(Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8));
    }

    private synchronized void scheduleNetworkUpdate(@Nullable Duration delay) {
        if (delay == null) {
            networkUpdateFuture = null;
            return;
        }
        networkUpdateFuture = Delayer.delayFor(delay.toMillis(), executor);
        networkUpdateFuture.thenRun(() -> {
            // Checking networkUpdatePeriod != null must be synchronized, so I've put it in a synchronized method.
            requireNetworkUpdatePeriodNotNull(() -> {
                new AddressBookQuery().setFileId(FileId.ADDRESS_BOOK).executeAsync(this).thenCompose(addressBook -> {
                    return requireNetworkUpdatePeriodNotNull(() -> {
                        try {
                            this.setNetworkFromAddressBook(addressBook);
                        } catch (Throwable error) {
                            return CompletableFuture.failedFuture(error);
                        }
                        return CompletableFuture.completedFuture(null);
                    });
                }).exceptionally(error -> {
                    logger.warn("Failed to update address book via mirror node query", error);
                    return null;
                });
                scheduleNetworkUpdate(networkUpdatePeriod);
                return null;
            });
        });
    }

    private synchronized CompletionStage<?> requireNetworkUpdatePeriodNotNull(Supplier<CompletionStage<?>> task) {
        return networkUpdatePeriod != null ? task.get() : CompletableFuture.completedFuture(null);
    }

    private void cancelScheduledNetworkUpdate() {
        if (networkUpdateFuture != null) {
            networkUpdateFuture.cancel(true);
        }
    }

    private void cancelAllSubscriptions() {
        subscriptions.forEach(SubscriptionHandle::unsubscribe);
    }

    void trackSubscription(SubscriptionHandle subscriptionHandle) {
        subscriptions.add(subscriptionHandle);
    }

    void untrackSubscription(SubscriptionHandle subscriptionHandle) {
        subscriptions.remove(subscriptionHandle);
    }

    /**
     * Replace all nodes in this Client with the nodes in the Address Book
     *
     * @param addressBook               A list of nodes and their metadata
     * @return {@code this}
     * @throws InterruptedException     when a thread is interrupted while it's waiting, sleeping, or otherwise occupied
     * @throws TimeoutException         when shutting down nodes
     */
    public synchronized Client setNetworkFromAddressBook(NodeAddressBook addressBook) throws InterruptedException, TimeoutException {
        network.setNetwork(Network.addressBookToNetwork(
            addressBook.nodeAddresses,
            isTransportSecurity() ? PORT_NODE_TLS : PORT_NODE_PLAIN
        ));
        return this;
    }

    /**
     * Replace all nodes in this Client with a new set of nodes (e.g. for an Address Book update).
     *
     * @param network                   a map of node account ID to node URL.
     * @return {@code this} for fluent API usage.
     * @throws TimeoutException         when shutting down nodes
     * @throws InterruptedException     when a thread is interrupted while it's waiting, sleeping, or otherwise occupied
     */
    public synchronized Client setNetwork(Map<String, AccountId> network) throws InterruptedException, TimeoutException {
        this.network.setNetwork(network);
        return this;
    }

    /**
     * Extract the network.
     *
     * @return                          the client's network
     */
    synchronized public Map<String, AccountId> getNetwork() {
        return network.getNetwork();
    }

    /**
     * Set if transport security should be used to connect to consensus nodes.
     * <br>
     * If transport security is enabled all connections to consensus nodes will use TLS, and
     * the server's certificate hash will be compared to the hash stored in the {@link NodeAddressBook}
     * for the given network.
     * <br>
     * *Note*: If transport security is enabled, but {@link Client#isVerifyCertificates()} is disabled then server certificates
     * will not be verified.
     *
     * @param transportSecurity         enable or disable transport security for consensus nodes
     * @return {@code this} for fluent API usage.
     * @throws InterruptedException     when a thread is interrupted while it's waiting, sleeping, or otherwise occupied
     */
    public Client setTransportSecurity(boolean transportSecurity) throws InterruptedException {
        network.setTransportSecurity(transportSecurity);
        return this;
    }

    /**
     *
     * Set if transport security should be used to connect to mirror nodes.
     * <br>
     * If transport security is enabled all connections to mirror nodes will use TLS.
     *
     * @deprecated Mirror nodes can only be accessed using TLS
     * @param transportSecurity - enable or disable transport security for mirror nodes
     * @return {@code this} for fluent API usage.
     */
    @Deprecated
    public Client setMirrorTransportSecurity(boolean transportSecurity) {
        return this;
    }

    /**
     * Is tls enabled for consensus nodes.
     *
     * @return                          is tls enabled
     */
    public boolean isTransportSecurity() {
        return network.isTransportSecurity();
    }

    /**
     * Is tls enabled for mirror nodes.
     *
     * @return                          is tls enabled
     */
    public boolean mirrorIsTransportSecurity() {
        return mirrorNetwork.isTransportSecurity();
    }

    /**
     * Set if server certificates should be verified against an existing address book
     *
     * @param verifyCertificates - enable or disable certificate verification
     * @return {@code this}
     */
    public Client setVerifyCertificates(boolean verifyCertificates) {
        network.setVerifyCertificates(verifyCertificates);
        return this;
    }

    /**
     * Is certificate verification enabled.
     *
     * @return                          is certificate verification enabled
     */
    public boolean isVerifyCertificates() {
        return network.isVerifyCertificates();
    }

    /**
     * Send a ping to the given node.
     *
     * @param nodeAccountId             Account ID of the node to ping
     * @throws TimeoutException         when the transaction times out
     * @throws PrecheckStatusException  when the precheck fails
     */
    public Void ping(AccountId nodeAccountId) throws PrecheckStatusException, TimeoutException {
        return ping(nodeAccountId, getRequestTimeout());
    }

    /**
     * Send a ping to the given node.
     *
     * @param nodeAccountId             Account ID of the node to ping
     * @param timeout                   The timeout after which the execution attempt will be cancelled.
     * @throws TimeoutException         when the transaction times out
     * @throws PrecheckStatusException  when the precheck fails
     */
    public Void ping(AccountId nodeAccountId, Duration timeout) throws PrecheckStatusException, TimeoutException {
        new AccountBalanceQuery()
            .setAccountId(nodeAccountId)
            .setNodeAccountIds(Collections.singletonList(nodeAccountId))
            .execute(this, timeout);

        return null;
    }

    /**
     * Send a ping to the given node asynchronously.
     *
     * @param nodeAccountId Account ID of the node to ping
     * @return an empty future that throws exception if there was an error
     */
    public CompletableFuture<Void> pingAsync(AccountId nodeAccountId) {
        return pingAsync(nodeAccountId, getRequestTimeout());
    }

    /**
     * Send a ping to the given node asynchronously.
     *
     * @param nodeAccountId Account ID of the node to ping
     * @param timeout The timeout after which the execution attempt will be cancelled.
     * @return an empty future that throws exception if there was an error
     */
    public CompletableFuture<Void> pingAsync(AccountId nodeAccountId, Duration timeout) {
        var result = new CompletableFuture<Void>();
        new AccountBalanceQuery()
            .setAccountId(nodeAccountId)
            .setNodeAccountIds(Collections.singletonList(nodeAccountId))
            .executeAsync(this, timeout)
            .whenComplete((balance, error) -> {
                if (error == null){
                    result.complete(null);
                } else {
                    result.completeExceptionally(error);
                }
        });
        return result;
    }

    /**
     * Send a ping to the given node asynchronously.
     *
     * @param nodeAccountId Account ID of the node to ping
     * @param callback a BiConsumer which handles the result or error.
     */
    public void pingAsync(AccountId nodeAccountId, BiConsumer<Void, Throwable> callback) {
        ConsumerHelper.biConsumer(pingAsync(nodeAccountId), callback);
    }

    /**
     * Send a ping to the given node asynchronously.
     *
     * @param nodeAccountId Account ID of the node to ping
     * @param timeout The timeout after which the execution attempt will be cancelled.
     * @param callback a BiConsumer which handles the result or error.
     */
    public void pingAsync(AccountId nodeAccountId, Duration timeout, BiConsumer<Void, Throwable> callback) {
        ConsumerHelper.biConsumer(pingAsync(nodeAccountId, timeout), callback);
    }

    /**
     * Send a ping to the given node asynchronously.
     *
     * @param nodeAccountId Account ID of the node to ping
     * @param onSuccess a Consumer which consumes the result on success.
     * @param onFailure a Consumer which consumes the error on failure.
     */
    public void pingAsync(AccountId nodeAccountId, Consumer<Void> onSuccess, Consumer<Throwable> onFailure) {
        ConsumerHelper.twoConsumers(pingAsync(nodeAccountId), onSuccess, onFailure);
    }

    /**
     * Send a ping to the given node asynchronously.
     *
     * @param nodeAccountId Account ID of the node to ping
     * @param timeout The timeout after which the execution attempt will be cancelled.
     * @param onSuccess a Consumer which consumes the result on success.
     * @param onFailure a Consumer which consumes the error on failure.
     */
    public void pingAsync(AccountId nodeAccountId, Duration timeout, Consumer<Void> onSuccess, Consumer<Throwable> onFailure) {
        ConsumerHelper.twoConsumers(pingAsync(nodeAccountId, timeout), onSuccess, onFailure);
    }

    /**
     * Sends pings to all nodes in the client's network.
     * Combines well with setMaxAttempts(1) to remove all dead nodes from the network.
     *
     * @throws TimeoutException         when the transaction times out
     * @throws PrecheckStatusException  when the precheck fails
     */
    public synchronized Void pingAll() throws PrecheckStatusException, TimeoutException {
        return pingAll(getRequestTimeout());
    }

    /**
     * Sends pings to all nodes in the client's network.
     * Combines well with setMaxAttempts(1) to remove all dead nodes from the network.
     *
     * @param timeoutPerPing            The timeout after which each execution attempt will be cancelled.
     * @throws TimeoutException         when the transaction times out
     * @throws PrecheckStatusException  when the precheck fails
     */
    public synchronized Void pingAll(Duration timeoutPerPing) throws PrecheckStatusException, TimeoutException {
        for (var nodeAccountId : network.getNetwork().values()) {
            ping(nodeAccountId, timeoutPerPing);
        }

        return null;
    }

    /**
     * Sends pings to all nodes in the client's network asynchronously.
     * Combines well with setMaxAttempts(1) to remove all dead nodes from the network.
     *
     * @return an empty future that throws exception if there was an error
     */
    public synchronized CompletableFuture<Void> pingAllAsync() {
        return pingAllAsync(getRequestTimeout());
    }

    /**
     * Sends pings to all nodes in the client's network asynchronously.
     * Combines well with setMaxAttempts(1) to remove all dead nodes from the network.
     *
     * @param timeoutPerPing The timeout after which each execution attempt will be cancelled.
     * @return an empty future that throws exception if there was an error
     */
    public synchronized CompletableFuture<Void> pingAllAsync(Duration timeoutPerPing) {
        var network = this.network.getNetwork();
        var list = new ArrayList<CompletableFuture<Void>>(network.size());

        for (var nodeAccountId : network.values()) {
            list.add(pingAsync(nodeAccountId, timeoutPerPing));
        }

        return CompletableFuture.allOf(list.toArray(new CompletableFuture<?>[0])).thenApply((v) -> null);
    }

    /**
     * Sends pings to all nodes in the client's network asynchronously.
     * Combines well with setMaxAttempts(1) to remove all dead nodes from the network.
     *
     * @param callback a BiConsumer which handles the result or error.
     */
    public void pingAllAsync(BiConsumer<Void, Throwable> callback) {
        ConsumerHelper.biConsumer(pingAllAsync(), callback);
    }

    /**
     * Sends pings to all nodes in the client's network asynchronously.
     * Combines well with setMaxAttempts(1) to remove all dead nodes from the network.
     *
     * @param timeoutPerPing The timeout after which each execution attempt will be cancelled.
     * @param callback a BiConsumer which handles the result or error.
     */
    public void pingAllAsync(Duration timeoutPerPing, BiConsumer<Void, Throwable> callback) {
        ConsumerHelper.biConsumer(pingAllAsync(timeoutPerPing), callback);
    }

    /**
     * Sends pings to all nodes in the client's network asynchronously.
     * Combines well with setMaxAttempts(1) to remove all dead nodes from the network.
     *
     * @param onSuccess a Consumer which consumes the result on success.
     * @param onFailure a Consumer which consumes the error on failure.
     */
    public void pingAllAsync(Consumer<Void> onSuccess, Consumer<Throwable> onFailure) {
        ConsumerHelper.twoConsumers(pingAllAsync(), onSuccess, onFailure);
    }

    /**
     * Sends pings to all nodes in the client's network asynchronously.
     * Combines well with setMaxAttempts(1) to remove all dead nodes from the network.
     *
     * @param timeoutPerPing The timeout after which each execution attempt will be cancelled.
     * @param onSuccess a Consumer which consumes the result on success.
     * @param onFailure a Consumer which consumes the error on failure.
     */
    public void pingAllAsync(Duration timeoutPerPing, Consumer<Void> onSuccess, Consumer<Throwable> onFailure) {
        ConsumerHelper.twoConsumers(pingAllAsync(timeoutPerPing), onSuccess, onFailure);
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
     * @param accountId  The AccountId of the operator
     * @param privateKey The PrivateKey of the operator
     * @return {@code this}
     */
    public synchronized Client setOperator(AccountId accountId, PrivateKey privateKey) {
        return setOperatorWith(accountId, privateKey.getPublicKey(), privateKey::sign);
    }

    /**
     * Sets the account that will, by default, by paying for transactions and queries built with
     * this client.
     * <p>
     * The operator account ID is used to generate a default transaction ID for all transactions
     * executed with this client.
     * <p>
     * The `transactionSigner` is invoked to sign all transactions executed by this client.
     *
     * @param accountId         The AccountId of the operator
     * @param publicKey         The PrivateKey of the operator
     * @param transactionSigner The signer for the operator
     * @return {@code this}
     */
    public synchronized Client setOperatorWith(AccountId accountId, PublicKey publicKey, Function<byte[], byte[]> transactionSigner) {
        if (getNetworkName() != null) {
            try {
                accountId.validateChecksum(this);
            } catch (BadEntityIdException exc) {
                throw new IllegalArgumentException(
                    "Tried to set the client operator account ID to an account ID with an invalid checksum: " + exc.getMessage()
                );
            }
        }

        this.operator = new Operator(accountId, publicKey, transactionSigner);
        return this;
    }

    /**
     * Current name of the network; corresponds to ledger ID in entity ID checksum calculations.
     *
     * @deprecated use {@link #getLedgerId()} instead
     *
     * @return                          the network name
     */
    @Nullable
    @Deprecated
    public synchronized NetworkName getNetworkName() {
        var ledgerId = network.getLedgerId();
        return ledgerId == null ? null : ledgerId.toNetworkName();
    }

    /**
     * Set the network name to a particular value. Useful when constructing a network which is a subset of an existing
     * known network.
     *
     * @deprecated use {@link #setLedgerId(LedgerId)} instead
     *
     * @param networkName               the desired network
     * @return {@code this}
     */
    @Deprecated
    public synchronized Client setNetworkName(@Nullable NetworkName networkName) {
        this.network.setLedgerId(networkName == null ? null : LedgerId.fromNetworkName(networkName));
        return this;
    }

    /**
     * Current LedgerId of the network; corresponds to ledger ID in entity ID checksum calculations.
     *
     * @return                          the ledger id
     */
    @Nullable
    public synchronized LedgerId getLedgerId() {
        return network.getLedgerId();
    }

    /**
     * Set the LedgerId to a particular value. Useful when constructing a network which is a subset of an existing
     * known network.
     *
     * @param ledgerId                  the desired ledger id
     * @return {@code this}
     */
    public synchronized Client setLedgerId(@Nullable LedgerId ledgerId) {
        this.network.setLedgerId(ledgerId);
        return this;
    }

    /**
     * Max number of attempts a request executed with this client will do.
     *
     * @return                          the maximus attempts
     */
    public synchronized int getMaxAttempts() {
        return maxAttempts;
    }

    /**
     * Set the max number of attempts a request executed with this client will do.
     *
     * @param maxAttempts               the desired max attempts
     * @return {@code this}
     */
    public synchronized Client setMaxAttempts(int maxAttempts) {
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("maxAttempts must be greater than zero");
        }
        this.maxAttempts = maxAttempts;
        return this;
    }

    /**
     * The maximum amount of time to wait between retries
     *
     * @return maxBackoff
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "A Duration can't actually be mutated"
    )
    public Duration getMaxBackoff() {
        return maxBackoff;
    }

    /**
     * The maximum amount of time to wait between retries. Every retry attempt will increase the wait time exponentially
     * until it reaches this time.
     *
     * @param maxBackoff The maximum amount of time to wait between retries
     * @return {@code this}
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "A Duration can't actually be mutated"
    )
    public Client setMaxBackoff(Duration maxBackoff) {
        if (maxBackoff == null || maxBackoff.toNanos() < 0) {
            throw new IllegalArgumentException("maxBackoff must be a positive duration");
        } else if (maxBackoff.compareTo(minBackoff) < 0) {
            throw new IllegalArgumentException("maxBackoff must be greater than or equal to minBackoff");
        }
        this.maxBackoff = maxBackoff;
        return this;
    }

    /**
     * The minimum amount of time to wait between retries
     *
     * @return minBackoff
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "A Duration can't actually be mutated"
    )
    public Duration getMinBackoff() {
        return minBackoff;
    }

    /**
     * The minimum amount of time to wait between retries. When retrying, the delay will start at this time and increase
     * exponentially until it reaches the maxBackoff.
     *
     * @param minBackoff The minimum amount of time to wait between retries
     * @return {@code this}
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "A Duration can't actually be mutated"
    )
    public Client setMinBackoff(Duration minBackoff) {
        if (minBackoff == null || minBackoff.toNanos() < 0) {
            throw new IllegalArgumentException("minBackoff must be a positive duration");
        } else if (minBackoff.compareTo(maxBackoff) > 0) {
            throw new IllegalArgumentException("minBackoff must be less than or equal to maxBackoff");
        }
        this.minBackoff = minBackoff;
        return this;
    }

    /**
     * Max number of times any node in the network can receive a bad gRPC status before being removed from the network.
     *
     * @return                          the maximum node attempts
     */
    public synchronized int getMaxNodeAttempts() {
        return network.getMaxNodeAttempts();
    }

    /**
     * Set the max number of times any node in the network can receive a bad gRPC status before being removed from the
     * network.
     *
     * @param maxNodeAttempts           the desired minimum attempts
     * @return {@code this}
     */
    public synchronized Client setMaxNodeAttempts(int maxNodeAttempts) {
        this.network.setMaxNodeAttempts(maxNodeAttempts);
        return this;
    }

    /**
     * The minimum backoff time for any node in the network.
     *
     * @deprecated - Use {@link Client#getNodeMaxBackoff()} instead
     * @return                          the wait time
     */
    @Deprecated
    public synchronized Duration getNodeWaitTime() {
        return getNodeMinBackoff();
    }

    /**
     * Set the minimum backoff time for any node in the network.
     *
     * @deprecated - Use {@link Client#setNodeMinBackoff(Duration)} ()} instead
     * @param nodeWaitTime              the wait time
     * @return                          the updated client
     */
    @Deprecated
    public synchronized Client setNodeWaitTime(Duration nodeWaitTime) {
        return setNodeMinBackoff(nodeWaitTime);
    }

    /**
     * The minimum backoff time for any node in the network.
     *
     * @return                          the minimum backoff time
     */
    public synchronized Duration getNodeMinBackoff() {
        return network.getMinNodeBackoff();
    }

    /**
     * Set the minimum backoff time for any node in the network.
     *
     * @param minBackoff                the desired minimum backoff time
     * @return {@code this}
     */
    public synchronized Client setNodeMinBackoff(Duration minBackoff) {
        network.setMinNodeBackoff(minBackoff);
        return this;
    }

    /**
     * The maximum backoff time for any node in the network.
     *
     * @return                          the maximum node backoff time
     */
    public synchronized Duration getNodeMaxBackoff() {
        return network.getMaxNodeBackoff();
    }

    /**
     * Set the maximum backoff time for any node in the network.
     *
     * @param maxBackoff                the desired max backoff time
     * @return {@code this}
     */
    public synchronized Client setNodeMaxBackoff(Duration maxBackoff) {
        network.setMaxNodeBackoff(maxBackoff);
        return this;
    }

    /**
     * Extract the minimum node readmit time.
     *
     * @return                          the minimum node readmit time
     */
    public Duration getMinNodeReadmitTime() {
        return network.getMinNodeReadmitTime();
    }

    /**
     * Assign the minimum node readmit time.
     *
     * @param minNodeReadmitTime        the requested duration
     * @return  {@code this}
     */
    public Client setMinNodeReadmitTime(Duration minNodeReadmitTime) {
        network.setMinNodeReadmitTime(minNodeReadmitTime);
        return this;
    }

    /**
     * Extract the node readmit time.
     *
     * @return                          the maximum node readmit time
     */
    public Duration getMaxNodeReadmitTime() {
        return network.getMaxNodeReadmitTime();
    }

    /**
     * Assign the maximum node readmit time.
     *
     * @param maxNodeReadmitTime        the maximum node readmit time
     * @return {@code this}
     */
    public Client setMaxNodeReadmitTime(Duration maxNodeReadmitTime) {
        network.setMaxNodeReadmitTime(maxNodeReadmitTime);
        return this;
    }

    /**
     * Set the max amount of nodes that will be chosen per request. By default, the request will use 1/3rd the network
     * nodes per request.
     *
     * @param maxNodesPerTransaction    the desired number of nodes
     * @return {@code this}
     */
    public synchronized Client setMaxNodesPerTransaction(int maxNodesPerTransaction) {
        this.network.setMaxNodesPerRequest(maxNodesPerTransaction);
        return this;
    }

    /**
     * Enable or disable automatic entity ID checksum validation.
     *
     * @param value                     the desired value
     * @return {@code this}
     */
    public synchronized Client setAutoValidateChecksums(boolean value) {
        autoValidateChecksums = value;
        return this;
    }

    /**
     * Is automatic entity ID checksum validation enabled.
     *
     * @return                          is validation enabled
     */
    public synchronized boolean isAutoValidateChecksumsEnabled() {
        return autoValidateChecksums;
    }

    /**
     * Get the ID of the operator. Useful when the client was constructed from file.
     *
     * @return {AccountId}
     */
    @Nullable
    public synchronized AccountId getOperatorAccountId() {
        if (operator == null) {
            return null;
        }

        return operator.accountId;
    }

    /**
     * Get the key of the operator. Useful when the client was constructed from file.
     *
     * @return {PublicKey}
     */
    @Nullable
    public synchronized PublicKey getOperatorPublicKey() {
        if (operator == null) {
            return null;
        }

        return operator.publicKey;
    }

    /**
     * The default maximum fee used for transactions.
     *
     * @return                          the max transaction fee
     */
    @Nullable
    public synchronized Hbar getDefaultMaxTransactionFee() {
        return defaultMaxTransactionFee;
    }

    /**
     * Set the maximum fee to be paid for transactions executed by this client.
     * <p>
     * Because transaction fees are always maximums, this will simply add a call to
     * {@link Transaction#setMaxTransactionFee(Hbar)} on every new transaction. The actual
     * fee assessed for a given transaction may be less than this value, but never greater.
     *
     * @param defaultMaxTransactionFee The Hbar to be set
     * @return {@code this}
     */
    public synchronized Client setDefaultMaxTransactionFee(Hbar defaultMaxTransactionFee) {
        Objects.requireNonNull(defaultMaxTransactionFee);
        if (defaultMaxTransactionFee.toTinybars() < 0) {
            throw new IllegalArgumentException("maxTransactionFee must be non-negative");
        }

        this.defaultMaxTransactionFee = defaultMaxTransactionFee;
        return this;
    }

    /**
     * Set the maximum fee to be paid for transactions executed by this client.
     * <p>
     * Because transaction fees are always maximums, this will simply add a call to
     * {@link Transaction#setMaxTransactionFee(Hbar)} on every new transaction. The actual
     * fee assessed for a given transaction may be less than this value, but never greater.
     *
     * @deprecated Use {@link #setDefaultMaxTransactionFee(Hbar)} instead.
     * @param maxTransactionFee The Hbar to be set
     * @return {@code this}
     */
    @Deprecated
    public synchronized Client setMaxTransactionFee(Hbar maxTransactionFee) {
        return setDefaultMaxTransactionFee(maxTransactionFee);
    }

    /**
     * Extract the maximum query payment.
     *
     * @return                          the default maximum query payment
     */
    public synchronized Hbar getDefaultMaxQueryPayment() {
        return defaultMaxQueryPayment;
    }

    /**
     * Set the maximum default payment allowable for queries.
     * <p>
     * When a query is executed without an explicit {@link Query#setQueryPayment(Hbar)} call,
     * the client will first request the cost
     * of the given query from the node it will be submitted to and attach a payment for that amount
     * from the operator account on the client.
     * <p>
     * If the returned value is greater than this value, a
     * {@link MaxQueryPaymentExceededException} will be thrown from
     * {@link Query#execute(Client)} or returned in the second callback of
     * {@link Query#executeAsync(Client, Consumer, Consumer)}.
     * <p>
     * Set to 0 to disable automatic implicit payments.
     *
     * @param defaultMaxQueryPayment The Hbar to be set
     * @return {@code this}
     */
    public synchronized Client setDefaultMaxQueryPayment(Hbar defaultMaxQueryPayment) {
        Objects.requireNonNull(defaultMaxQueryPayment);
        if (defaultMaxQueryPayment.toTinybars() < 0) {
            throw new IllegalArgumentException("defaultMaxQueryPayment must be non-negative");
        }

        this.defaultMaxQueryPayment = defaultMaxQueryPayment;
        return this;
    }

    /**
     * @deprecated Use {@link #setDefaultMaxQueryPayment(Hbar)} instead.
     * @param maxQueryPayment The Hbar to be set
     * @return {@code this}
     */
    @Deprecated
    public synchronized Client setMaxQueryPayment(Hbar maxQueryPayment) {
        return setDefaultMaxQueryPayment(maxQueryPayment);
    }

    /**
     * Should the transaction id be regenerated?
     *
     * @return                          the default regenerate transaction id
     */
    public synchronized boolean getDefaultRegenerateTransactionId() {
        return defaultRegenerateTransactionId;
    }

    /**
     * Assign the default regenerate transaction id.
     *
     * @param regenerateTransactionId   should there be a regenerated transaction id
     * @return {@code this}
     */
    public synchronized Client setDefaultRegenerateTransactionId(boolean regenerateTransactionId) {
        this.defaultRegenerateTransactionId = regenerateTransactionId;
        return this;
    }

    /**
     * Maximum amount of time a request can run
     *
     * @return                          the timeout value
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "A Duration can't actually be mutated"
    )
    public synchronized Duration getRequestTimeout() {
        return requestTimeout;
    }

    /**
     * Set the maximum amount of time a request can run. Used only in async variants of methods.
     *
     * @param requestTimeout            the timeout value
     * @return {@code this}
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "A Duration can't actually be mutated"
    )
    public synchronized Client setRequestTimeout(Duration requestTimeout) {
        this.requestTimeout = Objects.requireNonNull(requestTimeout);
        return this;
    }

    /**
     * Maximum amount of time closing a network can take.
     *
     * @return                          the timeout value
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "A Duration can't actually be mutated"
    )
    public Duration getCloseTimeout() {
        return closeTimeout;
    }

    /**
     * Set the maximum amount of time closing a network can take.
     *
     * @param closeTimeout              the timeout value
     * @return {@code this}
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "A Duration can't actually be mutated"
    )
    public Client setCloseTimeout(Duration closeTimeout) {
        this.closeTimeout = Objects.requireNonNull(closeTimeout);
        network.setCloseTimeout(closeTimeout);
        mirrorNetwork.setCloseTimeout(closeTimeout);
        return this;
    }

    /**
     * Maximum amount of time a gRPC request can run
     *
     * @return                          the gRPC deadline value
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "A Duration can't actually be mutated"
    )
    public Duration getGrpcDeadline() {
        return grpcDeadline.get();
    }

    /**
     * Set the maximum amount of time a gRPC request can run.
     *
     * @param grpcDeadline            the gRPC deadline value
     * @return {@code this}
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "A Duration can't actually be mutated"
    )
    public Client setGrpcDeadline(Duration grpcDeadline) {
        this.grpcDeadline.set(Objects.requireNonNull(grpcDeadline));
        return this;
    }

    /**
     * Extract the operator.
     *
     * @return                          the operator
     */
    @Nullable
    synchronized Operator getOperator() {
        return this.operator;
    }

    /**
     * Get the period for updating the Address Book
     *
     * @return the networkUpdatePeriod
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "A Duration can't actually be mutated"
    )
    @Nullable
    public synchronized Duration getNetworkUpdatePeriod() {
        return this.networkUpdatePeriod;
    }

    /**
     * Set the period for updating the Address Book
     *
     * @param networkUpdatePeriod   the period for updating the Address Book
     * @return {@code this}
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "A Duration can't actually be mutated"
    )
    public synchronized Client setNetworkUpdatePeriod(Duration networkUpdatePeriod) {
        cancelScheduledNetworkUpdate();
        this.networkUpdatePeriod = networkUpdatePeriod;
        scheduleNetworkUpdate(networkUpdatePeriod);
        return this;
    }

    /**
     * Initiates an orderly shutdown of all channels (to the Hedera network) in which preexisting
     * transactions or queries continue but more would be immediately cancelled.
     *
     * <p>After this method returns, this client can be re-used. Channels will be re-established as
     * needed.
     *
     * @throws TimeoutException if the mirror network doesn't close in time
     */
    @Override
    public synchronized void close() throws TimeoutException {
        close(closeTimeout);
    }

    /**
     * Initiates an orderly shutdown of all channels (to the Hedera network) in which preexisting
     * transactions or queries continue but more would be immediately cancelled.
     *
     * <p>After this method returns, this client can be re-used. Channels will be re-established as
     * needed.
     *
     * @param timeout The Duration to be set
     * @throws TimeoutException if the mirror network doesn't close in time
     */
    public synchronized void close(Duration timeout) throws TimeoutException {
        var closeDeadline = Instant.now().plus(timeout);

        networkUpdatePeriod = null;
        cancelScheduledNetworkUpdate();
        cancelAllSubscriptions();

        network.beginClose();
        mirrorNetwork.beginClose();

        var networkError = network.awaitClose(closeDeadline, null);
        var mirrorNetworkError = mirrorNetwork.awaitClose(closeDeadline, networkError);

        // https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html
        try {
            executor.shutdown();
            if (!executor.awaitTermination(timeout.getSeconds() / 2, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(timeout.getSeconds() / 2, TimeUnit.SECONDS)) {
                    logger.warn("Pool did not terminate");
                }
            }
        } catch (InterruptedException ex) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        if (mirrorNetworkError != null) {
            if (mirrorNetworkError instanceof TimeoutException) {
                throw (TimeoutException) mirrorNetworkError;
            } else {
                throw new RuntimeException(mirrorNetworkError);
            }
        }
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

    private static class Config {
        @Nullable
        private JsonElement network;

        @Nullable
        private JsonElement networkName;

        @Nullable
        private ConfigOperator operator;

        @Nullable
        private JsonElement mirrorNetwork;

        private static class ConfigOperator {
            private String accountId = "";
            private String privateKey = "";
        }
    }
}
