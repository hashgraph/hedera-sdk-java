import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import java.util.Collections;
import java.util.Objects;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class TopicMessageIntegrationTest {
    @Test
    @DisplayName("Can receive a topic message")
    void canReceiveATopicMessage() {
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
                .execute(client);

            assertEquals(info.topicId, topicId);
            assertEquals(info.topicMemo, "[e2e::TopicCreateTransaction]");
            assertEquals(info.sequenceNumber, 0);
            assertEquals(info.adminKey, operatorKey);

            var receivedMessage = new boolean[]{false};
            var start = Instant.now();

            var handle = new TopicMessageQuery()
                .setTopicId(topicId)
                .setStartTime(Instant.EPOCH)
                .subscribe(client, (message) -> {
                    receivedMessage[0] = new String(message.contents, StandardCharsets.UTF_8).equals("Hello, from HCS!");
                });

            new TopicMessageSubmitTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setTopicId(topicId)
                .setMessage("Hello, from HCS!")
                .execute(client)
                .getReceipt(client);

            while (!receivedMessage[0]) {
                if (Duration.between(start, Instant.now()).compareTo(Duration.ofSeconds(60)) > 0) {
                    throw new Exception("TopicMessage was not received in 60 seconds or less");
                }

                Thread.sleep(2000);
            }

            handle.unsubscribe();

            new TopicDeleteTransaction()
                .setTopicId(topicId)
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }

    @Test
    @DisplayName("Can receive a large topic message")
    void canReceiveALargeTopicMessage() {
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
                .execute(client);

            assertEquals(info.topicId, topicId);
            assertEquals(info.topicMemo, "[e2e::TopicCreateTransaction]");
            assertEquals(info.sequenceNumber, 0);
            assertEquals(info.adminKey, operatorKey);

            var receivedMessage = new boolean[]{false};
            var start = Instant.now();

            var handle = new TopicMessageQuery()
                .setTopicId(topicId)
                .setStartTime(Instant.EPOCH)
                .subscribe(client, (message) -> {
                    receivedMessage[0] = new String(message.contents, StandardCharsets.UTF_8).equals(Contents.BIG_CONTENTS);
                });

            new TopicMessageSubmitTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setTopicId(topicId)
                .setMessage(Contents.BIG_CONTENTS)
                .execute(client)
                .getReceipt(client);

            while (!receivedMessage[0]) {
                if (Duration.between(start, Instant.now()).compareTo(Duration.ofSeconds(60)) > 0) {
                    throw new Exception("TopicMessage was not received in 60 seconds or less");
                }

                Thread.sleep(1000);
            }

            handle.unsubscribe();

            new TopicDeleteTransaction()
                .setTopicId(topicId)
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }
}
