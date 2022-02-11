import com.hedera.hashgraph.sdk.AccountAllowanceApproveTransaction;
import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrivateKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AccountAllowanceIntegrationTest {
    @Test
    @DisplayName("Can spend hbar allowance")
    void canSpendHbarAllowance() throws Throwable {
        var testEnv = new IntegrationTestEnv(1);

        var aliceKey = PrivateKey.generateED25519();
        var aliceId = new AccountCreateTransaction()
            .setKey(aliceKey)
            .setInitialBalance(new Hbar(10))
            .execute(testEnv.client)
            .getReceipt(testEnv.client)
            .accountId;

        var bobKey = PrivateKey.generateED25519();
        var bobId = new AccountCreateTransaction()
            .setKey(bobKey)
            .setInitialBalance(new Hbar(10))
            .execute(testEnv.client)
            .getReceipt(testEnv.client)
            .accountId;

        new AccountAllowanceApproveTransaction()
            .addHbarAllowance(aliceId, new Hbar(10));
        
    }
}
