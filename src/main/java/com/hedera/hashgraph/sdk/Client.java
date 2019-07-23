package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.account.*;
import com.hedera.hashgraph.sdk.consensus.*;
import com.hedera.hashgraph.sdk.crypto.Key;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class Client {
    final Random random = new Random();
    private Map<AccountId, Node> channels;

    static final long DEFAULT_MAX_TXN_FEE = 100_000;

    // todo: transaction fees should be defaulted to whatever the transaction fee schedule is
    private long maxTransactionFee = DEFAULT_MAX_TXN_FEE;

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

    public Client setMaxTransactionFee(@Nonnegative long maxTransactionFee) {
        if (maxTransactionFee <= 0) {
            throw new IllegalArgumentException("maxTransactionFee must be > 0");
        }

        this.maxTransactionFee = maxTransactionFee;
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

    public TopicId createTopic(String topicMemo) throws HederaException, HederaNetworkException {
        var record = new CreateTopicTransaction(this).setTopicMemo(topicMemo)
            .executeForRecord();
        return record.getReceipt().getTopicId();
    }

    public TransactionId deleteTopic(TopicId topicId) throws HederaException, HederaNetworkException {
        return new DeleteTopicTransaction(this).setTopicId(topicId).execute();
    }

    public TransactionId submitMessage(TopicId topicId, byte[] message) throws HederaException, HederaNetworkException {
        return new SubmitMessageTransaction(this).setTopicId(topicId).setMessage(message)
            .execute();
    }

    public TopicInfo getTopicInfo(TopicId topicId) throws HederaException, HederaNetworkException {
        return new GetTopicInfoQuery(this).setTopicId(topicId)
            .execute();
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
