import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TokenWipeIntegrationTest {
    @Test
    @DisplayName("Can wipe accounts balance")
    void canWipeAccountsBalance() {
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

            new TokenGrantKycTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setAccountId(accountId)
                .setTokenId(tokenId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TransferTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .addTokenTransfer(tokenId, testEnv.operatorId, -10)
                .addTokenTransfer(tokenId, accountId, 10)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TokenWipeTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTokenId(tokenId)
                .setAccountId(accountId)
                .setAmount(10)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Cannot wipe accounts balance when account ID is not set")
    void cannotWipeAccountsBalanceWhenAccountIDIsNotSet() {
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

            new TokenGrantKycTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setAccountId(accountId)
                .setTokenId(tokenId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TransferTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .addTokenTransfer(tokenId, testEnv.operatorId, -10)
                .addTokenTransfer(tokenId, accountId, 10)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var error = assertThrows(PrecheckStatusException.class, () -> {
                new TokenWipeTransaction()
                    .setNodeAccountIds(testEnv.nodeAccountIds)
                    .setTokenId(tokenId)
                    .setAmount(10)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.INVALID_ACCOUNT_ID.toString()));

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Cannot wipe accounts balance when token ID is not set")
    void cannotWipeAccountsBalanceWhenTokenIDIsNotSet() {
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

            new TokenGrantKycTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setAccountId(accountId)
                .setTokenId(tokenId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TransferTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .addTokenTransfer(tokenId, testEnv.operatorId, -10)
                .addTokenTransfer(tokenId, accountId, 10)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var error = assertThrows(PrecheckStatusException.class, () -> {
                new TokenWipeTransaction()
                    .setNodeAccountIds(testEnv.nodeAccountIds)
                    .setAccountId(accountId)
                    .setAmount(10)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.INVALID_TOKEN_ID.toString()));

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Cannot wipe accounts balance when amount is not set")
    void cannotWipeAccountsBalanceWhenAmountIsNotSet() {
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

            new TokenGrantKycTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setAccountId(accountId)
                .setTokenId(tokenId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TransferTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .addTokenTransfer(tokenId, testEnv.operatorId, -10)
                .addTokenTransfer(tokenId, accountId, 10)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var error = assertThrows(PrecheckStatusException.class, () -> {
                new TokenWipeTransaction()
                    .setNodeAccountIds(testEnv.nodeAccountIds)
                    .setTokenId(tokenId)
                    .setAccountId(accountId)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.INVALID_WIPING_AMOUNT.toString()));

            testEnv.client.close();
        });
    }
}
