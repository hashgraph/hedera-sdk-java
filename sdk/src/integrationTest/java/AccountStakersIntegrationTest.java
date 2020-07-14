import com.hedera.hashgraph.sdk.AccountStakersQuery;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.HederaPreCheckStatusException;
import com.hedera.hashgraph.sdk.Status;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountStakersIntegrationTest {
    @Test
    void test() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = client.getOperatorId();

            try {
                new AccountStakersQuery()
                    .setAccountId(operatorId)
                    .setMaxQueryPayment(new Hbar(1))
                    .execute(client);
            } catch (HederaPreCheckStatusException e) {
                assertEquals(e.status, Status.NOT_SUPPORTED);
            }

            client.close();
        });
    }
}
