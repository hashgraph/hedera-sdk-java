import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.AccountInfoQuery;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TransactionReceiptQuery;
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

                // Create a new Hedera account with a small initial balance

                @Var var transactionId = new AccountCreateTransaction()
                    .setInitialBalance(200_000_000) // 2 Hbar
                    .setMaxTransactionFee(100_000_000) // 1 Hbar
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
                assertThat(accountInfo.balance).isEqualTo(200000000);
                assertThat(accountInfo.receiveRecordThreshold).isEqualTo(Long.MAX_VALUE);
                assertThat(accountInfo.sendRecordThreshold).isEqualTo(Long.MAX_VALUE);
                assertThat(accountInfo.key).isEqualTo(newKey.getPublicKey());
                assertThat(accountInfo.proxyAccountId).isNull();
                assertThat(accountInfo.proxyReceived).isZero();

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
                    .setMaxTransactionFee(100_000_000) // 1 Hbar
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
