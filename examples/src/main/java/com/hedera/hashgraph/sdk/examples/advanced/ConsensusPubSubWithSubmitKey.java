package com.hedera.hashgraph.sdk.examples.advanced;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.consensus.ConsensusClient;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessageSubmitTransaction;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicCreateTransaction;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PublicKey;
import io.github.cdimascio.dotenv.Dotenv;

import java.time.Instant;
import java.util.Objects;
import java.util.Random;

/**
 * An example of an HCS topic that utilizes a submitKey to limit who can submit messages on the topic.
 *
 * Creates a new HCS topic with a single ED25519 submitKey.
 * Subscribes to the topic (no key required).
 * Publishes a number of messages to the topic signed by the submitKey.
 */
class ConsensusPubSubWithSubmitKey extends AdvancedExample {
    private ConsensusClient mirrorNodeClient;

    private int messagesToPublish;
    private int millisBetweenMessages;

    private ConsensusTopicId topicId;
    private Ed25519PrivateKey submitKey;

    public ConsensusPubSubWithSubmitKey(int messagesToPublish, int millisBetweenMessages) {
        this.messagesToPublish = messagesToPublish;
        this.millisBetweenMessages = millisBetweenMessages;
        setupMirrorNodeClient();
    }

    public static void main(String[] args) throws HederaException, InterruptedException {
        new ConsensusPubSubWithSubmitKey(5, 2000).execute();
    }

    public void execute() throws HederaException, InterruptedException {
        createTopicWithSubmitKey();

        subscribeToTopic();

        publishMessagesToTopic();
    }

    private void setupMirrorNodeClient() {
        // Interface used to subscribe to messages on the HCS topic.
        mirrorNodeClient = new ConsensusClient(Objects.requireNonNull(Dotenv.load().get("MIRROR_NODE_ADDRESS")))
            .setErrorHandler(e -> System.out.println("Error in ConsensusClient: " + e));
    }

    private void createTopicWithSubmitKey() throws HederaException {
        // Generate a Ed25519 private, public key pair
        submitKey = Ed25519PrivateKey.generate();
        Ed25519PublicKey submitPublicKey = submitKey.getPublicKey();

        final TransactionId transactionId = new ConsensusTopicCreateTransaction()
            .setMaxTransactionFee(20_000_000L)
            .setSubmitKey(submitPublicKey)
            .execute(getHapiClient());

        topicId = transactionId.getReceipt(getHapiClient()).getConsensusTopicId();
        System.out.println("Created new topic " + topicId + " with ED25519 submitKey of " + submitKey);
    }

    private void subscribeToTopic() {
        mirrorNodeClient.subscribe(topicId, Instant.ofEpochSecond(0), message -> {
            System.out.println("Received message: " + message.getMessageString()
                + ": consensus timestamp " + message.consensusTimestamp
                + ": topic sequence number " + message.sequenceNumber);
        });
    }

    private void publishMessagesToTopic() throws InterruptedException, HederaException {
        Random r = new Random();
        for (int i = 0; i < messagesToPublish; i++) {
            String message = "random message " + r.nextLong();

            System.out.println("Publishing message: " + message);

            new ConsensusMessageSubmitTransaction()
                .setTopicId(topicId)
                .setMessage(message)
                .build(getHapiClient())

                // The transaction is automatically signed by the payer.
                // Due to the topic having a submitKey requirement, additionally sign the transaction with that key.
                .sign(submitKey)

                .execute(getHapiClient())
                .getReceipt(getHapiClient());

            Thread.sleep(millisBetweenMessages);
        }

        Thread.sleep(10000);
    }
}
