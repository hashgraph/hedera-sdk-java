import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.AccountInfoQuery;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TransactionReceiptQuery;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountIntegrationTest {
    @Test
    void getBalanceForGenesis() {
        assertDoesNotThrow(() -> {
            try (var client = Client.forTestnet()) {
                var genesisAccountId = new AccountId(2);
                var balance = new AccountBalanceQuery()
                    .setAccountId(genesisAccountId)
                    .execute(client);

                // The network is in serious trouble if genesis hits zero
                assertTrue(balance.asTinybar() > 0);
            }
        });
    }

    @Test
    void createThenDeleteAccount() {
        assertDoesNotThrow(() -> {
            // TODO: Share this setting somehow
            var operatorKey = PrivateKey.fromString("302e020100300506032b65700422042091dad4f120ca225ce66deb1d6fb7ecad0e53b5e879aa45b0c5e0db7923f26d08");
            var operatorId = new AccountId(147722);

            var newKey = PrivateKey.generate();

            try (var client = Client.forTestnet()) {
                client.setOperator(operatorId, operatorKey);

                var initialBalance = new Hbar(2);
                var maxTransactionFee = new Hbar(1);

                // Create a new Hedera account with a small initial balance

                @Var var transactionId = new AccountCreateTransaction()
                    .setInitialBalance(initialBalance) // 2 Hbar
                    .setMaxTransactionFee(maxTransactionFee) // 1 Hbar
                    .setKey(newKey)
                    .execute(client);

                var transactionReceipt = new TransactionReceiptQuery()
                    .setTransactionId(transactionId)
                    .execute(client);

                assertNotNull(transactionReceipt.accountId);
                assertEquals(transactionReceipt.status, Status.Success);
                assertTrue(transactionReceipt.accountId.num > 0);

                // Fetch the account info for this account

                var accountInfo = new AccountInfoQuery()
                    .setAccountId(transactionReceipt.accountId)
                    .execute(client);

                assertEquals(accountInfo.accountId, transactionReceipt.accountId);
                assertEquals(accountInfo.autoRenewPeriod, Duration.ofDays(90));
                assertEquals(accountInfo.balance, initialBalance);
                assertEquals(accountInfo.receiveRecordThreshold.asTinybar(), Long.MAX_VALUE);
                assertEquals(accountInfo.sendRecordThreshold.asTinybar(), Long.MAX_VALUE);
                assertEquals(accountInfo.key, newKey.getPublicKey());
                assertNull(accountInfo.proxyAccountId);
                assertEquals(accountInfo.proxyReceived, Hbar.ZERO);

                // Now fetch it again but as the new account
                // to be doubly sure that the
                // new account is actually usable

                client.setOperator(accountInfo.accountId, newKey);

                var accountInfo2 = new AccountInfoQuery()
                    .setAccountId(transactionReceipt.accountId)
                    .execute(client);

                assertEquals(accountInfo2.accountId, accountInfo.accountId);

                // Now delete the account (and give everything back to the original operator)

                transactionId = new AccountDeleteTransaction()
                    .setMaxTransactionFee(maxTransactionFee) // 1 Hbar
                    .setDeleteAccountId(accountInfo.accountId)
                    .setTransferAccountId(operatorId)
                    .execute(client);

                new TransactionReceiptQuery()
                    .setTransactionId(transactionId)
                    .execute(client);
            }
        });
    }
}
