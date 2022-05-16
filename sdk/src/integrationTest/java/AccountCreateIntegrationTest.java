import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountInfoQuery;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TransferTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class AccountCreateIntegrationTest {
    @Test
    @DisplayName("Can create account with only initial balance and key")
    void canCreateAccountWithOnlyInitialBalanceAndKey() throws Exception {
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
    @DisplayName("Can create account with no initial balance")
    void canCreateAccountWithNoInitialBalance() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var key = PrivateKey.generateED25519();

        var response = new AccountCreateTransaction()
            .setKey(key)
            .execute(testEnv.client);

        var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

        var info = new AccountInfoQuery()
            .setAccountId(accountId)
            .execute(testEnv.client);

        assertThat(info.accountId).isEqualTo(accountId);
        assertThat(info.isDeleted).isFalse();
        assertThat(info.key.toString()).isEqualTo(key.getPublicKey().toString());
        assertThat(info.balance).isEqualTo(new Hbar(0));
        assertThat(info.autoRenewPeriod).isEqualTo(Duration.ofDays(90));
        assertThat(info.proxyAccountId).isNull();
        assertThat(info.proxyReceived).isEqualTo(Hbar.ZERO);

        testEnv.close(accountId, key);
    }

    @Test
    @DisplayName("Cannot create account with no key")
    void canNotCreateAccountWithNoKey() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        assertThatExceptionOfType(PrecheckStatusException.class).isThrownBy(() -> {
            new AccountCreateTransaction()
                .setInitialBalance(new Hbar(1))
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        }).withMessageContaining(Status.KEY_REQUIRED.toString());

        testEnv.close();
    }

    @Test
    @DisplayName("Can create account using aliasKey")
    void canCreateWithAliasKey() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var key = PrivateKey.generateED25519();

        var aliasId = key.toAccountId(0, 0);

        new TransferTransaction()
            .addHbarTransfer(testEnv.operatorId, new Hbar(10).negated())
            .addHbarTransfer(aliasId, new Hbar(10))
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        var info = new AccountInfoQuery()
            .setAccountId(aliasId)
            .execute(testEnv.client);

        assertThat(key.getPublicKey()).isEqualTo(info.aliasKey);

        testEnv.close(info.accountId, key);
    }

    @Test
    @DisplayName("Regenerates TransactionIds in response to expiration")
    void managesExpiration() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var key = PrivateKey.generate();

        var accountCreateTx = new AccountCreateTransaction()
            .setKey(key)
            .setTransactionValidDuration(Duration.ofSeconds(25))
            .freezeWith(testEnv.client);

        Thread.sleep(30000);

        var response = accountCreateTx.execute(testEnv.client);

        var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

        var info = new AccountInfoQuery()
            .setAccountId(accountId)
            .execute(testEnv.client);

        assertThat(info.accountId).isEqualTo(accountId);
        assertThat(info.isDeleted).isFalse();
        assertThat(info.key.toString()).isEqualTo(key.getPublicKey().toString());
        assertThat(info.balance).isEqualTo(new Hbar(0));
        assertThat(info.autoRenewPeriod).isEqualTo(Duration.ofDays(90));
        assertThat(info.proxyAccountId).isNull();
        assertThat(info.proxyReceived).isEqualTo(Hbar.ZERO);

        testEnv.close(accountId, key);
    }
}
