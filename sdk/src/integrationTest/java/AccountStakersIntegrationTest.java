import com.hedera.hashgraph.sdk.AccountStakersQuery;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountStakersIntegrationTest {
    @Test
    @DisplayName("Cannot query account stakers since it is not supported")
    void cannotQueryAccountStakersSinceItIsNotSupported() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var error = assertThrows(PrecheckStatusException.class, () -> {
                new AccountStakersQuery()
                    .setNodeAccountIds(testEnv.nodeAccountIds)
                    .setAccountId(testEnv.operatorId)
                    .setMaxQueryPayment(new Hbar(1))
                    .execute(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.NOT_SUPPORTED.toString()));

            testEnv.client.close();
        });
    }
}
