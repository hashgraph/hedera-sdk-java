import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Instant;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.assertj.core.api.Assertions.*;

public class MirrorNodeCloseClientIntegrationTest {
    @Test
    @DisplayName("Can close the Client while subscribed to mirror node topic queries")
    void canCloseClient() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var response = new TopicCreateTransaction()
            .setAdminKey(testEnv.operatorKey)
            .setTopicMemo("[e2e::TopicCreateTransaction]")
            .execute(testEnv.client);

        var topicId = Objects.requireNonNull(response.getReceipt(testEnv.client).topicId);

        new TopicInfoQuery()
            .setTopicId(topicId)
            .execute(testEnv.client);

        Thread.sleep(3000);

        var handle1 = new TopicMessageQuery()
            .setTopicId(topicId)
            .setStartTime(Instant.EPOCH)
            .subscribe(testEnv.client, (message) -> {
                System.out.println(new String(message.contents, StandardCharsets.UTF_8));
            });

        var handle2 = new TopicMessageQuery()
            .setTopicId(topicId)
            .setStartTime(Instant.EPOCH)
            .subscribe(testEnv.client, (message) -> {
                System.out.println(new String(message.contents, StandardCharsets.UTF_8));
            });

        assertThatNoException().isThrownBy(() -> {
            // Close the Client while still subscribed to the Topic.
            // The Client should automatically unsubscribe from all topics by itself,
            // no need to manually unsubscribe handle1 and handle2.
            testEnv.client.close();
        });
    }
}
