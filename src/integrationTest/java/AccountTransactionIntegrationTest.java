import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class AccountTransactionIntegrationTest {
    @Test
    void createThenDeleteAccount() {
        assertThatCode(() -> {
            var operatorKey = PrivateKey.fromString(System.getenv("OPERATOR_KEY"));
            var operatorId = new AccountId(147722);

            var newKey = PrivateKey.generateEd25519();

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

                assertThat(transactionReceipt.accountId).isNotNull();
                assertThat(transactionReceipt.status).isEqualTo(Status.Success);
                assertThat(transactionReceipt.accountId.num).isGreaterThan(0);

                // Fetch the account info for this account

                var accountInfo = new AccountInfoQuery()
                    .setAccountId(transactionReceipt.accountId)
                    .execute(client);

                assertThat(accountInfo.accountId).isEqualTo(transactionReceipt.accountId);
                assertThat(accountInfo.autoRenewPeriod).isEqualTo(Duration.ofDays(90));
                assertThat(accountInfo.balance).isEqualTo(initialBalance);
                assertThat(accountInfo.receiveRecordThreshold).isEqualTo(Long.MAX_VALUE);
                assertThat(accountInfo.sendRecordThreshold).isEqualTo(Long.MAX_VALUE);
                assertThat(accountInfo.key).isEqualTo(newKey.getPublicKey());
                assertThat(accountInfo.proxyAccountId).isNull();
                assertThat(accountInfo.proxyReceived).isEqualTo(Hbar.ZERO);

                // Now fetch it again but as the new account
                // to be doubly sure that the
                // new account is actually usable

                client.setOperator(accountInfo.accountId, newKey);

                var accountInfo2 = new AccountInfoQuery()
                    .setAccountId(transactionReceipt.accountId)
                    .execute(client);

                assertThat(accountInfo2.accountId).isEqualTo(accountInfo.accountId);

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
        }).doesNotThrowAnyException();
    }
}
