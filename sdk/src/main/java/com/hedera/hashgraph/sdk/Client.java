package com.hedera.hashgraph.sdk;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java8.util.Lists;
import java8.util.concurrent.CompletableFuture;
import java8.util.function.Consumer;
import java8.util.function.Function;
import org.threeten.bp.Duration;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

/**
 * Managed client for use on the Hedera Hashgraph network.
 */
public final class Client implements AutoCloseable, WithPing, WithPingAll {
    static final int DEFAULT_MAX_ATTEMPTS = 10;
    static final Duration DEFAULT_MAX_BACKOFF = Duration.ofSeconds(8L);
    static final Duration DEFAULT_MIN_BACKOFF = Duration.ofMillis(250L);

    private static final Hbar DEFAULT_MAX_QUERY_PAYMENT = new Hbar(1);

    final ExecutorService executor;
    @Nullable
    Hbar defaultMaxTransactionFee = null;
    Hbar defaultMaxQueryPayment = DEFAULT_MAX_QUERY_PAYMENT;

    Network network;
    MirrorNetwork mirrorNetwork;

    @Nullable
    private Operator operator;

    private Duration requestTimeout = Duration.ofMinutes(2);

    private int maxAttempts = DEFAULT_MAX_ATTEMPTS;

    private volatile Duration maxBackoff = DEFAULT_MAX_BACKOFF;

    private volatile Duration minBackoff = DEFAULT_MIN_BACKOFF;

    private boolean autoValidateChecksums = false;

    Client(ExecutorService executor, Network network, MirrorNetwork mirrorNetwork) {
        this.executor = executor;
        this.network = network;
        this.mirrorNetwork = mirrorNetwork;
    }

