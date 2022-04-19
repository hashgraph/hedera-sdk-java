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
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.TopicCreateTransaction;
import com.hedera.hashgraph.sdk.TopicId;
import com.hedera.hashgraph.sdk.TopicMessageQuery;
import com.hedera.hashgraph.sdk.TopicMessageSubmitTransaction;
import com.hedera.hashgraph.sdk.TransactionResponse;
import io.github.cdimascio.dotenv.Dotenv;

import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Random;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * An example of an HCS topic that utilizes a submitKey to limit who can submit messages on the topic.
 * <p>
 * Creates a new HCS topic with a single ED25519 submitKey.
 * Subscribes to the topic (no key required).
 * Publishes a number of messages to the topic signed by the submitKey.
 */
public class ConsensusPubSubWithSubmitKeyExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private final int messagesToPublish;
    private final int millisBetweenMessages;
    private Client client;
    private TopicId topicId;
    private PrivateKey submitKey;

    public ConsensusPubSubWithSubmitKeyExample(int messagesToPublish, int millisBetweenMessages) {
        this.messagesToPublish = messagesToPublish;
        this.millisBetweenMessages = millisBetweenMessages;
        setupClient();
    }

    public static void main(String[] args) throws TimeoutException, InterruptedException, PrecheckStatusException, ReceiptStatusException {
        new ConsensusPubSubWithSubmitKeyExample(5, 2000).execute();
    }

    public void execute() throws TimeoutException, InterruptedException, PrecheckStatusException, ReceiptStatusException {
        createTopicWithSubmitKey();
        Thread.sleep(5000);

        subscribeToTopic();

        publishMessagesToTopic();
    }

    private void setupClient() {
        client = Client.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for by this
        // account and be signed by this key
<<<<<<< HEAD
        c.setOperator(payerId, payerPrivateKey);
        c.setMirrorNetwork(List.of(Objects.requireNonNull(Dotenv.load().get("MIRROR_NODE_ADDRESS"))));

        client = c;
=======
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);
>>>>>>> main
    }

    /**
     * Generate a brand new ED25519 key pair.
     * <p>
     * Create a new topic with that key as the topic's submitKey; required to sign all future
     * ConsensusMessageSubmitTransactions for that topic.
     */
    private void createTopicWithSubmitKey() throws TimeoutException, PrecheckStatusException, ReceiptStatusException {
        // Generate a Ed25519 private, public key pair
        submitKey = PrivateKey.generateED25519();
        PublicKey submitPublicKey = submitKey.getPublicKey();

        TransactionResponse transactionResponse = new TopicCreateTransaction()
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
            .subscribe(client, (resp) -> {
                String messageAsString = new String(resp.contents, StandardCharsets.UTF_8);

                System.out.println(resp.consensusTimestamp + " received topic message: " + messageAsString);
            });
    }

    /**
     * Publish a list of messages to a topic, signing each transaction with the topic's submitKey.
     */
    private void publishMessagesToTopic() throws TimeoutException, InterruptedException, PrecheckStatusException, ReceiptStatusException {
        Random r = new Random();
        for (int i = 0; i < messagesToPublish; i++) {
            String message = "random message " + r.nextLong();

            System.out.println("Publishing message: " + message);

            new TopicMessageSubmitTransaction()
                .setTopicId(topicId)
                .setMessage(message)
                .freezeWith(client)

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
