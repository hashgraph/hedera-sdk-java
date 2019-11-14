package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.account.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.account.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.account.AccountInfo;
import com.hedera.hashgraph.sdk.account.AccountInfoQuery;
import com.hedera.hashgraph.sdk.account.CryptoTransferTransaction;
import com.hedera.hashgraph.sdk.crypto.PublicKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
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
 *
 * These error messages are printed because the runtime may exit with pending calls if you do not
 * shutdown the channels properly which allows them to finish any pending calls.
 *
 * To shutdown channels, this class implements {@link AutoCloseable} (allowing use
 * with the try-with-resources syntax) which waits at most a few seconds for channels to finish
 * their calls and terminate.
 *
 * Alternatively, you may call {@link #awaitChannelShutdown(long, TimeUnit)} directly with a
 * custom timeout.
 *
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
    private Ed25519PrivateKey operatorKey;

    public Client(AccountId nodeAccountId, String nodeUrl) {
        nodes = new HashMap<>();
        putNode(nodeAccountId, nodeUrl);
    }

    public Client(Map<AccountId, String> nodes) {
        if (nodes.isEmpty()) {
            throw new IllegalArgumentException("List of nodes must not be empty");
        }

        this.nodes = nodes.entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, t -> new Node(t.getKey(), t.getValue())));
    }

    /**
     * Insert or update a node in the client.
     *
     * If a replaced node is already being used by some transaction or query, this call will cause
     * that transaction/query to return an error on execute.
     *
     * @param nodeAccountId
     * @param nodeUrl
     * @return
     */
    public Client putNode(AccountId nodeAccountId, String nodeUrl) {
        final Node replaced = nodes.put(nodeAccountId, new Node(nodeAccountId, nodeUrl));

        if (replaced != null) {
            // only `.shutdown()` is necessary to silence the error, we just don't want to
            // exit prematurely if the whole client is going to be garbage collected
            // which is why we await termination in close() below
            replaced.closeChannel();
        }

        return this;
    }

    /**
     * Set the maximum fee to be paid for transactions executed by this client.
     *
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
     * Set the maximum default payment value allowable for queries.
     *
     * When a query is executed without an explicit {@link QueryBuilder#setPayment(Transaction)}
     * or {@link QueryBuilder#setPaymentDefault(long)} call, the client will first request the cost
     * of the given query from the node it will be submitted to and attach a payment for that amount
     * from the operator account on the client.
     *
     * If the returned value is greater than this value, a
     * {@link QueryBuilder.MaxPaymentExceededException} will be thrown from
     * {@link QueryBuilder#execute()} or returned in the second callback of
     * {@link QueryBuilder#executeAsync(Consumer, Consumer)}.
     *
     * Set to 0 to disable automatic implicit payments.
     *
     * @param maxQueryPayment
     * @return
     */
    public Client setMaxQueryPayment(@Nonnegative long maxQueryPayment) {
        this.maxQueryPayment = maxQueryPayment;
        return this;
    }

    public Client setOperator(AccountId operatorId, Ed25519PrivateKey operatorKey) {
        this.operatorId = operatorId;
        this.operatorKey = operatorKey;
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
    Ed25519PrivateKey getOperatorKey() {
        return operatorKey;
    }

    Node pickNode() {
        if (nodes.isEmpty()) {
            throw new IllegalStateException("List of channels has become empty");
        }

        int r = random.nextInt(nodes.size());
        Iterator<Node> channelIter = nodes.values()
            .iterator();

        for (int i = 1; i < r; i++) {
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

    //
    // Simplified interface intended for high-level, opinionated operation
    //

    /**
     * @deprecated hides useful configuration parameters for accounts and makes it difficult to
     * handle errors properly; if an error occurs while fetching the receipt for the
     * {@link AccountCreateTransaction} then the transaction ID is lost.
     *
     * You should build and execute your own {@link AccountCreateTransaction} instead.
     */
    @Deprecated
    public AccountId createAccount(PublicKey publicKey, long initialBalance) throws HederaException, HederaNetworkException {
        TransactionReceipt receipt = new AccountCreateTransaction(this).setKey(publicKey)
            .setInitialBalance(initialBalance)
            .executeForReceipt();

        return receipt.getAccountId();
    }

    /**
     * @deprecated hides useful configuration parameters for accounts and makes it difficult to
     * handle errors properly; if an error occurs while fetching the receipt for the
     * {@link AccountCreateTransaction} then the transaction ID is lost.
     *
     * You should build and execute your own {@link AccountCreateTransaction} instead.
     */
    @Deprecated
    public void createAccountAsync(PublicKey publicKey, long initialBalance, Consumer<AccountId> onSuccess, Consumer<HederaThrowable> onError) {
        new AccountCreateTransaction(this).setKey(publicKey)
            .setInitialBalance(initialBalance)
            .executeForReceiptAsync(receipt -> onSuccess.accept(receipt.getAccountId()), onError);
    }

    public AccountInfo getAccount(AccountId id) throws HederaException, HederaNetworkException {
        return new AccountInfoQuery(this).setAccountId(id)
            .execute();
    }

    public void getAccountAsync(AccountId id, Consumer<AccountInfo> onSuccess, Consumer<HederaThrowable> onError) {
        new AccountInfoQuery(this).setAccountId(id)
            .executeAsync(onSuccess, onError);
    }

    public long getAccountBalance(AccountId id) throws HederaException, HederaNetworkException {
        return new AccountBalanceQuery(this).setAccountId(id)
            .execute();
    }

    public void getAccountBalanceAsync(AccountId id, Consumer<Long> onSuccess, Consumer<HederaThrowable> onError) {
        new AccountBalanceQuery(this).setAccountId(id)
            .executeAsync(onSuccess, onError);
    }

    /**
     * @deprecated difficult to overload for multi-party transfers; additionally,
     * if an error occurs while fetching the receipt for the {@link CryptoTransferTransaction} then
     * the transaction ID is lost.
     *
     * You should build and execute your own {@link CryptoTransferTransaction} instead.
     */
    @Deprecated
    public TransactionId transferCryptoTo(AccountId recipient, long amount) throws HederaException, HederaNetworkException {
        return new CryptoTransferTransaction(this).addSender(Objects.requireNonNull(operatorId), amount)
            .addRecipient(recipient, amount)
            .execute();
    }

    /**
     * @deprecated difficult to overload for multi-party transfers; additionally,
     * if an error occurs while fetching the receipt for the {@link CryptoTransferTransaction} then
     * the transaction ID is lost.
     *
     * You should build and execute your own {@link CryptoTransferTransaction} instead.
     */
    @Deprecated
    public void transferCryptoToAsync(AccountId recipient, long amount, Consumer<TransactionId> onSuccess, Consumer<HederaThrowable> onError) {
        new CryptoTransferTransaction(this).addSender(Objects.requireNonNull(operatorId), amount)
            .addRecipient(recipient, amount)
            .executeAsync(onSuccess, onError);
    }


    /**
     * Waits 10 seconds for all channels to finish their calls to their respective nodes.
     *
     * Any new transactions or queries executed with this client after this call will return an
     * error.
     *
     * If you want to adjust the timeout, use {@link #awaitChannelShutdown(long, TimeUnit)} instead.
     *
     * @throws InterruptedException if the thread is interrupted during shutdown.
     * @throws TimeoutException if 10 seconds elapses before the channels are closed.
     */
    @Override
    public void close() throws InterruptedException, TimeoutException {
        awaitChannelShutdown(10, TimeUnit.SECONDS);
    }

    /**
     * Wait for all channels to finish their calls to their respective nodes.
     *
     * Any new transactions or queries executed with this client after this call will return an
     * error.
     *
     * @param timeout the timeout amount for the entire shutdown operation (not per channel).
     * @param timeUnit the unit of the timeout amount.
     * @throws InterruptedException if the thread is interrupted during shutdown.
     * @throws TimeoutException if the timeout elapses before all channels are shutdown.
     */
    public void awaitChannelShutdown(long timeout, TimeUnit timeUnit) throws InterruptedException, TimeoutException {
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
}
