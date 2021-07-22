import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TopicDeleteIntegrationTest {
    @Test
    @DisplayName("Can delete topic")
    void canDeleteTopic() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv(1);

            var response = new TopicCreateTransaction()
                .setAdminKey(testEnv.operatorKey)
                .setTopicMemo("[e2e::TopicCreateTransaction]")
                .execute(testEnv.client);

            var topicId = Objects.requireNonNull(response.getReceipt(testEnv.client).topicId);

            new TopicDeleteTransaction()
                .setTopicId(topicId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.close();
        });
    }

    @Test
    @DisplayName("Cannot delete immutable topic")
    void cannotDeleteImmutableTopic() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv(1);

            var response = new TopicCreateTransaction()
                .execute(testEnv.client);

            var topicId = Objects.requireNonNull(response.getReceipt(testEnv.client).topicId);

            var error = assertThrows(ReceiptStatusException.class, () -> {
                new TopicDeleteTransaction()
                    .setTopicId(topicId)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.UNAUTHORIZED.toString()));

            testEnv.close();
        });
    }
}
