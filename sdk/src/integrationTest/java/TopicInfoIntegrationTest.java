import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class TopicInfoIntegrationTest {

    @Test
    @DisplayName("Can query topic info")
    void canQueryTopicInfo() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var response = new TopicCreateTransaction()
                .setAdminKey(operatorKey)
                .setTopicMemo("[e2e::TopicCreateTransaction]")
                .execute(client);

            var topicId = Objects.requireNonNull(response.getReceipt(client).topicId);

            var info = new TopicInfoQuery()
                .setTopicId(topicId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client);

            assertEquals(info.topicMemo, "[e2e::TopicCreateTransaction]");

            new TopicDeleteTransaction()
                .setTopicId(topicId)
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }

    @Test
    @DisplayName("Can get cost for topic info query")
    void getCostQueryTopicInfo() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var response = new TopicCreateTransaction()
                .setAdminKey(operatorKey)
                .setTopicMemo("[e2e::TopicCreateTransaction]")
                .execute(client);

            var topicId = Objects.requireNonNull(response.getReceipt(client).topicId);

            var infoQuery = new TopicInfoQuery()
                .setTopicId(topicId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId));

            var cost = infoQuery.getCost(client);

            assertNotNull(cost);

            var info = infoQuery.execute(client);

            assertEquals(info.topicMemo, "[e2e::TopicCreateTransaction]");

            new TopicDeleteTransaction()
                .setTopicId(topicId)
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }

    @Test
    @DisplayName("Can get cost for topic info query")
    void getCostBigMaxQueryTopicInfo() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var response = new TopicCreateTransaction()
                .setAdminKey(operatorKey)
                .setTopicMemo("[e2e::TopicCreateTransaction]")
                .execute(client);

            var topicId = Objects.requireNonNull(response.getReceipt(client).topicId);

            var infoQuery = new TopicInfoQuery()
                .setTopicId(topicId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setMaxQueryPayment(new Hbar(1000));

            var cost = infoQuery.getCost(client);

            assertNotNull(cost);

            var info = infoQuery.execute(client);

            assertEquals(info.topicMemo, "[e2e::TopicCreateTransaction]");

            new TopicDeleteTransaction()
                .setTopicId(topicId)
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }

    @Test
    @DisplayName("Can get cost for topic info query")
    void getCostSmallMaxQueryTopicInfo() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var response = new TopicCreateTransaction()
                .setAdminKey(operatorKey)
                .setTopicMemo("[e2e::TopicCreateTransaction]")
                .execute(client);

            var topicId = Objects.requireNonNull(response.getReceipt(client).topicId);

            var infoQuery = new TopicInfoQuery()
                .setTopicId(topicId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setMaxQueryPayment(Hbar.fromTinybars(1));

            var cost = infoQuery.getCost(client);

            assertNotNull(cost);

            var error = assertThrows(RuntimeException.class, () -> {
                infoQuery.execute(client);
            });

            assertEquals(error.getMessage(), "com.hedera.hashgraph.sdk.MaxQueryPaymentExceededException: cost for TopicInfoQuery, of "+cost.toString()+", without explicit payment is greater than the maximum allowed payment of 1 tℏ");

            new TopicDeleteTransaction()
                .setTopicId(topicId)
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }

    @Test
    @DisplayName("Can get cost for topic info query")
    void getCostInsufficientTxFeeQueryTopicInfo() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var response = new TopicCreateTransaction()
                .setAdminKey(operatorKey)
                .setTopicMemo("[e2e::TopicCreateTransaction]")
                .execute(client);

            var topicId = Objects.requireNonNull(response.getReceipt(client).topicId);

            var infoQuery = new TopicInfoQuery()
                .setTopicId(topicId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId));

            var cost = infoQuery.getCost(client);

            assertNotNull(cost);

            var error = assertThrows(PrecheckStatusException.class, () -> {
                infoQuery.setQueryPayment(Hbar.fromTinybars(1)).execute(client);
            });

            assertEquals(error.status.toString(), "INSUFFICIENT_TX_FEE");

            new TopicDeleteTransaction()
                .setTopicId(topicId)
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }


}
