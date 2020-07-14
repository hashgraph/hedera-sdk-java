import com.hedera.hashgraph.sdk.HederaPreCheckStatusException;
import com.hedera.hashgraph.sdk.NetworkVersionInfoQuery;
import com.hedera.hashgraph.sdk.Status;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NetworkVersionInfoIntegrationTest {
    @Test
    void test() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();

            try {
                new NetworkVersionInfoQuery().execute(client);
            } catch (HederaPreCheckStatusException e) {
                assertEquals(e.status, Status.NOT_SUPPORTED);
            }

//            NetworkVersionInfo version = new NetworkVersionInfoQuery()
//                .execute(client);
//
//            assertEquals(version.hapiProtoVersion.major, 0);
//            assertEquals(version.hapiProtoVersion.minor, 0);
//            assertEquals(version.hapiProtoVersion.patch, 0);
//
//            assertEquals(version.hederaServicesVersion.major, 0);
//            assertEquals(version.hederaServicesVersion.minor, 0);
//            assertEquals(version.hederaServicesVersion.patch, 0);
        });
    }
}
