import com.hedera.hashgraph.sdk.NetworkVersionInfoQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class NetworkVersionInfoIntegrationTest {
    @Test
    @DisplayName("Cannot query network version info")
    void cannotQueryNetworkVersionInfo() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            new NetworkVersionInfoQuery()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .execute(testEnv.client);

            testEnv.client.close();
        });
    }
}
