import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.NetworkVersionInfoQuery;
import com.hedera.hashgraph.sdk.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NetworkVersionInfoIntegrationTest {
    @Test
    @DisplayName("Cannot query network version info")
    void cannotQueryNetworkVersionInfo() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();

            var error = assertThrows(PrecheckStatusException.class, () -> {
                new NetworkVersionInfoQuery().execute(client);
            });

            assertTrue(error.getMessage().contains(Status.NOT_SUPPORTED.toString()));

            client.close();
        });
    }
}
