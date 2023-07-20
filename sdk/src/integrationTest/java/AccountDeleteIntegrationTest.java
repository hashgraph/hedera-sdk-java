import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.AccountInfoQuery;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class AccountDeleteIntegrationTest {
    @Test
    @DisplayName("Can delete account")
    void canDeleteAccount() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var key = PrivateKey.generateED25519();

        var response = new AccountCreateTransaction()
            .setKey(key)
            .setInitialBalance(new Hbar(1))
            .execute(testEnv.client);

        var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

        var info = new AccountInfoQuery()
            .setAccountId(accountId)
            .execute(testEnv.client);

        assertThat(info.accountId).isEqualTo(accountId);
        assertThat(info.isDeleted).isFalse();
        assertThat(info.key.toString()).isEqualTo(key.getPublicKey().toString());
        assertThat(info.balance).isEqualTo(new Hbar(1));
        assertThat(info.autoRenewPeriod).isEqualTo(Duration.ofDays(90));
        assertThat(info.proxyAccountId).isNull();
        assertThat(info.proxyReceived).isEqualTo(Hbar.ZERO);

        testEnv.close(accountId, key);
    }

    @Test
    @DisplayName("Cannot delete invalid account ID")
    void cannotCreateAccountWithNoKey() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        assertThatExceptionOfType(PrecheckStatusException.class).isThrownBy(() -> {
            new AccountDeleteTransaction()
                .setTransferAccountId(testEnv.operatorId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        }).withMessageContaining(Status.ACCOUNT_ID_DOES_NOT_EXIST.toString());

        testEnv.close();
    }

    @Test
    @DisplayName("Cannot delete account that has not signed transaction")
    void cannotDeleteAccountThatHasNotSignedTransaction() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var key = PrivateKey.generateED25519();

        var response = new AccountCreateTransaction()
            .setKey(key)
            .setInitialBalance(new Hbar(1))
            .execute(testEnv.client);

        var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

        assertThatExceptionOfType(ReceiptStatusException.class).isThrownBy(() -> {
            new AccountDeleteTransaction()
                .setAccountId(accountId)
                .setTransferAccountId(testEnv.operatorId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        }).withMessageContaining(Status.INVALID_SIGNATURE.toString());

        testEnv.close(accountId, key);
    }
}
