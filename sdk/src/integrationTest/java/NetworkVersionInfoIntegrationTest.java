import com.hedera.hashgraph.sdk.NetworkVersionInfoQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class NetworkVersionInfoIntegrationTest {
    @Test
    @DisplayName("Cannot query network version info")
    void cannotQueryNetworkVersionInfo() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withOneNode();

            new NetworkVersionInfoQuery()
                .execute(testEnv.client);

            testEnv.cleanUpAndClose();
        });
    }
}
