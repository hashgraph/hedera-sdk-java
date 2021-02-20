import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class TopicCreateIntegrationTest {
    @Test
    @DisplayName("Can create topic")
    void canCreateTopic() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var response = new TopicCreateTransaction()
                .setAdminKey(operatorKey)
                .setTopicMemo("[e2e::TopicCreateTransaction]")
                .execute(client);

            var topicId = Objects.requireNonNull(response.getReceipt(client).topicId);

            new TopicDeleteTransaction()
                .setTopicId(topicId)
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }

    @Test
    @DisplayName("Can create topic with no field set")
    void canCreateTopicWithNoFieldsSet() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();

            var response = new TopicCreateTransaction().execute(client);
            assertNotNull(response.getReceipt(client).topicId);

            client.close();
        });
    }
}
