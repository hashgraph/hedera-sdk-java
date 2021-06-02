import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class TopicMessageSubmitIntegrationTest {
    @Test
    @DisplayName("Can submit a topic message")
    void canSubmitATopicMessage() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var response = new TopicCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setAdminKey(testEnv.operatorKey)
                .setTopicMemo("[e2e::TopicCreateTransaction]")
                .execute(testEnv.client);

            var topicId = Objects.requireNonNull(response.getReceipt(testEnv.client).topicId);

            @Var var info = new TopicInfoQuery()
                .setTopicId(topicId)
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .execute(testEnv.client);

            assertEquals(info.topicId, topicId);
            assertEquals(info.topicMemo, "[e2e::TopicCreateTransaction]");
            assertEquals(info.sequenceNumber, 0);
            assertEquals(info.adminKey, testEnv.operatorKey.getPublicKey());

            new TopicMessageSubmitTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTopicId(topicId)
                .setMessage("Hello, from HCS!")
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            info = new TopicInfoQuery()
                .setTopicId(topicId)
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .execute(testEnv.client);

            assertEquals(info.topicId, topicId);
            assertEquals(info.topicMemo, "[e2e::TopicCreateTransaction]");
            assertEquals(info.sequenceNumber, 1);
            assertEquals(info.adminKey, testEnv.operatorKey.getPublicKey());

            new TopicDeleteTransaction()
                .setTopicId(topicId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Can submit a large topic message")
    void canSubmitALargeTopicMessage() {
        // Skip if using PreviewNet
        Assumptions.assumeTrue(!System.getProperty("HEDERA_NETWORK").equals("previewnet"));

        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var response = new TopicCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setAdminKey(testEnv.operatorKey)
                .setTopicMemo("[e2e::TopicCreateTransaction]")
                .execute(testEnv.client);

            var topicId = Objects.requireNonNull(response.getReceipt(testEnv.client).topicId);

            @Var var info = new TopicInfoQuery()
                .setTopicId(topicId)
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .execute(testEnv.client);

            assertEquals(info.topicId, topicId);
            assertEquals(info.topicMemo, "[e2e::TopicCreateTransaction]");
            assertEquals(info.sequenceNumber, 0);
            assertEquals(info.adminKey, testEnv.operatorKey.getPublicKey());

            var responses = new TopicMessageSubmitTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTopicId(topicId)
                .setMaxChunks(15)
                .setMessage(Contents.BIG_CONTENTS)
                .executeAll(testEnv.client);

            for (var resp : responses) {
                resp.getReceipt(testEnv.client);
            }

            info = new TopicInfoQuery()
                .setTopicId(topicId)
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .execute(testEnv.client);

            assertEquals(info.topicId, topicId);
            assertEquals(info.topicMemo, "[e2e::TopicCreateTransaction]");
            assertEquals(info.sequenceNumber, 14);
            assertEquals(info.adminKey, testEnv.operatorKey.getPublicKey());

            new TopicDeleteTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTopicId(topicId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Cannot submit message when topic ID is not set")
    void cannotSubmitMessageWhenTopicIDIsNotSet() {
        // Skip if using PreviewNet
        Assumptions.assumeTrue(!System.getProperty("HEDERA_NETWORK").equals("previewnet"));

        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var response = new TopicCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setAdminKey(testEnv.operatorKey)
                .setTopicMemo("[e2e::TopicCreateTransaction]")
                .execute(testEnv.client);

            var topicId = Objects.requireNonNull(response.getReceipt(testEnv.client).topicId);

            var error = assertThrows(ReceiptStatusException.class, () -> {
                new TopicMessageSubmitTransaction()
                    .setNodeAccountIds(testEnv.nodeAccountIds)
                    .setMessage(Contents.BIG_CONTENTS)
                    .setMaxChunks(15)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

            new TopicDeleteTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTopicId(topicId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            assertTrue(error.getMessage().contains(Status.INVALID_TOPIC_ID.toString()));

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Cannot submit message when message is not set")
    void cannotSubmitMessageWhenMessageIsNotSet() {
        // Skip if using PreviewNet
        Assumptions.assumeTrue(!System.getProperty("HEDERA_NETWORK").equals("previewnet"));

        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var response = new TopicCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setAdminKey(testEnv.operatorKey)
                .setTopicMemo("[e2e::TopicCreateTransaction]")
                .execute(testEnv.client);

            var topicId = Objects.requireNonNull(response.getReceipt(testEnv.client).topicId);

            var error = assertThrows(ReceiptStatusException.class, () -> {
                new TopicMessageSubmitTransaction()
                    .setNodeAccountIds(testEnv.nodeAccountIds)
                    .setTopicId(topicId)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

            new TopicDeleteTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTopicId(topicId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            assertTrue(error.getMessage().contains(Status.INVALID_TOPIC_MESSAGE.toString()));

            testEnv.client.close();
        });
    }
}
