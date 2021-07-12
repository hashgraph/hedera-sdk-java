import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TokenDissociateIntegrationTest {
    @Test
    @DisplayName("Can dissociate account with token")
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

            new TokenDissociateTransaction()
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
    @DisplayName("Can execute token dissociate transaction even when token IDs are not set")
    void canExecuteTokenDissociateTransactionEvenWhenTokenIDsAreNotSet() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(testEnv.client);

            var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

            new TokenDissociateTransaction()
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
    @DisplayName("Cannot dissociate account with tokens when account ID is not set")
    void cannotDissociateAccountWithTokensWhenAccountIDIsNotSet() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(testEnv.client);

            Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

            var error = assertThrows(PrecheckStatusException.class, () -> {
                new TokenDissociateTransaction()
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
    @DisplayName("Cannot dissociate account with tokens when account does not sign transaction")
    void cannotDissociateAccountWhenAccountDoesNotSignTransaction() {
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
                new TokenDissociateTransaction()
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

    @Test
    @DisplayName("Cannot dissociate account from token when account was not associated with")
    void cannotDissociateAccountFromTokenWhenAccountWasNotAssociatedWith() {
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
                new TokenDissociateTransaction()
                    .setNodeAccountIds(testEnv.nodeAccountIds)
                    .setAccountId(accountId)
                    .setTokenIds(Collections.singletonList(tokenId))
                    .freezeWith(testEnv.client)
                    .sign(key)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.TOKEN_NOT_ASSOCIATED_TO_ACCOUNT.toString()));

            testEnv.client.close();
        });
    }
}
