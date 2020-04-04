import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.Transaction;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import org.junit.jupiter.api.Test;

class AccountCreateTransactionIntegrationTest {
    @Test
    void createsAccount() {
        PrivateKey newKey = PrivateKey.generateEd25519();

        PrivateKey operatorKey = PrivateKey.fromString(System.getenv("OPERATOR_KEY"));
        AccountId operatorId = new AccountId(0, 0, 147722);

        Transaction transaction = new AccountCreateTransaction()
            .setTransactionId(TransactionId.generate(operatorId))
            .setNodeAccountId(new AccountId(0, 0, 3))
            .setInitialBalance(10)
            .setMaxTransactionFee(30_000_000)
            .setKey(newKey)
            .build()
            .sign(operatorKey);

        TransactionResponse transactionResponse = transaction.execute();

        System.err.println("response = " + transactionResponse.toString());
    }
}
