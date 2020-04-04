import static org.assertj.core.api.Assertions.assertThat;

import com.hedera.hashgraph.sdk.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import org.junit.jupiter.api.Test;

class AccountBalanceQueryIntegrationTest {
    @Test
    void getAccountBalanceForGenesis() {
        try (var client = Client.forTestnet()) {
            var genesisAccountId = new AccountId(2);
            var balance = new AccountBalanceQuery().setAccountId(genesisAccountId).execute(client);

            // The network is in serious trouble if genesis hits zero
            assertThat(balance).isGreaterThan(0);
        }
    }
}
