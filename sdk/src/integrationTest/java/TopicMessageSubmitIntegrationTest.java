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
        // Skip if using PreviewNet
        Assumptions.assumeTrue(!System.getProperty("HEDERA_NETWORK").equals("previewnet"));

        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var response = new TopicCreateTransaction()
                .setAdminKey(operatorKey)
                .setTopicMemo("[e2e::TopicCreateTransaction]")
                .setMaxTransactionFee(new Hbar(5))
                .execute(client);

            var topicId = Objects.requireNonNull(response.getReceipt(client).topicId);

            @Var var info = new TopicInfoQuery()
                .setTopicId(topicId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setQueryPayment(new Hbar(22))
                .execute(client);

            assertEquals(info.topicId, topicId);
            assertEquals(info.topicMemo, "[e2e::TopicCreateTransaction]");
            assertEquals(info.sequenceNumber, 0);
            assertEquals(info.adminKey, operatorKey);

            new TopicMessageSubmitTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setTopicId(topicId)
                .setMessage("Hello, from HCS!")
                .execute(client)
                .getReceipt(client);

            info = new TopicInfoQuery()
                .setTopicId(topicId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setQueryPayment(new Hbar(22))
                .execute(client);

            assertEquals(info.topicId, topicId);
            assertEquals(info.topicMemo, "[e2e::TopicCreateTransaction]");
            assertEquals(info.sequenceNumber, 1);
            assertEquals(info.adminKey, operatorKey);

            new TopicDeleteTransaction()
                .setTopicId(topicId)
                .setMaxTransactionFee(new Hbar(5))
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }

    @Test
    @DisplayName("Can submit a large topic message")
    void canSubmitALargeTopicMessage() {
        // Skip if using PreviewNet
        Assumptions.assumeTrue(!System.getProperty("HEDERA_NETWORK").equals("previewnet"));

        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var response = new TopicCreateTransaction()
                .setAdminKey(operatorKey)
                .setTopicMemo("[e2e::TopicCreateTransaction]")
                .execute(client);

            var topicId = Objects.requireNonNull(response.getReceipt(client).topicId);

            @Var var info = new TopicInfoQuery()
                .setTopicId(topicId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setQueryPayment(new Hbar(22))
                .execute(client);

            assertEquals(info.topicId, topicId);
            assertEquals(info.topicMemo, "[e2e::TopicCreateTransaction]");
            assertEquals(info.sequenceNumber, 0);
            assertEquals(info.adminKey, operatorKey);

            var responses = new TopicMessageSubmitTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setTopicId(topicId)
                .setMessage(Contents.BIG_CONTENTS)
                .executeAll(client);

            for (var resp : responses) {
                resp.getReceipt(client);
            }

            info = new TopicInfoQuery()
                .setTopicId(topicId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setQueryPayment(new Hbar(22))
                .execute(client);

            assertEquals(info.topicId, topicId);
            assertEquals(info.topicMemo, "[e2e::TopicCreateTransaction]");
            assertEquals(info.sequenceNumber, 4);
            assertEquals(info.adminKey, operatorKey);

            new TopicDeleteTransaction()
                .setTopicId(topicId)
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }

    @Test
    @DisplayName("Cannot submit message when topic ID is not set")
    void cannotSubmitMessageWhenTopicIDIsNotSet() {
        // Skip if using PreviewNet
        Assumptions.assumeTrue(!System.getProperty("HEDERA_NETWORK").equals("previewnet"));

        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var response = new TopicCreateTransaction()
                .setAdminKey(operatorKey)
                .setTopicMemo("[e2e::TopicCreateTransaction]")
                .execute(client);

            var topicId = Objects.requireNonNull(response.getReceipt(client).topicId);

            var error = assertThrows(HederaPreCheckStatusException.class, () -> {
                new TopicMessageSubmitTransaction()
                    .setMessage(Contents.BIG_CONTENTS)
                    .execute(client)
                    .getReceipt(client);
            });

            new TopicDeleteTransaction()
                .setTopicId(topicId)
                .execute(client)
                .getReceipt(client);

            assertTrue(error.getMessage().contains(Status.INVALID_TOPIC_ID.toString()));

            client.close();
        });
    }

    @Test
    @DisplayName("Cannot submit message when message is not set")
    void cannotSubmitMessageWhenMessageIsNotSet() {
        // Skip if using PreviewNet
        Assumptions.assumeTrue(!System.getProperty("HEDERA_NETWORK").equals("previewnet"));

        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var response = new TopicCreateTransaction()
                .setAdminKey(operatorKey)
                .setTopicMemo("[e2e::TopicCreateTransaction]")
                .execute(client);

            var topicId = Objects.requireNonNull(response.getReceipt(client).topicId);

            var error = assertThrows(HederaPreCheckStatusException.class, () -> {
                new TopicMessageSubmitTransaction()
                    .setTopicId(topicId)
                    .execute(client)
                    .getReceipt(client);
            });

            new TopicDeleteTransaction()
                .setTopicId(topicId)
                .execute(client)
                .getReceipt(client);

            assertTrue(error.getMessage().contains(Status.INVALID_TOPIC_MESSAGE.toString()));

            client.close();
        });
    }
}
