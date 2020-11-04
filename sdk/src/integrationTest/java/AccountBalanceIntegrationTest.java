import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TransactionId;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountBalanceIntegrationTest {
    @Test
    void test() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = client.getOperatorAccountId();

            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setMaxTransactionFee(new Hbar(2))
                .setInitialBalance(new Hbar(1))
                .execute(client);

            var receipt = response.transactionId.getReceipt(client);

            assertNotNull(receipt.accountId);
            assertTrue(Objects.requireNonNull(receipt.accountId).num > 0);

            var account = receipt.accountId;

            @Var var balance = new AccountBalanceQuery()
                .setAccountId(account)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client);

            assertEquals(balance.hbars, new Hbar(1));

            new AccountDeleteTransaction()
                .setAccountId(account)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setTransferAccountId(operatorId)
                .setTransactionId(TransactionId.generate(account))
                .freezeWith(client)
                .sign(key)
                .execute(client)
                .transactionId
                .getReceipt(client);

            client.close();
        });
    }
}
