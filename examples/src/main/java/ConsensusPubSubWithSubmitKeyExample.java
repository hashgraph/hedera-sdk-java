import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaPreCheckStatusException;
import com.hedera.hashgraph.sdk.HederaReceiptStatusException;
import com.hedera.hashgraph.sdk.TopicMessageSubmitTransaction;
import com.hedera.hashgraph.sdk.TopicMessageQuery;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.TopicCreateTransaction;
import com.hedera.hashgraph.sdk.TopicId;

import io.github.cdimascio.dotenv.Dotenv;
import java8.util.Lists;
import org.threeten.bp.Instant;

/**
 * An example of an HCS topic that utilizes a submitKey to limit who can submit messages on the topic.
 * <p>
 * Creates a new HCS topic with a single ED25519 submitKey.
 * Subscribes to the topic (no key required).
 * Publishes a number of messages to the topic signed by the submitKey.
 */
public class ConsensusPubSubWithSubmitKeyExample {
    private Client client;

    private final int messagesToPublish;
    private final int millisBetweenMessages;

    private TopicId topicId;
    private PrivateKey submitKey;

    public ConsensusPubSubWithSubmitKeyExample(int messagesToPublish, int millisBetweenMessages) {
        this.messagesToPublish = messagesToPublish;
        this.millisBetweenMessages = millisBetweenMessages;
        setupClient();
    }

    public static void main(String[] args) throws TimeoutException, InterruptedException, HederaPreCheckStatusException, HederaReceiptStatusException {
        new ConsensusPubSubWithSubmitKeyExample(5, 2000).execute();
    }

    public void execute() throws TimeoutException, InterruptedException, HederaPreCheckStatusException, HederaReceiptStatusException {
        createTopicWithSubmitKey();
        Thread.sleep(5000);

        subscribeToTopic();

        publishMessagesToTopic();
    }

    private void setupClient() {
        // Transaction payer's account ID and ED25519 private key.
        AccountId payerId = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
        PrivateKey payerPrivateKey =
            PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

        // Interface used to publish messages on the HCS topic.
        client = Client.forTestnet();

        // Defaults the operator account ID and key such that all generated transactions will be paid for by this
        // account and be signed by this key
        client.setOperator(payerId, payerPrivateKey);

        client.setMirrorNetwork(Lists.of(Objects.requireNonNull(Dotenv.load().get("MIRROR_NODE_ADDRESS"))));
    }

    /**
     * Generate a brand new ED25519 key pair.
     * <p>
     * Create a new topic with that key as the topic's submitKey; required to sign all future
     * ConsensusMessageSubmitTransactions for that topic.
     */
    private void createTopicWithSubmitKey() throws TimeoutException, HederaPreCheckStatusException, HederaReceiptStatusException, InterruptedException {
        // Generate a Ed25519 private, public key pair
        submitKey = PrivateKey.generate();
        PublicKey submitPublicKey = submitKey.getPublicKey();

        var transactionResponse = new TopicCreateTransaction()
            .setTopicMemo("HCS topic with submit key")
            .setSubmitKey(submitPublicKey)
            .execute(client);


        topicId = Objects.requireNonNull(transactionResponse.getReceipt(client).topicId);
        System.out.println("Created new topic " + topicId + " with ED25519 submitKey of " + submitKey);
    }

    /**
     * Subscribe to messages on the topic, printing out the received message and metadata as it is published by the
     * Hedera mirror node.
     */
    private void subscribeToTopic() {
        new TopicMessageQuery()
            .setTopicId(topicId)
            .setStartTime(Instant.ofEpochSecond(0))
            .subscribe(client, System.out::println);
    }

    /**
     * Publish a list of messages to a topic, signing each transaction with the topic's submitKey.
     */
    private void publishMessagesToTopic() throws TimeoutException, InterruptedException, HederaPreCheckStatusException, HederaReceiptStatusException {
        Random r = new Random();
        for (int i = 0; i < messagesToPublish; i++) {
            String message = "random message " + r.nextLong();

            System.out.println("Publishing message: " + message);

            new TopicMessageSubmitTransaction()
                .setTopicId(topicId)
                .setMessage(message)
                .build(client)

                // The transaction is automatically signed by the payer.
                // Due to the topic having a submitKey requirement, additionally sign the transaction with that key.
                .sign(submitKey)

                .execute(client)
                .transactionId
                .getReceipt(client);

            Thread.sleep(millisBetweenMessages);
        }

        Thread.sleep(10000);
    }
}
