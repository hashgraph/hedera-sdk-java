import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class TopicDeleteIntegrationTest {
    @Test
    @DisplayName("Can delete topic")
    void canDeleteTopic() {
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

            new TopicDeleteTransaction()
                .setTopicId(topicId)
                .setMaxTransactionFee(new Hbar(5))
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }

    @Test
    @DisplayName("Cannot delete immutable topic")
    void cannotDeleteImmutableTopic() {
        // Skip if using PreviewNet
        Assumptions.assumeTrue(!System.getProperty("HEDERA_NETWORK").equals("previewnet"));

        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();

            var response = new TopicCreateTransaction().execute(client);

            var topicId = Objects.requireNonNull(response.getReceipt(client).topicId);

            var error = assertThrows(HederaPreCheckStatusException.class, () -> {
                new TopicDeleteTransaction()
                    .setTopicId(topicId)
                    .setNodeAccountIds(Collections.singletonList(response.nodeId))
                    .execute(client)
                    .getReceipt(client);
            });

            assertTrue(error.getMessage().contains(Status.UNAUTHORIZED.toString()));

            client.close();
        });
    }
}
