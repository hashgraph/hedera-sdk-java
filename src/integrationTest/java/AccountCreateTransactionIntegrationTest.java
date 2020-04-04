import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TransactionId;
import org.junit.jupiter.api.Test;

class AccountCreateTransactionIntegrationTest {
    @Test
    void createsAccount() {
        var operatorKey = PrivateKey.fromString(System.getenv("OPERATOR_KEY"));
        var operatorId = new AccountId(0, 0, 147722);

        var newKey = PrivateKey.generateEd25519();

        try (var client = Client.forTestnet()) {
            var transaction = new AccountCreateTransaction()
                .setTransactionId(TransactionId.generate(operatorId))
                .setNodeAccountId(new AccountId(0, 0, 3))
                .setInitialBalance(10)
                .setMaxTransactionFee(50_000_000)
                .setKey(newKey)
                .build();

            transaction.sign(operatorKey);

            var transactionResponse = transaction.execute(client);

            System.err.println("response = " + transactionResponse.toString());
        }
    }
}
