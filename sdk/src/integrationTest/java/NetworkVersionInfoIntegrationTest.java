import com.hedera.hashgraph.sdk.NetworkVersionInfoQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class NetworkVersionInfoIntegrationTest {
    @Test
    @DisplayName("Cannot query network version info")
    void cannotQueryNetworkVersionInfo() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        new NetworkVersionInfoQuery()
            .execute(testEnv.client);

        testEnv.close();
    }
}
