import com.hedera.hashgraph.sdk.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class AccountBalanceQueryIntegrationTest {
    @Test
    void getAccountBalanceForGenesis() {
        assertThatCode(() -> {
            try (var client = Client.forTestnet()) {
                var genesisAccountId = new AccountId(2);
                var balance =
                    new AccountBalanceQuery()
                        .setAccountId(genesisAccountId)
                        .execute(client);

                // The network is in serious trouble if genesis hits zero
                assertThat(balance).isGreaterThan(0);
            }
        }).doesNotThrowAnyException();
    }
}
