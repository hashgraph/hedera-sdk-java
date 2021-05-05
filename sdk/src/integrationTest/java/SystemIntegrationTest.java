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
            var testEnv = new IntegrationTestEnv();

            @Var var error = assertThrows(PrecheckStatusException.class, () -> {
                new SystemDeleteTransaction()
                    .setNodeAccountIds(testEnv.nodeAccountIds)
                    .setContractId(new ContractId(10))
                    .setExpirationTime(Instant.now())
                    .execute(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.NOT_SUPPORTED.toString()));

            error = assertThrows(PrecheckStatusException.class, () -> {
                new SystemDeleteTransaction()
                    .setNodeAccountIds(testEnv.nodeAccountIds)
                    .setFileId(new FileId(10))
                    .setExpirationTime(Instant.now())
                    .execute(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.NOT_SUPPORTED.toString()));

            error = assertThrows(PrecheckStatusException.class, () -> {
                new SystemUndeleteTransaction()
                    .setNodeAccountIds(testEnv.nodeAccountIds)
                    .setContractId(new ContractId(10))
                    .execute(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.NOT_SUPPORTED.toString()));

            error = assertThrows(PrecheckStatusException.class, () -> {
                new SystemUndeleteTransaction()
                    .setNodeAccountIds(testEnv.nodeAccountIds)
                    .setFileId(new FileId(10))
                    .execute(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.NOT_SUPPORTED.toString()));

            testEnv.client.close();
        });
    }
}
