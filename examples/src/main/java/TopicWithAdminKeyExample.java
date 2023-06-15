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
import com.hedera.hashgraph.sdk.KeyList;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.TopicCreateTransaction;
import com.hedera.hashgraph.sdk.TopicId;
import com.hedera.hashgraph.sdk.TopicInfo;
import com.hedera.hashgraph.sdk.TopicInfoQuery;
import com.hedera.hashgraph.sdk.TopicUpdateTransaction;
import com.hedera.hashgraph.sdk.Transaction;
import com.hedera.hashgraph.sdk.TransactionResponse;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.Arrays;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

/**
 * An example of HCS topic management using a threshold key as the adminKey and going through a key rotation to a new
 * set of keys.
 * <p>
 * Creates a new HCS topic with a 2-of-3 threshold key for the adminKey.
 * Updates the HCS topic to a 3-of-4 threshold key for the adminKey.
 */
class TopicWithAdminKeyExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

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
        hapiClient = Client.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for by this
        // account and be signed by this key
        hapiClient.setOperator(OPERATOR_ID, OPERATOR_KEY);
    }

    private void createTopicWithAdminKey() throws TimeoutException, PrecheckStatusException, ReceiptStatusException {
        // Generate the initial keys that are part of the adminKey's thresholdKey.
        // 3 ED25519 keys part of a 2-of-3 threshold key.
        initialAdminKeys = new PrivateKey[3];
        Arrays.setAll(initialAdminKeys, i -> PrivateKey.generate());

        KeyList thresholdKey = KeyList.withThreshold(2);
        Collections.addAll(thresholdKey, initialAdminKeys);

        Transaction<?> transaction = new TopicCreateTransaction()
            .setTopicMemo("demo topic")
            .setAdminKey(thresholdKey)
            .freezeWith(hapiClient);

        // Sign the transaction with 2 of 3 keys that are part of the adminKey threshold key.
        Arrays.stream(initialAdminKeys, 0, 2).forEach(k -> {
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
        Arrays.setAll(newAdminKeys, i -> PrivateKey.generate());

        KeyList thresholdKey = KeyList.withThreshold(3);
        Collections.addAll(thresholdKey, newAdminKeys);

        Transaction<?> transaction = new TopicUpdateTransaction()
            .setTopicId(topicId)
            .setTopicMemo("updated demo topic")
            .setAdminKey(thresholdKey)
            .freezeWith(hapiClient);

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

        TransactionResponse transactionResponse = transaction.execute(hapiClient);

        // Retrieve results post-consensus.
        transactionResponse.getReceipt(hapiClient);

        System.out.println("Updated topic " + topicId + " with 3-of-4 threshold key as adminKey");

        TopicInfo topicInfo = new TopicInfoQuery().setTopicId(topicId).execute(hapiClient);
        System.out.println(topicInfo);
    }
}
