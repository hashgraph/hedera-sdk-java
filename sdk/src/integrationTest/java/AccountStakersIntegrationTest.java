import com.hedera.hashgraph.sdk.AccountStakersQuery;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class AccountStakersIntegrationTest {
    @Test
    @DisplayName("Cannot query account stakers since it is not supported")
    void cannotQueryAccountStakersSinceItIsNotSupported() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());

            var error = assertThrows(PrecheckStatusException.class, () -> {
                new AccountStakersQuery()
                    .setAccountId(operatorId)
                    .setMaxQueryPayment(new Hbar(1))
                    .execute(client);
            });

            assertTrue(error.getMessage().contains(Status.NOT_SUPPORTED.toString()));

            client.close();
        });
    }
}
