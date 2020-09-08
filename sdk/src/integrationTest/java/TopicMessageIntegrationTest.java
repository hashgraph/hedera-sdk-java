import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.TopicMessageQuery;
import com.hedera.hashgraph.sdk.TopicMessageSubmitTransaction;
import com.hedera.hashgraph.sdk.TopicCreateTransaction;
import com.hedera.hashgraph.sdk.TopicDeleteTransaction;
import com.hedera.hashgraph.sdk.TopicInfoQuery;
import com.hedera.hashgraph.sdk.Hbar;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TopicMessageIntegrationTest {
    @Test
    void test() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();

            client.setMirrorNetwork(List.of("api.testnet.kabuto.sh:50211"));

            var operatorKey = client.getOperatorKey();

            var response = new TopicCreateTransaction()
                .setAdminKey(operatorKey)
                .setTopicMemo("[e2e::TopicCreateTransaction]")
                .setMaxTransactionFee(new Hbar(5))
                .execute(client);

            var receipt = response
                .transactionId
                .getReceipt(client);

            assertNotNull(receipt.topicId);
            assertTrue(Objects.requireNonNull(receipt.topicId).num > 0);

            var topic = receipt.topicId;

            System.out.println("Topic: " + topic);

            @Var var info = new TopicInfoQuery()
                .setTopicId(topic)
                .setNodeId(response.nodeId)
                .setQueryPayment(new Hbar(22))
                .execute(client);

            assertEquals(info.topicId, topic);
            assertEquals(info.topicMemo, "[e2e::TopicCreateTransaction]");
            assertEquals(info.sequenceNumber, 0);
            assertEquals(info.adminKey, operatorKey);

            var handle = new TopicMessageQuery()
              .setTopicId(topic)
              .subscribe(client, (message) -> {
                  System.out.println("Received message");
                  System.out.println(new String(message.contents, StandardCharsets.UTF_8));
              });

            for (var i = 0; i < 100; ++i) {
                new TopicMessageSubmitTransaction()
                  .setNodeId(response.nodeId)
                  .setTopicId(topic)
                  .setMessage("[ " + i + " ] Hello from HCS!")
                  .execute(client);

                System.out.println("Sent #" + i);

                try {
                  Thread.sleep(2500);
                } catch (InterruptedException e) {
                  // Do nothing
                }
            }

            handle.unsubscribe();

            new TopicDeleteTransaction()
                .setTopicId(topic)
                .setMaxTransactionFee(new Hbar(5))
                .execute(client)
                .transactionId
                .getReceipt(client);

            client.close();
        });
    }
}
