import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.TransactionReceiptQuery;
import org.junit.jupiter.api.Test;

class AccountCreateTransactionIntegrationTest {
    @Test
    void createsAccount() {
        assertThatCode(
                        () -> {
                            var operatorKey = PrivateKey.fromString(System.getenv("OPERATOR_KEY"));
                            var operatorId = new AccountId(0, 0, 147722);

                            var newKey = PrivateKey.generateEd25519();

                            try (var client = Client.forTestnet()) {
                                var transactionId =
                                        new AccountCreateTransaction()
                                                .setTransactionId(
                                                        TransactionId.generate(operatorId))
                                                .setNodeAccountId(new AccountId(0, 0, 3))
                                                .setInitialBalance(10)
                                                .setMaxTransactionFee(50_000_000)
                                                .setKey(newKey)
                                                .execute(client);

                                var transactionReceipt =
                                        new TransactionReceiptQuery()
                                                .setTransactionId(transactionId)
                                                .execute(client);

                                assertThat(transactionReceipt.accountId.isPresent()).isTrue();
                            }
                        })
                .doesNotThrowAnyException();
    }
}
