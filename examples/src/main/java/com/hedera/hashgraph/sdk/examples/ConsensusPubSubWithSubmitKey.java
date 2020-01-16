package com.hedera.hashgraph.sdk.examples;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.consensus.ConsensusClient;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessageSubmitTransaction;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicCreateTransaction;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PublicKey;
import io.github.cdimascio.dotenv.Dotenv;
import org.spongycastle.util.encoders.Hex;

import java.time.Instant;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

/**
 * An example of an HCS topic that utilizes a submitKey to limit who can submit messages on the topic.
 *
 * Creates a new HCS topic with a single ED25519 submitKey.
 * Subscribes to the topic (no key required).
 * Publishes a number of messages to the topic signed by the submitKey.
 */
public class ConsensusPubSubWithSubmitKey {
    private Client hapiClient;
    private ConsensusClient mirrorNodeClient;

    private int messagesToPublish;
    private int millisBetweenMessages;

    private ConsensusTopicId topicId;
    private Ed25519PrivateKey submitKey;

    public ConsensusPubSubWithSubmitKey(int messagesToPublish, int millisBetweenMessages) {
        this.messagesToPublish = messagesToPublish;
        this.millisBetweenMessages = millisBetweenMessages;
        setupHapiClient();
        setupMirrorNodeClient();
    }

    public static void main(String[] args) throws InterruptedException, HederaStatusException {
        new ConsensusPubSubWithSubmitKey(5, 2000).execute();
    }

    public void execute() throws InterruptedException, HederaStatusException {
        createTopicWithSubmitKey();

        subscribeToTopic();

        publishMessagesToTopic();
    }

    private void setupHapiClient() {
        // see `.env.sample` in the repository root for how to specify these values or set environment variables with
        // the same names

        // The Hedera Hashgraph node's IP address, port, and account ID.
        AccountId nodeAccountId = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("NODE_ID")));
        String nodeAddress = Objects.requireNonNull(Dotenv.load().get("NODE_ADDRESS"));

        // Transaction payer's account ID and ED25519 private key.
        AccountId payerId = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
        Ed25519PrivateKey payerPrivateKey =
            Ed25519PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

        // Interface used to publish messages on the HCS topic.
        hapiClient = new Client(new HashMap<AccountId, String>() {
            {
                put(nodeAccountId, nodeAddress);
            }
        });

        // Defaults the operator account ID and key such that all generated transactions will be paid for by this
        // account and be signed by this key
        hapiClient.setOperator(payerId, payerPrivateKey);
    }

    private void setupMirrorNodeClient() {
        // Interface used to subscribe to messages on the HCS topic.
        mirrorNodeClient = new ConsensusClient(Objects.requireNonNull(Dotenv.load().get("MIRROR_NODE_ADDRESS")))
            .setErrorHandler(e -> System.out.println("Error in ConsensusClient: " + e));
    }

    /**
     * Generate a brand new ED25519 key pair.
     *
     * Create a new topic with that key as the topic's submitKey; required to sign all future
     * ConsensusMessageSubmitTransactions for that topic.
     *
     * @throws HederaStatusException
     */
    private void createTopicWithSubmitKey() throws HederaStatusException {
        // Generate a Ed25519 private, public key pair
        submitKey = Ed25519PrivateKey.generate();
        Ed25519PublicKey submitPublicKey = submitKey.publicKey;

        final TransactionId transactionId = new ConsensusTopicCreateTransaction()
            .setMaxTransactionFee(20_000_000L)
            .setTopicMemo("HCS topic with submit key")
            .setSubmitKey(submitPublicKey)
            .execute(hapiClient);

        topicId = transactionId.getReceipt(hapiClient).getConsensusTopicId();
        System.out.println("Created new topic " + topicId + " with ED25519 submitKey of " + submitKey);
    }

    /**
     * Subscribe to messages on the topic, printing out the received message and metadata as it is published by the
     * Hedera mirror node.
     */
    private void subscribeToTopic() {
        mirrorNodeClient.subscribe(topicId, Instant.ofEpochSecond(0), message -> {
            System.out.println("Received message: " + message.getMessageString()
                + " consensus timestamp: " + message.consensusTimestamp
                + " topic sequence number: " + message.sequenceNumber
                + " topic running hash: " + Hex.toHexString(message.runningHash));
        });
    }

    /**
     * Publish a list of messages to a topic, signing each transaction with the topic's submitKey.
     * @throws InterruptedException
     * @throws HederaStatusException
     */
    private void publishMessagesToTopic() throws InterruptedException, HederaStatusException {
        Random r = new Random();
        for (int i = 0; i < messagesToPublish; i++) {
            String message = "random message " + r.nextLong();

            System.out.println("Publishing message: " + message);

            new ConsensusMessageSubmitTransaction()
                .setTopicId(topicId)
                .setMessage(message)
                .build(hapiClient)

                // The transaction is automatically signed by the payer.
                // Due to the topic having a submitKey requirement, additionally sign the transaction with that key.
                .sign(submitKey)

                .execute(hapiClient)
                .getReceipt(hapiClient);

            Thread.sleep(millisBetweenMessages);
        }

        Thread.sleep(10000);
    }
}
