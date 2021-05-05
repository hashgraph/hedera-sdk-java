import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class TokenAssociateIntegrationTest {
    @Test
    @DisplayName("Can associate account with token")
    void canAssociateAccountWithToken() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(testEnv.client);

            var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

            var tokenId = Objects.requireNonNull(
                new TokenCreateTransaction()
                    .setNodeAccountIds(testEnv.nodeAccountIds)
                    .setTokenName("ffff")
                    .setTokenSymbol("F")
                    .setDecimals(3)
                    .setInitialSupply(1000000)
                    .setTreasuryAccountId(testEnv.operatorId)
                    .setAdminKey(testEnv.operatorKey)
                    .setFreezeKey(testEnv.operatorKey)
                    .setWipeKey(testEnv.operatorKey)
                    .setKycKey(testEnv.operatorKey)
                    .setSupplyKey(testEnv.operatorKey)
                    .setFreezeDefault(false)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client)
                    .tokenId
            );

            new TokenAssociateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setAccountId(accountId)
                .setTokenIds(Collections.singletonList(tokenId))
                .freezeWith(testEnv.client)
                .sign(key)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Can execute token associate transaction even when token IDs are not set")
    void canExecuteTokenAssociateTransactionEvenWhenTokenIDsAreNotSet() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(testEnv.client);

            var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

            new TokenAssociateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setAccountId(accountId)
                .freezeWith(testEnv.client)
                .sign(key)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Cannot associate account with tokens when account ID is not set")
    void cannotAssociateAccountWithTokensWhenAccountIDIsNotSet() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(testEnv.client);

            var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

            var error = assertThrows(PrecheckStatusException.class, () -> {
                new TokenAssociateTransaction()
                    .setNodeAccountIds(testEnv.nodeAccountIds)
                    .freezeWith(testEnv.client)
                    .sign(key)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.INVALID_ACCOUNT_ID.toString()));

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Cannot associate account with tokens when account does not sign transaction")
    void cannotAssociateAccountWhenAccountDoesNotSignTransaction() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(testEnv.client);

            var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

            var tokenId = Objects.requireNonNull(
                new TokenCreateTransaction()
                    .setNodeAccountIds(testEnv.nodeAccountIds)
                    .setTokenName("ffff")
                    .setTokenSymbol("F")
                    .setDecimals(3)
                    .setInitialSupply(1000000)
                    .setTreasuryAccountId(testEnv.operatorId)
                    .setAdminKey(testEnv.operatorKey)
                    .setFreezeKey(testEnv.operatorKey)
                    .setWipeKey(testEnv.operatorKey)
                    .setKycKey(testEnv.operatorKey)
                    .setSupplyKey(testEnv.operatorKey)
                    .setFreezeDefault(false)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client)
                    .tokenId
            );

            var error = assertThrows(ReceiptStatusException.class, () -> {
                new TokenAssociateTransaction()
                    .setNodeAccountIds(testEnv.nodeAccountIds)
                    .setAccountId(accountId)
                    .setTokenIds(Collections.singletonList(tokenId))
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.INVALID_SIGNATURE.toString()));

            testEnv.client.close();
        });
    }
}
