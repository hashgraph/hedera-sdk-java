import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TopicInfoIntegrationTest {

    @Test
    @DisplayName("Can query topic info")
    void canQueryTopicInfo() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var response = new TopicCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setAdminKey(testEnv.operatorKey)
                .setTopicMemo("[e2e::TopicCreateTransaction]")
                .execute(testEnv.client);

            var topicId = Objects.requireNonNull(response.getReceipt(testEnv.client).topicId);

            var info = new TopicInfoQuery()
                .setTopicId(topicId)
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .execute(testEnv.client);

            assertEquals(info.topicMemo, "[e2e::TopicCreateTransaction]");

            new TopicDeleteTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTopicId(topicId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Can get cost for topic info query")
    void getCostQueryTopicInfo() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var response = new TopicCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setAdminKey(testEnv.operatorKey)
                .setTopicMemo("[e2e::TopicCreateTransaction]")
                .execute(testEnv.client);

            var topicId = Objects.requireNonNull(response.getReceipt(testEnv.client).topicId);

            var infoQuery = new TopicInfoQuery()
                .setTopicId(topicId)
                .setNodeAccountIds(testEnv.nodeAccountIds);

            var cost = infoQuery.getCost(testEnv.client);

            assertNotNull(cost);

            var info = infoQuery.execute(testEnv.client);

            assertEquals(info.topicMemo, "[e2e::TopicCreateTransaction]");

            new TopicDeleteTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTopicId(topicId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Can get cost for topic info query")
    void getCostBigMaxQueryTopicInfo() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var response = new TopicCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setAdminKey(testEnv.operatorKey)
                .setTopicMemo("[e2e::TopicCreateTransaction]")
                .execute(testEnv.client);

            var topicId = Objects.requireNonNull(response.getReceipt(testEnv.client).topicId);

            var infoQuery = new TopicInfoQuery()
                .setTopicId(topicId)
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setMaxQueryPayment(new Hbar(1000));

            var cost = infoQuery.getCost(testEnv.client);

            assertNotNull(cost);

            var info = infoQuery.execute(testEnv.client);

            assertEquals(info.topicMemo, "[e2e::TopicCreateTransaction]");

            new TopicDeleteTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTopicId(topicId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Can get cost for topic info query")
    void getCostSmallMaxQueryTopicInfo() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var response = new TopicCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setAdminKey(testEnv.operatorKey)
                .setTopicMemo("[e2e::TopicCreateTransaction]")
                .execute(testEnv.client);

            var topicId = Objects.requireNonNull(response.getReceipt(testEnv.client).topicId);

            var infoQuery = new TopicInfoQuery()
                .setTopicId(topicId)
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setMaxQueryPayment(Hbar.fromTinybars(1));

            var cost = infoQuery.getCost(testEnv.client);

            assertNotNull(cost);

            var error = assertThrows(RuntimeException.class, () -> {
                infoQuery.execute(testEnv.client);
            });

            assertEquals(error.getMessage(), "com.hedera.hashgraph.sdk.MaxQueryPaymentExceededException: cost for TopicInfoQuery, of "+cost.toString()+", without explicit payment is greater than the maximum allowed payment of 1 tℏ");

            new TopicDeleteTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTopicId(topicId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Can get cost for topic info query")
    void getCostInsufficientTxFeeQueryTopicInfo() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var response = new TopicCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setAdminKey(testEnv.operatorKey)
                .setTopicMemo("[e2e::TopicCreateTransaction]")
                .execute(testEnv.client);

            var topicId = Objects.requireNonNull(response.getReceipt(testEnv.client).topicId);

            var infoQuery = new TopicInfoQuery()
                .setTopicId(topicId)
                .setNodeAccountIds(testEnv.nodeAccountIds);

            var cost = infoQuery.getCost(testEnv.client);

            assertNotNull(cost);

            var error = assertThrows(PrecheckStatusException.class, () -> {
                infoQuery.setQueryPayment(Hbar.fromTinybars(1)).execute(testEnv.client);
            });

            assertEquals(error.status.toString(), "INSUFFICIENT_TX_FEE");

            new TopicDeleteTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTopicId(topicId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.client.close();
        });
    }


}
