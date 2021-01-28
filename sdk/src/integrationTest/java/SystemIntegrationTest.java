import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class SystemIntegrationTest {
    @Test
    @DisplayName("All system transactions are not supported")
    void allSystemTransactionsAreNotSupported() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();

            @Var var error = assertThrows(PrecheckStatusException.class, () -> {
                new SystemDeleteTransaction()
                    .setContractId(new ContractId(10))
                    .setExpirationTime(Instant.now())
                    .execute(client);
            });

            assertTrue(error.getMessage().contains(Status.NOT_SUPPORTED.toString()));

            error = assertThrows(PrecheckStatusException.class, () -> {
                new SystemDeleteTransaction()
                    .setFileId(new FileId(10))
                    .setExpirationTime(Instant.now())
                    .execute(client);
            });

            assertTrue(error.getMessage().contains(Status.NOT_SUPPORTED.toString()));

            error = assertThrows(PrecheckStatusException.class, () -> {
                new SystemUndeleteTransaction()
                    .setContractId(new ContractId(10))
                    .execute(client);
            });

            assertTrue(error.getMessage().contains(Status.NOT_SUPPORTED.toString()));

            error = assertThrows(PrecheckStatusException.class, () -> {
                new SystemUndeleteTransaction()
                    .setFileId(new FileId(10))
                    .execute(client);
            });

            assertTrue(error.getMessage().contains(Status.NOT_SUPPORTED.toString()));

            client.close();
        });
    }
}
