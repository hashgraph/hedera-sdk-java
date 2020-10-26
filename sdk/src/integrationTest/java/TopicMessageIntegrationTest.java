import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.TopicMessageQuery;
import com.hedera.hashgraph.sdk.TopicMessageSubmitTransaction;
import com.hedera.hashgraph.sdk.TopicCreateTransaction;
import com.hedera.hashgraph.sdk.TopicDeleteTransaction;
import com.hedera.hashgraph.sdk.TopicInfoQuery;
import com.hedera.hashgraph.sdk.Hbar;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TopicMessageIntegrationTest {
    @Test
    void test() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();

            client.setMirrorNetwork(List.of("api.testnet.kabuto.sh:50211"));

            var operatorKey = client.getOperatorPublicKey();

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

            @Var var info = new TopicInfoQuery()
                .setTopicId(topic)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setQueryPayment(new Hbar(22))
                .execute(client);

            assertEquals(info.topicId, topic);
            assertEquals(info.topicMemo, "[e2e::TopicCreateTransaction]");
            assertEquals(info.sequenceNumber, 0);
            assertEquals(info.adminKey, operatorKey);

            var receivedMessage = new boolean[]{false};
            var start = Instant.now();

            var handle = new TopicMessageQuery()
              .setTopicId(topic)
              .subscribe(client, (message) -> {
                  assertEquals(new String(message.contents, StandardCharsets.UTF_8), "Hello, from HCS!");
                  receivedMessage[0] = true;
              });

            new TopicMessageSubmitTransaction()
              .setNodeAccountIds(Collections.singletonList(response.nodeId))
              .setTopicId(topic)
              .setMessage("Hello, from HCS!")
              .execute(client);

            while(!receivedMessage[0]) {
                if (Duration.between(start, Instant.now()).compareTo(Duration.ofSeconds(30)) > 0) {
                    throw new Exception("TopicMessage was not received in 30 seconds or less");
                }

                Thread.sleep(1000);
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
