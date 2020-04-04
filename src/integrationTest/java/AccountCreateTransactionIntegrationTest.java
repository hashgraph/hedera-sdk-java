import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.Transaction;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import org.junit.jupiter.api.Test;

class AccountCreateTransactionIntegrationTest {
    @Test
    void createsAccount() {
        PrivateKey newKey = PrivateKey.generateEd25519();

        Transaction transaction = new AccountCreateTransaction()
            .setInitialBalance(10)
            .setKey(newKey)
            .build();

        TransactionResponse transactionResponse = transaction.execute();

        System.err.println("response = " + transactionResponse.toString());
    }
}
