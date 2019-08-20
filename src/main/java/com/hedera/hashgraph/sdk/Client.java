package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.account.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.account.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.account.AccountInfo;
import com.hedera.hashgraph.sdk.account.AccountInfoQuery;
import com.hedera.hashgraph.sdk.account.CryptoTransferTransaction;
import com.hedera.hashgraph.sdk.crypto.Key;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;

public final class Client {
    final Random random = new Random();
    private Map<AccountId, Node> channels;

    static final long DEFAULT_MAX_TXN_FEE = 100_000;

    // todo: transaction fees should be defaulted to whatever the transaction fee schedule is
    private long maxTransactionFee = DEFAULT_MAX_TXN_FEE;
    // require users to opt into query payment
    private long maxQueryPayment = 0;

    @Nullable
    private AccountId operatorId;

    @Nullable
    private Ed25519PrivateKey operatorKey;

    public Client(Map<AccountId, String> nodes) {

        if (nodes.isEmpty()) {
            throw new IllegalArgumentException("List of nodes must not be empty");
        }

        channels = nodes.entrySet()
            .stream()
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, t -> new Node(t.getKey(), t.getValue())));
    }

    /**
     * Set the maximum fee to be paid for transactions executed by this client.
     *
     * Because transaction fees are always maximums, this will simply add a call to
     * {@link TransactionBuilder#setTransactionFee(long)} on every new transaction. The actual
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
        if (channels.isEmpty()) {
            throw new IllegalStateException("List of channels has become empty");
        }

        var r = random.nextInt(channels.size());
        var channelIter = channels.values()
            .iterator();

        for (int i = 1; i < r; i++) {
            channelIter.next();
        }

        return channelIter.next();
    }

    Node getNodeForId(AccountId node) {
        var selectedChannel = channels.get(node);

        if (selectedChannel == null) {
            throw new IllegalArgumentException("Node Id does not exist");
        }

        return selectedChannel;
    }

    //
    // Simplified interface intended for high-level, opinionated operation
    //

    public AccountId createAccount(Key publicKey, long initialBalance) throws HederaException, HederaNetworkException {
        var receipt = new AccountCreateTransaction(this).setKey(publicKey)
            .setInitialBalance(initialBalance)
            .executeForReceipt();

        return receipt.getAccountId();
    }

    public void createAccountAsync(Key publicKey, long initialBalance, Consumer<AccountId> onSuccess, Consumer<HederaThrowable> onError) {
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

    public TransactionId transferCryptoTo(AccountId recipient, long amount) throws HederaException, HederaNetworkException {
        return new CryptoTransferTransaction(this).addSender(Objects.requireNonNull(operatorId), amount)
            .addRecipient(recipient, amount)
            .execute();
    }

    public void transferCryptoToAsync(AccountId recipient, long amount, Consumer<TransactionId> onSuccess, Consumer<HederaThrowable> onError) {
        new CryptoTransferTransaction(this).addSender(Objects.requireNonNull(operatorId), amount)
            .addRecipient(recipient, amount)
            .executeAsync(onSuccess, onError);
    }
}