    private static ExecutorService createExecutor() {
        var threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("hedera-sdk-%d")
            .setDaemon(true)
            .build();

        return Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            threadFactory);
    }

    public Client setMirrorNetwork(MirrorNetwork mirrorNetwork) {
        this.mirrorNetwork = mirrorNetwork;
        return this;
    }

    public synchronized Client setMirrorNetwork(List<String> network) throws InterruptedException {
        this.mirrorNetwork.setNetwork(network);
        return this;
    }

    public List<String> getMirrorNetwork() {
        if (mirrorNetwork != null) {
            return mirrorNetwork.addresses;
        } else {
            return new ArrayList<>();
        }
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
        var mirrorNetwork = MirrorNetwork.forMainnet(executor);

        return new Client(executor, network, mirrorNetwork);
    }

    public static Client forName(String name) {
        switch (name) {
            case "mainnet":
                return Client.forMainnet();
            case "testnet":
                return Client.forTestnet();
            case "previewnet":
                return Client.forPreviewnet();
            default:
                throw new IllegalArgumentException("Name must be one-of `mainnet`, `testnet`, or `previewnet`");
        }
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

        return new Client(executor, network, mirrorNetwork);
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
        var mirrorNetwork = MirrorNetwork.forMainnet(executor);

        return new Client(executor, network, mirrorNetwork);
    }

    public static Client forPreviewnet() {
        var executor = createExecutor();
        var network = Network.forPreviewnet(executor);
        var mirrorNetwork = MirrorNetwork.forMainnet(executor);

        return new Client(executor, network, mirrorNetwork);
    }

    /**
     * Configure a client based off the given JSON string.
     *
     * @param json The json string containing the client configuration
     * @return {@link com.hedera.hashgraph.sdk.Client}
     */
    public static Client fromConfig(String json) throws Exception {
        return fromConfig(new StringReader(json));
    }

    /**
     * Configure a client based off the given JSON reader.
     *
     * @param json The Reader containing the client configuration
     * @return {@link com.hedera.hashgraph.sdk.Client}
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
        } else {
            String networks = config.network.getAsString();
            switch (networks) {
                case "mainnet":
                    client = Client.forMainnet();
                    break;
                case "testnet":
                    client = Client.forTestnet();
                    break;
                case "previewnet":
                    client = Client.forPreviewnet();
                    break;
                default:
                    throw new JsonParseException("Illegal argument for network.");
            }
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
                    case "mainnet":
                        client.setMirrorNetwork(Lists.of("hcs.mainnet.mirrornode.hedera.com:5600"));
                        break;
                    case "testnet":
                        client.setMirrorNetwork(Lists.of("hcs.testnet.mirrornode.hedera.com:5600"));
                        break;
                    case "previewnet":
                        client.setMirrorNetwork(Lists.of("hcs.previewnet.mirrornode.hedera.com:5600"));
                        break;
                    default:
                        throw new JsonParseException("Illegal argument for mirrorNetwork.");
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

    /**
     * Replace all nodes in this Client with a new set of nodes (e.g. for an Address Book update).
     * <p>
     *
     * @param network a map of node account ID to node URL.
     * @return {@code this} for fluent API usage.
     */
    public synchronized Client setNetwork(Map<String, AccountId> network) throws InterruptedException, TimeoutException {
        this.network.setNetwork(network);
        return this;
    }

    public Map<String, AccountId> getNetwork() {
        var network = new HashMap<String, AccountId>(this.network.network.size());

        for (var entry : this.network.network.entrySet()) {
            network.put(entry.getKey(), AccountId.fromProtobuf(entry.getValue().toProtobuf()));
        }

        return network;
    }

    @FunctionalExecutable(type = "Void", onClient = true, inputType = "AccountId")
    public synchronized CompletableFuture<Void> pingAsync(AccountId nodeAccountId) {
        return new AccountBalanceQuery()
            .setAccountId(nodeAccountId)
            .setNodeAccountIds(Collections.singletonList(nodeAccountId))
            .executeAsync(this)
            .handle((balance, e) -> {
                // Do nothing
                return null;
            });
    }

    @FunctionalExecutable(type = "Void", onClient = true)
    public synchronized CompletableFuture<Void> pingAllAsync() {
        var list = new ArrayList<CompletableFuture<Void>>(network.network.size());

        for (var nodeAccountId : network.network.values()) {
            list.add(pingAsync(nodeAccountId));
        }

        return CompletableFuture.allOf(list.toArray(new CompletableFuture<?>[0])).thenApply((v) -> null);
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

    @Nullable
    public synchronized NetworkName getNetworkName() {
        return network.networkName;
    }

    public synchronized Client setNetworkName(@Nullable NetworkName networkName) {
        this.network.networkName = networkName;
        return this;
    }

    public synchronized int getMaxAttempts() {
        return maxAttempts;
    }

    public synchronized Client setMaxAttempts(int maxAttempts) {
        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("maxAttempts must be greater than zero");
        }
        this.maxAttempts = maxAttempts;
        return this;
    }

    /**
     * @return maxBackoff The maximum amount of time to wait between retries
     */
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
     * @return minBackoff The minimum amount of time to wait between retries
     */
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
    public Client setMinBackoff(Duration minBackoff) {
        if (minBackoff == null || minBackoff.toNanos() < 0) {
            throw new IllegalArgumentException("minBackoff must be a positive duration");
        } else if (minBackoff.compareTo(maxBackoff) > 0) {
            throw new IllegalArgumentException("minBackoff must be less than or equal to maxBackoff");
        }
        this.minBackoff = minBackoff;
        return this;
    }

    public synchronized int getMaxNodeAttempts() {
        return network.getMaxNodeAttempts();
    }

    public synchronized Client setMaxNodeAttempts(int maxNodeAttempts) {
        this.network.setMaxNodeAttempts(maxNodeAttempts);
        return this;
    }

    public synchronized Duration getNodeWaitTime() {
        return network.getNodeWaitTime();
    }

    public synchronized Client setNodeWaitTime(Duration nodeWaitTime) {
        network.setNodeWaitTime(nodeWaitTime);
        return this;
    }

    public synchronized Client setMaxNodesPerTransaction(int maxNodesPerTransaction) {
        this.network.setMaxNodesPerTransaction(maxNodesPerTransaction);
        return this;
    }

    public synchronized Client setAutoValidateChecksums(boolean value) {
        autoValidateChecksums = value;
        return this;
    }

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
     * @deprecated Use {@link #setDefaultMaxTransactionFee(Hbar)} instead.
     */
    public synchronized Client setMaxTransactionFee(Hbar maxTransactionFee) {
        return setDefaultMaxTransactionFee(maxTransactionFee);
    }

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
     */
    @Deprecated
    public synchronized Client setMaxQueryPayment(Hbar maxQueryPayment) {
        return setDefaultMaxQueryPayment(maxQueryPayment);
    }

    public synchronized Duration getRequestTimeout() {
        return requestTimeout;
    }

    public synchronized Client setRequestTimeout(Duration requestTimeout) {
        this.requestTimeout = requestTimeout;
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
    public synchronized void close() throws TimeoutException {
        close(Duration.ofSeconds(30));
    }

    /**
     * Initiates an orderly shutdown of all channels (to the Hedera network) in which preexisting
     * transactions or queries continue but more would be immediately cancelled.
     *
     * <p>After this method returns, this client can be re-used. Channels will be re-established as
     * needed.
     *
     * @param timeout The Duration to be set
     */
    public synchronized void close(Duration timeout) throws TimeoutException {
        network.close(timeout);
        mirrorNetwork.close(timeout);
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
        private ConfigOperator operator;

        @Nullable
        private JsonElement mirrorNetwork;

        private static class ConfigOperator {
            private String accountId = "";
            private String privateKey = "";
        }
    }
}
