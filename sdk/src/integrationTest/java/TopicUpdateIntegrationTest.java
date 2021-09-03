import com.hedera.hashgraph.sdk.TopicCreateTransaction;
import com.hedera.hashgraph.sdk.TopicDeleteTransaction;
import com.hedera.hashgraph.sdk.TopicInfoQuery;
import com.hedera.hashgraph.sdk.TopicUpdateTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


public class TopicUpdateIntegrationTest {
    @Test
    @DisplayName("Can update topic")
    void canUpdateTopic() throws Exception {
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

        assertEquals("hello", topicInfo.topicMemo);
        assertNull(topicInfo.autoRenewAccountId);

        new TopicDeleteTransaction()
            .setTopicId(topicId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        testEnv.close();
    }
}
