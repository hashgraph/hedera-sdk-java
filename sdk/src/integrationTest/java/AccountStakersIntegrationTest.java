import com.hedera.hashgraph.sdk.AccountStakersQuery;
import com.hedera.hashgraph.sdk.Hbar;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountStakersIntegrationTest {
    @Test
    void test() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();

            assertThrows(Exception.class, () -> {
                new AccountStakersQuery()
                    .setAccountId(client.getOperatorId())
                    .setMaxQueryPayment(new Hbar(1))
                    .execute(client);
            });
        });
    }
}
