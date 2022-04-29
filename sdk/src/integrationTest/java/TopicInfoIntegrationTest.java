import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.MaxQueryPaymentExceededException;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.TopicCreateTransaction;
import com.hedera.hashgraph.sdk.TopicDeleteTransaction;
import com.hedera.hashgraph.sdk.TopicInfoQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class TopicInfoIntegrationTest {

    @Test
    @DisplayName("Can query topic info")
    void canQueryTopicInfo() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var response = new TopicCreateTransaction()
            .setAdminKey(testEnv.operatorKey)
            .setTopicMemo("[e2e::TopicCreateTransaction]")
            .execute(testEnv.client);

        var topicId = Objects.requireNonNull(response.getReceipt(testEnv.client).topicId);

        var info = new TopicInfoQuery()
            .setTopicId(topicId)
            .execute(testEnv.client);

        assertThat(info.topicMemo).isEqualTo("[e2e::TopicCreateTransaction]");

        new TopicDeleteTransaction()
            .setTopicId(topicId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        testEnv.close();
    }

    @Test
    @DisplayName("Can get cost for topic info query")
    void getCostQueryTopicInfo() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var response = new TopicCreateTransaction()
            .setAdminKey(testEnv.operatorKey)
            .setTopicMemo("[e2e::TopicCreateTransaction]")
            .execute(testEnv.client);

        var topicId = Objects.requireNonNull(response.getReceipt(testEnv.client).topicId);

        var infoQuery = new TopicInfoQuery()
            .setTopicId(topicId);

        var cost = infoQuery.getCost(testEnv.client);

        assertThat(cost).isNotNull();

        var info = infoQuery.execute(testEnv.client);

        assertThat(info.topicMemo).isEqualTo("[e2e::TopicCreateTransaction]");

        new TopicDeleteTransaction()
            .setTopicId(topicId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        testEnv.close();
    }

    @Test
    @DisplayName("Can get cost for topic info query")
    void getCostBigMaxQueryTopicInfo() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var response = new TopicCreateTransaction()
            .setAdminKey(testEnv.operatorKey)
            .setTopicMemo("[e2e::TopicCreateTransaction]")
            .execute(testEnv.client);

        var topicId = Objects.requireNonNull(response.getReceipt(testEnv.client).topicId);

        var infoQuery = new TopicInfoQuery()
            .setTopicId(topicId)
            .setMaxQueryPayment(new Hbar(1000));

        var cost = infoQuery.getCost(testEnv.client);

        assertThat(cost).isNotNull();

        var info = infoQuery.execute(testEnv.client);

        assertThat(info.topicMemo).isEqualTo("[e2e::TopicCreateTransaction]");

        new TopicDeleteTransaction()
            .setTopicId(topicId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        testEnv.close();
    }

    @Test
    @DisplayName("Can get cost for topic info query")
    void getCostSmallMaxQueryTopicInfo() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var response = new TopicCreateTransaction()
            .setAdminKey(testEnv.operatorKey)
            .setTopicMemo("[e2e::TopicCreateTransaction]")
            .execute(testEnv.client);

        var topicId = Objects.requireNonNull(response.getReceipt(testEnv.client).topicId);

        var infoQuery = new TopicInfoQuery()
            .setTopicId(topicId)
            .setMaxQueryPayment(Hbar.fromTinybars(1));

        assertThatExceptionOfType(MaxQueryPaymentExceededException.class).isThrownBy(() -> {
            infoQuery.execute(testEnv.client);
        });

        new TopicDeleteTransaction()
            .setTopicId(topicId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        testEnv.close();
    }

    @Test
    @DisplayName("Can get cost for topic info query")
    void getCostInsufficientTxFeeQueryTopicInfo() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var response = new TopicCreateTransaction()
            .setAdminKey(testEnv.operatorKey)
            .setTopicMemo("[e2e::TopicCreateTransaction]")
            .execute(testEnv.client);

        var topicId = Objects.requireNonNull(response.getReceipt(testEnv.client).topicId);

        var infoQuery = new TopicInfoQuery()
            .setTopicId(topicId);

        var cost = infoQuery.getCost(testEnv.client);

        assertThat(cost).isNotNull();

        assertThatExceptionOfType(PrecheckStatusException.class).isThrownBy(() -> {
            infoQuery.setQueryPayment(Hbar.fromTinybars(1)).execute(testEnv.client);
        }).satisfies(error -> assertThat(error.status.toString()).isEqualTo("INSUFFICIENT_TX_FEE"));

        new TopicDeleteTransaction()
            .setTopicId(topicId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        testEnv.close();
    }


}
