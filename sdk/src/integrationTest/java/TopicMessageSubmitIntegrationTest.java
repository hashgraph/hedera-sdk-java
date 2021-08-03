import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TopicCreateTransaction;
import com.hedera.hashgraph.sdk.TopicDeleteTransaction;
import com.hedera.hashgraph.sdk.TopicInfoQuery;
import com.hedera.hashgraph.sdk.TopicMessageSubmitTransaction;
import com.hedera.hashgraph.sdk.Transaction;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TopicMessageSubmitIntegrationTest {
    @Test
    @DisplayName("Can submit a topic message")
    void canSubmitATopicMessage() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv(1);

            var response = new TopicCreateTransaction()
                .setAdminKey(testEnv.operatorKey)
                .setTopicMemo("[e2e::TopicCreateTransaction]")
                .execute(testEnv.client);

            var topicId = Objects.requireNonNull(response.getReceipt(testEnv.client).topicId);

            @Var var info = new TopicInfoQuery()
                .setTopicId(topicId)
                .execute(testEnv.client);

            assertEquals(info.topicId, topicId);
            assertEquals(info.topicMemo, "[e2e::TopicCreateTransaction]");
            assertEquals(info.sequenceNumber, 0);
            assertEquals(info.adminKey, testEnv.operatorKey);

            new TopicMessageSubmitTransaction()
                .setTopicId(topicId)
                .setMessage("Hello, from HCS!")
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            info = new TopicInfoQuery()
                .setTopicId(topicId)
                .execute(testEnv.client);

            assertEquals(info.topicId, topicId);
            assertEquals(info.topicMemo, "[e2e::TopicCreateTransaction]");
            assertEquals(info.sequenceNumber, 1);
            assertEquals(info.adminKey, testEnv.operatorKey);

            new TopicDeleteTransaction()
                .setTopicId(topicId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.close();
        });
    }

    @Test
    @DisplayName("Can submit a large topic message")
    void canSubmitALargeTopicMessage() {
        // Skip if using PreviewNet
        Assumptions.assumeTrue(!System.getProperty("HEDERA_NETWORK").equals("previewnet"));

        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv(1);

            var response = new TopicCreateTransaction()
                .setAdminKey(testEnv.operatorKey)
                .setTopicMemo("[e2e::TopicCreateTransaction]")
                .execute(testEnv.client);

            var topicId = Objects.requireNonNull(response.getReceipt(testEnv.client).topicId);

            @Var var info = new TopicInfoQuery()
                .setTopicId(topicId)
                .execute(testEnv.client);

            assertEquals(info.topicId, topicId);
            assertEquals(info.topicMemo, "[e2e::TopicCreateTransaction]");
            assertEquals(info.sequenceNumber, 0);
            assertEquals(info.adminKey, testEnv.operatorKey);

            var responses = new TopicMessageSubmitTransaction()
                .setTopicId(topicId)
                .setMaxChunks(15)
                .setMessage(Contents.BIG_CONTENTS)
                .executeAll(testEnv.client);

            for (var resp : responses) {
                resp.getReceipt(testEnv.client);
            }

            info = new TopicInfoQuery()
                .setTopicId(topicId)
                .execute(testEnv.client);

            assertEquals(info.topicId, topicId);
            assertEquals(info.topicMemo, "[e2e::TopicCreateTransaction]");
            assertEquals(info.sequenceNumber, 14);
            assertEquals(info.adminKey, testEnv.operatorKey);

            new TopicDeleteTransaction()
                .setTopicId(topicId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.close();
        });
    }

    @Test
    @DisplayName("Cannot submit message when topic ID is not set")
    void cannotSubmitMessageWhenTopicIDIsNotSet() {
        // Skip if using PreviewNet
        Assumptions.assumeTrue(!System.getProperty("HEDERA_NETWORK").equals("previewnet"));

        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv(1);

            var response = new TopicCreateTransaction()
                .setAdminKey(testEnv.operatorKey)
                .setTopicMemo("[e2e::TopicCreateTransaction]")
                .execute(testEnv.client);

            var topicId = Objects.requireNonNull(response.getReceipt(testEnv.client).topicId);

            var error = assertThrows(ReceiptStatusException.class, () -> {
                new TopicMessageSubmitTransaction()
                    .setMessage(Contents.BIG_CONTENTS)
                    .setMaxChunks(15)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

            new TopicDeleteTransaction()
                .setTopicId(topicId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            assertTrue(error.getMessage().contains(Status.INVALID_TOPIC_ID.toString()));

            testEnv.close();
        });
    }

    @Test
    @DisplayName("Cannot submit message when message is not set")
    void cannotSubmitMessageWhenMessageIsNotSet() {
        // Skip if using PreviewNet
        Assumptions.assumeTrue(!System.getProperty("HEDERA_NETWORK").equals("previewnet"));

        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv(1);

            var response = new TopicCreateTransaction()
                .setAdminKey(testEnv.operatorKey)
                .setTopicMemo("[e2e::TopicCreateTransaction]")
                .execute(testEnv.client);

            var topicId = Objects.requireNonNull(response.getReceipt(testEnv.client).topicId);

            var error = assertThrows(ReceiptStatusException.class, () -> {
                new TopicMessageSubmitTransaction()
                    .setTopicId(topicId)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

            new TopicDeleteTransaction()
                .setTopicId(topicId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            assertTrue(error.getMessage().contains(Status.INVALID_TOPIC_MESSAGE.toString()));

            testEnv.close();
        });
    }

    @Test
    @DisplayName("Hex Decode Regression Test")
    void decodeHexRegressionTest() {
        assertDoesNotThrow(() -> {
            String binaryHex = "2ac2010a580a130a0b08d38f8f880610a09be91512041899e11c120218041880c2d72f22020878da01330a0418a5a1201210303030303030313632373633373731351a190a130a0b08d38f8f880610a09be91512041899e11c1001180112660a640a20603edaec5d1c974c92cb5bee7b011310c3b84b13dc048424cd6ef146d6a0d4a41a40b6a08f310ee29923e5868aac074468b2bde05da95a806e2f4a4f452177f129ca0abae7831e595b5beaa1c947e2cb71201642bab33fece5184b04547afc40850a";
            byte[] transactionBytes = Hex.decode(binaryHex);

            Transaction transaction = Transaction.fromBytes(transactionBytes);

            String idString = transaction.getTransactionId().toString();
            String transactionString = transaction.toString();
        });
    }
}
