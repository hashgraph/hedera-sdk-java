import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import com.hedera.hashgraph.sdk.*;
import com.hedera.hashgraph.sdk.PrecheckStatusException;

import java8.util.J8Arrays;
import io.github.cdimascio.dotenv.Dotenv;

import javax.annotation.Nullable;

/**
 * An example of HCS topic management using a threshold key as the adminKey and going through a key rotation to a new
 * set of keys.
 * <p>
 * Creates a new HCS topic with a 2-of-3 threshold key for the adminKey.
 * Updates the HCS topic to a 3-of-4 threshold key for the adminKey.
 */
class TopicWithAdminKeyExample {
    private Client hapiClient;

    @Nullable
    private TopicId topicId;

    private PrivateKey[] initialAdminKeys;

    private TopicWithAdminKeyExample() {
        setupHapiClient();
    }

    public static void main(String[] args) throws ReceiptStatusException, TimeoutException, PrecheckStatusException {
        new TopicWithAdminKeyExample().execute();
    }

    public void execute() throws ReceiptStatusException, TimeoutException, PrecheckStatusException {
        createTopicWithAdminKey();

        updateTopicAdminKeyAndMemo();
    }

    private void setupHapiClient() {
        // Transaction payer's account ID and ED25519 private key.
        AccountId payerId = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
        PrivateKey payerPrivateKey =
            PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

        String hederaNetwork = Dotenv.load().get("HEDERA_NETWORK");
        String configFile = Dotenv.load().get("CONFIG_FILE");

        Client client;

        if (hederaNetwork != null && hederaNetwork.equals("previewnet")) {
            client = Client.forPreviewnet();
        } else {
            try {
                client = Client.fromConfigFile(configFile != null ? configFile : "");
            } catch (Exception e) {
                client = Client.forTestnet();
            }
        }

        // Defaults the operator account ID and key such that all generated transactions will be paid for by this
        // account and be signed by this key
        hapiClient = client.setOperator(payerId, payerPrivateKey);
    }

    private void createTopicWithAdminKey() throws TimeoutException, PrecheckStatusException, ReceiptStatusException {
        // Generate the initial keys that are part of the adminKey's thresholdKey.
        // 3 ED25519 keys part of a 2-of-3 threshold key.
        initialAdminKeys = new PrivateKey[3];
        J8Arrays.setAll(initialAdminKeys, i -> PrivateKey.generate());

        KeyList thresholdKey = KeyList.withThreshold(2);
        Collections.addAll(thresholdKey, initialAdminKeys);

        Transaction<?> transaction = new TopicCreateTransaction()
            .setTopicMemo("demo topic")
            .setAdminKey(thresholdKey)
            .freezeWith(hapiClient);

        // Sign the transaction with 2 of 3 keys that are part of the adminKey threshold key.
        J8Arrays.stream(initialAdminKeys, 0, 2).forEach(k -> {
            System.out.println("Signing ConsensusTopicCreateTransaction with key " + k);
            transaction.sign(k);
        });

        TransactionResponse transactionResponse = transaction.execute(hapiClient);

        topicId = transactionResponse.getReceipt(hapiClient).topicId;

        System.out.println("Created new topic " + topicId + " with 2-of-3 threshold key as adminKey.");
    }

    private void updateTopicAdminKeyAndMemo() throws TimeoutException, PrecheckStatusException, ReceiptStatusException {
        // Generate the new keys that are part of the adminKey's thresholdKey.
        // 4 ED25519 keys part of a 3-of-4 threshold key.
        PrivateKey[] newAdminKeys = new PrivateKey[4];
        J8Arrays.setAll(newAdminKeys, i -> PrivateKey.generate());

        KeyList thresholdKey = KeyList.withThreshold(3);
        Collections.addAll(thresholdKey, newAdminKeys);

        Transaction<?> transaction = new TopicUpdateTransaction()
            .setTopicId(topicId)
            .setTopicMemo("updated demo topic")
            .setAdminKey(thresholdKey)
            .freezeWith(hapiClient);

        // Sign with the initial adminKey. 2 of the 3 keys already part of the topic's adminKey.
        J8Arrays.stream(initialAdminKeys, 0, 2).forEach(k -> {
            System.out.println("Signing ConsensusTopicUpdateTransaction with initial admin key " + k);
            transaction.sign(k);
        });

        // Sign with the new adminKey. 3 of 4 keys already part of the topic's adminKey.
        J8Arrays.stream(newAdminKeys, 0, 3).forEach(k -> {
            System.out.println("Signing ConsensusTopicUpdateTransaction with new admin key " + k);
            transaction.sign(k);
        });

        TransactionResponse transactionResponse = transaction.execute(hapiClient);

        // Retrieve results post-consensus.
        transactionResponse.getReceipt(hapiClient);

        System.out.println("Updated topic " + topicId + " with 3-of-4 threshold key as adminKey");

        TopicInfo topicInfo = new TopicInfoQuery().setTopicId(topicId).execute(hapiClient);
        System.out.println(topicInfo);
    }
}
