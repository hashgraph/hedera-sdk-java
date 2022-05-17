import com.hedera.hashgraph.sdk.AccountStakersQuery;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class AccountStakersIntegrationTest {
    @Test
    @DisplayName("Cannot query account stakers since it is not supported")
    void cannotQueryAccountStakersSinceItIsNotSupported() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        assertThatExceptionOfType(PrecheckStatusException.class).isThrownBy(() -> {
            new AccountStakersQuery()
                .setAccountId(testEnv.operatorId)
                .setMaxQueryPayment(new Hbar(1))
                .execute(testEnv.client);
        }).withMessageContaining(Status.NOT_SUPPORTED.toString());

        testEnv.close();
    }
}
