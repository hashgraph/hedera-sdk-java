import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import java.util.Objects;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TopicMessageIntegrationTest {
    @Test
    @DisplayName("Can receive a topic message")
    void canReceiveATopicMessage() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withOneNode();

            var response = new TopicCreateTransaction()
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .setAdminKey(testEnv.operatorKey)
                .setTopicMemo("[e2e::TopicCreateTransaction]")
                .execute(testEnv.client);

            var topicId = Objects.requireNonNull(response.getReceipt(testEnv.client).topicId);

            @Var var info = new TopicInfoQuery()
                .setTopicId(topicId)
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .execute(testEnv.client);

            assertEquals(info.topicId, topicId);
            assertEquals(info.topicMemo, "[e2e::TopicCreateTransaction]");
            assertEquals(info.sequenceNumber, 0);
            assertEquals(info.adminKey, testEnv.operatorKey);

            var receivedMessage = new boolean[]{false};
            var start = Instant.now();

            var handle = new TopicMessageQuery()
                .setTopicId(topicId)
                .setStartTime(Instant.EPOCH)
                .subscribe(testEnv.client, (message) -> {
                    receivedMessage[0] = new String(message.contents, StandardCharsets.UTF_8).equals("Hello, from HCS!");
                });

            new TopicMessageSubmitTransaction()
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .setTopicId(topicId)
                .setMessage("Hello, from HCS!")
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            while (!receivedMessage[0]) {
                if (Duration.between(start, Instant.now()).compareTo(Duration.ofSeconds(60)) > 0) {
                    throw new Exception("TopicMessage was not received in 60 seconds or less");
                }

                Thread.sleep(2000);
            }

            handle.unsubscribe();

            new TopicDeleteTransaction()
                .setTopicId(topicId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.cleanUpAndClose();
        });
    }

    @Test
    @DisplayName("Can receive a large topic message")
    void canReceiveALargeTopicMessage() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withOneNode();

            var response = new TopicCreateTransaction()
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .setAdminKey(testEnv.operatorKey)
                .setTopicMemo("[e2e::TopicCreateTransaction]")
                .execute(testEnv.client);

            var topicId = Objects.requireNonNull(response.getReceipt(testEnv.client).topicId);

            @Var var info = new TopicInfoQuery()
                .setTopicId(topicId)
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .execute(testEnv.client);

            assertEquals(info.topicId, topicId);
            assertEquals(info.topicMemo, "[e2e::TopicCreateTransaction]");
            assertEquals(info.sequenceNumber, 0);
            assertEquals(info.adminKey, testEnv.operatorKey);

            var receivedMessage = new boolean[]{false};
            var start = Instant.now();

            var handle = new TopicMessageQuery()
                .setTopicId(topicId)
                .setStartTime(Instant.EPOCH)
                .subscribe(testEnv.client, (message) -> {
                    receivedMessage[0] = new String(message.contents, StandardCharsets.UTF_8).equals(Contents.BIG_CONTENTS);
                });

            new TopicMessageSubmitTransaction()
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .setTopicId(topicId)
                .setMessage(Contents.BIG_CONTENTS)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            while (!receivedMessage[0]) {
                if (Duration.between(start, Instant.now()).compareTo(Duration.ofSeconds(60)) > 0) {
                    throw new Exception("TopicMessage was not received in 60 seconds or less");
                }

                Thread.sleep(1000);
            }

            handle.unsubscribe();

            new TopicDeleteTransaction()
                .setTopicId(topicId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.cleanUpAndClose();
        });
    }
}
