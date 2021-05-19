package com.hedera.hashgraph.sdk;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java8.util.Lists;
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
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

/**
 * Managed client for use on the Hedera Hashgraph network.
 */
public final class Client implements AutoCloseable {
    private static final Hbar DEFAULT_MAX_QUERY_PAYMENT = new Hbar(1);
    private static final Hbar DEFAULT_MAX_TRANSACTION_FEE = new Hbar(1);

    Hbar maxTransactionFee = DEFAULT_MAX_QUERY_PAYMENT;
    Hbar maxQueryPayment = DEFAULT_MAX_TRANSACTION_FEE;

    Network network;
    MirrorNetwork mirrorNetwork;

    final ExecutorService executor;

    @Nullable
    private Operator operator;

    Duration requestTimeout = Duration.ofMinutes(2);

    Client(Map<String, AccountId> network) {
        var threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("hedera-sdk-%d")
            .setDaemon(true)
            .build();

        this.executor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            threadFactory);

        this.network = new Network(executor, network);
        this.mirrorNetwork = new MirrorNetwork(executor);
    }

    public synchronized void setMirrorNetwork(List<String> network) throws InterruptedException {
        mirrorNetwork.setNetwork(network);
    }

    public List<String> getMirrorNetwork() {
        return mirrorNetwork.addresses;
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
     * @return {@link com.hedera.hashgraph.sdk.Client}
     */
    public static Client forNetwork(Map<String, AccountId> network) {
        return new Client(network);
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
        var network = new Hashtable<String, AccountId>();
        network.put("35.237.200.180:50211", new AccountId(3));
        network.put("35.186.191.247:50211", new AccountId(4));
        network.put("35.192.2.25:50211", new AccountId(5));
        network.put("35.199.161.108:50211", new AccountId(6));
        network.put("35.203.82.240:50211", new AccountId(7));
        network.put("35.236.5.219:50211", new AccountId(8));
        network.put("35.197.192.225:50211", new AccountId(9));
        network.put("35.242.233.154:50211", new AccountId(10));
        network.put("35.240.118.96:50211", new AccountId(11));
        network.put("35.204.86.32:50211", new AccountId(12));
        network.put("35.234.132.107:50211", new AccountId(13));
        network.put("35.236.2.27:50211", new AccountId(14));
        network.put("35.228.11.53:50211", new AccountId(15));
        network.put("34.91.181.183:50211", new AccountId(16));
        network.put("34.86.212.247:50211", new AccountId(17));
        network.put("172.105.247.67:50211", new AccountId(18));
        network.put("34.89.87.138:50211", new AccountId(19));
        network.put("34.82.78.255:50211", new AccountId(20));

        var client = Client.forNetwork(network);

        try {
            client.setMirrorNetwork(Lists.of("hcs.mainnet.mirrornode.hedera.com:5600"));
        } catch (InterruptedException e) {
            // This should never occur. The network is empty.
        }

        return client;
    }

    /**
     * Construct a Hedera client pre-configured for <a
     * href="https://docs.hedera.com/guides/testnet/nodes">Testnet access</a>.
     *
     * @return {@link com.hedera.hashgraph.sdk.Client}
     */
    public static Client forTestnet() {
        var network = new Hashtable<String, AccountId>();
        network.put("0.testnet.hedera.com:50211", new AccountId(3));
        network.put("1.testnet.hedera.com:50211", new AccountId(4));
        network.put("2.testnet.hedera.com:50211", new AccountId(5));
        network.put("3.testnet.hedera.com:50211", new AccountId(6));
        network.put("4.testnet.hedera.com:50211", new AccountId(7));


        var client = Client.forNetwork(network);

        try {
            client.setMirrorNetwork(Lists.of("hcs.testnet.mirrornode.hedera.com:5600"));
        } catch (InterruptedException e) {
            // This should never occur. The network is empty.
        }

        return client;
    }

    public static Client forPreviewnet() {
        var network = new Hashtable<String, AccountId>();
        network.put("0.previewnet.hedera.com:50211", new AccountId(3));
        network.put("1.previewnet.hedera.com:50211", new AccountId(4));
        network.put("2.previewnet.hedera.com:50211", new AccountId(5));
        network.put("3.previewnet.hedera.com:50211", new AccountId(6));
        network.put("4.previewnet.hedera.com:50211", new AccountId(7));


        var client = Client.forNetwork(network);

        try {
            client.setMirrorNetwork(Lists.of("hcs.previewnet.mirrornode.hedera.com:5600"));
        } catch (InterruptedException e) {
            // This should never occur. The network is empty.
        }

        return client;
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
                nodes.put(entry.getValue().toString().replace("\"", ""), AccountId.fromString(entry.getKey().toString().replace("\"", "")));
            }
            client = new Client(nodes);
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
        var network = new Hashtable<String, AccountId>(this.network.network.size());

        for (var entry : this.network.network.entrySet()) {
            network.put(entry.getKey(), AccountId.fromProtobuf(entry.getValue().toProtobuf()));
        }

        return network;
    }

    public void ping(AccountId nodeAccountId) throws TimeoutException, PrecheckStatusException {
        new AccountBalanceQuery()
            .setAccountId(nodeAccountId)
            .setNodeAccountIds(Collections.singletonList(nodeAccountId))
            .execute(this);
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
        this.operator = new Operator(accountId, publicKey, transactionSigner);
        return this;
    }

    /**
     * Get the ID of the operator. Useful when the client was constructed from file.
     *
     * @return {AccountId}
     */
    @Nullable
    public AccountId getOperatorAccountId() {
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
    public PublicKey getOperatorPublicKey() {
        if (operator == null) {
            return null;
        }

        return operator.publicKey;
    }

    /**
     * Set the maximum fee to be paid for transactions executed by this client.
     * <p>
     * Because transaction fees are always maximums, this will simply add a call to
     * {@link Transaction#setMaxTransactionFee(Hbar)} on every new transaction. The actual
     * fee assessed for a given transaction may be less than this value, but never greater.
     *
     * @param maxTransactionFee The Hbar to be set
     * @return {@code this}
     */
    public synchronized Client setMaxTransactionFee(Hbar maxTransactionFee) {
        if (maxTransactionFee.toTinybars() < 0) {
            throw new IllegalArgumentException("maxTransactionFee must be non-negative");
        }

        this.maxTransactionFee = maxTransactionFee;
        return this;
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
     * @param maxQueryPayment The Hbar to be set
     * @return {@code this}
     */
    public synchronized Client setMaxQueryPayment(Hbar maxQueryPayment) {
        if (maxQueryPayment.toTinybars() < 0) {
            throw new IllegalArgumentException("maxQueryPayment must be non-negative");
        }

        this.maxQueryPayment = maxQueryPayment;
        return this;
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
    public void close(Duration timeout) throws TimeoutException {
        network.close(timeout);
        mirrorNetwork.close(timeout);
    }

    private String getUserAgent() {
        var thePackage = getClass().getPackage();
        var implementationVersion = thePackage != null ? thePackage.getImplementationVersion() : null;
        return "hedera-sdk-java/" + ((implementationVersion != null) ? ("v" + implementationVersion) : "DEV");
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
