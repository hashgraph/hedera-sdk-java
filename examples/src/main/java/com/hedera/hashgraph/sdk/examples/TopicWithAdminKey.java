package com.hedera.hashgraph.sdk.examples;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.Transaction;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicCreateTransaction;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicInfo;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicInfoQuery;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicUpdateTransaction;
import com.hedera.hashgraph.sdk.crypto.ThresholdKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * An example of HCS topic management using a threshold key as the adminKey and going through a key rotation to a new
 * set of keys.
 *
 * Creates a new HCS topic with a 2-of-3 threshold key for the adminKey.
 * Updates the HCS topic to a 3-of-4 threshold key for the adminKey.
 */
class TopicWithAdminKey {
    private Client hapiClient;

    private ConsensusTopicId topicId;
    private Ed25519PrivateKey[] initialAdminKeys;

    private TopicWithAdminKey() {
        setupHapiClient();
    }

    public static void main(String[] args) throws HederaStatusException {
        new TopicWithAdminKey().execute();
    }

    public void execute() throws HederaStatusException {
        createTopicWithAdminKey();

        updateTopicAdminKeyAndMemo();
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

    private void createTopicWithAdminKey() throws HederaStatusException {
        // Generate the initial keys that are part of the adminKey's thresholdKey.
        // 3 ED25519 keys part of a 2-of-3 threshold key.
        initialAdminKeys = new Ed25519PrivateKey[3];
        Arrays.setAll(initialAdminKeys, i -> Ed25519PrivateKey.generate());

        ThresholdKey thresholdKey = new ThresholdKey(2)
            .addAll(Arrays.stream(initialAdminKeys)
                .peek(k -> System.out.println("Adding key to 2-of-3 threshold adminKey: " + k))
                .map(k -> k.publicKey)
                .collect(Collectors.toList()));

        Transaction transaction = new ConsensusTopicCreateTransaction()
            .setMaxTransactionFee(50_000_000L)
            .setTopicMemo("demo topic")
            .setAdminKey(thresholdKey)
            .build(hapiClient);

        // Sign the transaction with 2 of 3 keys that are part of the adminKey threshold key.
        Arrays.stream(initialAdminKeys, 0, 2).forEach(k -> {
            System.out.println("Signing ConsensusTopicCreateTransaction with key " + k);
            transaction.sign(k);
        });

        TransactionId transactionId = transaction.execute(hapiClient);

        topicId = transactionId.getReceipt(hapiClient).getConsensusTopicId();

        System.out.println("Created new topic " + topicId + " with 2-of-3 threshold key as adminKey.");
    }

    private void updateTopicAdminKeyAndMemo() throws HederaStatusException {
        // Generate the new keys that are part of the adminKey's thresholdKey.
        // 4 ED25519 keys part of a 3-of-4 threshold key.
        Ed25519PrivateKey[] newAdminKeys = new Ed25519PrivateKey[4];
        Arrays.setAll(newAdminKeys, i -> Ed25519PrivateKey.generate());

        ThresholdKey thresholdKey = new ThresholdKey(3)
            .addAll(Arrays.stream(newAdminKeys)
                .peek(k -> System.out.println("New key created for 3-of-4 threshold adminKey: " + k))
                .map(k -> k.publicKey)
                .collect(Collectors.toList()));

        Transaction transaction = new ConsensusTopicUpdateTransaction()
            .setMaxTransactionFee(50_000_000L)
            .setTopicId(topicId)
            .setTopicMemo("updated demo topic")
            .setAdminKey(thresholdKey)
            .build(hapiClient);

        // Sign with the initial adminKey. 2 of the 3 keys already part of the topic's adminKey.
        Arrays.stream(initialAdminKeys, 0, 2).forEach(k -> {
            System.out.println("Signing ConsensusTopicUpdateTransaction with initial admin key " + k);
            transaction.sign(k);
        });

        // Sign with the new adminKey. 3 of 4 keys already part of the topic's adminKey.
        Arrays.stream(newAdminKeys, 0, 3).forEach(k -> {
            System.out.println("Signing ConsensusTopicUpdateTransaction with new admin key " + k);
            transaction.sign(k);
        });

        TransactionId transactionId = transaction.execute(hapiClient);

        // Retrieve results post-consensus.
        transactionId.getReceipt(hapiClient);

        System.out.println("Updated topic " + topicId + " with 3-of-4 threshold key as adminKey");

        ConsensusTopicInfo topicInfo = new ConsensusTopicInfoQuery().setTopicId(topicId).execute(hapiClient);
        System.out.println(topicInfo);
    }
}
