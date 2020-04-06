import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TransactionReceiptQuery;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class AccountTransactionIntegrationTest {
    @Test
    void createsAccount() {
        assertThatCode(() -> {
            var operatorKey = PrivateKey.fromString(System.getenv("OPERATOR_KEY"));
            var operatorId = new AccountId(147722);

            var newKey = PrivateKey.generateEd25519();

            try (var client = Client.forTestnet()) {
                client.setOperator(operatorId, operatorKey);

                // Create a new Hedera account with a small initial balance

                var transactionId = new AccountCreateTransaction()
                    .setInitialBalance(10)
                    .setMaxTransactionFee(50_000_000)
                    .setKey(newKey)
                    .execute(client);

                var transactionReceipt = new TransactionReceiptQuery()
                    .setTransactionId(transactionId)
                    .execute(client);

                assertThat(transactionReceipt.accountId.isPresent()).isTrue();
                assertThat(transactionReceipt.accountId.get().num).isGreaterThan(0);

                // TODO: Fetch the account info of this account

                // TODO: Delete this account
            }
        }).doesNotThrowAnyException();
    }
}
