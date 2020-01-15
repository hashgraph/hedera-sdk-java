package com.hedera.hashgraph.sdk.examples.advanced;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.Transaction;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicCreateTransaction;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicInfo;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicInfoQuery;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicUpdateTransaction;
import com.hedera.hashgraph.sdk.crypto.ThresholdKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * An example of HCS topic management using a threshold key as the adminKey and going through a key rotation to a new
 * set of keys.
 *
 * Creates a new HCS topic with a 2-of-3 threshold key for the adminKey.
 * Updates the HCS topic to a 3-of-4 threshold key for the adminKey.
 */
class TopicWithAdminKey extends AdvancedExample {
    private ConsensusTopicId topicId;
    private Ed25519PrivateKey[] initialAdminKeys;

    private TopicWithAdminKey() {
    }

    public static void main(String[] args) throws HederaException {
        new TopicWithAdminKey().execute();
    }

    public void execute() throws HederaException {
        createTopicWithAdminKey();

        updateTopicAdminKeyAndMemo();
    }

    private void createTopicWithAdminKey() throws HederaException {
        // Generate the initial keys that are part of the adminKey's thresholdKey.
        // 3 ED25519 keys part of a 2-of-3 threshold key.
        initialAdminKeys = new Ed25519PrivateKey[3];
        Arrays.setAll(initialAdminKeys, i -> Ed25519PrivateKey.generate());

        ThresholdKey thresholdKey = new ThresholdKey(2)
            .addAll(Arrays.stream(initialAdminKeys)
                .peek(k -> System.out.println("Adding key to 2-of-3 threshold adminKey: " + k))
                .map(Ed25519PrivateKey::getPublicKey)
                .collect(Collectors.toList()));

        Transaction transaction = new ConsensusTopicCreateTransaction()
            .setMaxTransactionFee(50_000_000L)
            .setTopicMemo("demo topic")
            .setAdminKey(thresholdKey)
            .build(getHapiClient());

        // Sign the transaction with 2 of 3 keys that are part of the adminKey threshold key.
        Arrays.stream(initialAdminKeys, 0, 2).forEach(k -> {
            System.out.println("Signing ConsensusTopicCreateTransaction with key " + k);
            transaction.sign(k);
        });

        TransactionId transactionId = transaction.execute(getHapiClient());

        topicId = transactionId.getReceipt(getHapiClient()).getConsensusTopicId();

        System.out.println("Created new topic " + topicId + " with 2-of-3 threshold key as adminKey.");
    }

    private void updateTopicAdminKeyAndMemo() throws HederaException {
        // Generate the new keys that are part of the adminKey's thresholdKey.
        // 4 ED25519 keys part of a 3-of-4 threshold key.
        Ed25519PrivateKey[] newAdminKeys = new Ed25519PrivateKey[4];
        Arrays.setAll(newAdminKeys, i -> Ed25519PrivateKey.generate());

        ThresholdKey thresholdKey = new ThresholdKey(3)
            .addAll(Arrays.stream(newAdminKeys)
                .peek(k -> System.out.println("New key created for 3-of-4 threshold adminKey: " + k))
                .map(Ed25519PrivateKey::getPublicKey)
                .collect(Collectors.toList()));

        Transaction transaction = new ConsensusTopicUpdateTransaction()
            .setMaxTransactionFee(50_000_000L)
            .setTopicId(topicId)
            .setTopicMemo("updated demo topic")
            .setAdminKey(thresholdKey)
            .build(getHapiClient());

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

        TransactionId transactionId = transaction.execute(getHapiClient());

        // Retrieve results post-consensus.
        transactionId.getReceipt(getHapiClient());

        System.out.println("Updated topic " + topicId + " with 3-of-4 threshold key as adminKey");

        ConsensusTopicInfo topicInfo = new ConsensusTopicInfoQuery().setTopicId(topicId).execute(getHapiClient());
        System.out.println(topicInfo);
    }
}
