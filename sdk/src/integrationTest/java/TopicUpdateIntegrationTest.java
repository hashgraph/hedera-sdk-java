import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


public class TopicUpdateIntegrationTest {
    @Test
    @DisplayName("Can update topic")
    void canUpdateTopic() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv(1);

            var response = new TopicCreateTransaction()
                .setAdminKey(testEnv.operatorKey)
                .setAutoRenewAccountId(testEnv.operatorId)
                .setTopicMemo("[e2e::TopicCreateTransaction]")
                .execute(testEnv.client);

            var topicId = Objects.requireNonNull(response.getReceipt(testEnv.client).topicId);

            new TopicUpdateTransaction()
                .clearAutoRenewAccountId()
                .setTopicMemo("hello")
                .setTopicId(topicId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var topicInfo = new TopicInfoQuery()
                .setTopicId(topicId)
                .execute(testEnv.client);

            assertEquals(topicInfo.topicMemo, "hello");
            assertNull(topicInfo.autoRenewAccountId);

            new TopicDeleteTransaction()
                .setTopicId(topicId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.close();
        });
    }
}
