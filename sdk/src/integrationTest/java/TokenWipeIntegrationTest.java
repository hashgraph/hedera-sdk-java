import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class TokenWipeIntegrationTest {
    @Test
    @DisplayName("Can wipe accounts balance")
    void canWipeAccountsBalance() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(client);

            var accountId = Objects.requireNonNull(response.getReceipt(client).accountId);

            var tokenId = Objects.requireNonNull(
                new TokenCreateTransaction()
                    .setTokenName("ffff")
                    .setTokenSymbol("F")
                    .setDecimals(3)
                    .setInitialSupply(1000000)
                    .setTreasuryAccountId(operatorId)
                    .setAdminKey(operatorKey)
                    .setFreezeKey(operatorKey)
                    .setWipeKey(operatorKey)
                    .setKycKey(operatorKey)
                    .setSupplyKey(operatorKey)
                    .setFreezeDefault(false)
                    .execute(client)
                    .getReceipt(client)
                    .tokenId
            );

            new TokenAssociateTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setAccountId(accountId)
                .setTokenIds(Collections.singletonList(tokenId))
                .freezeWith(client)
                .sign(key)
                .execute(client)
                .getReceipt(client);

            new TokenGrantKycTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setAccountId(accountId)
                .setTokenId(tokenId)
                .execute(client)
                .getReceipt(client);

            new TransferTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .addTokenTransfer(tokenId, operatorId, -10)
                .addTokenTransfer(tokenId, accountId, 10)
                .execute(client)
                .getReceipt(client);

            new TokenWipeTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setTokenId(tokenId)
                .setAccountId(accountId)
                .setAmount(10)
                .execute(client)
                .getReceipt(client);

            new AccountDeleteTransaction()
                .setAccountId(accountId)
                .setTransferAccountId(operatorId)
                .freezeWith(client)
                .sign(key)
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }

    @Test
    @DisplayName("Cannot wipe accounts balance when account ID is not set")
    void cannotWipeAccountsBalanceWhenAccountIDIsNotSet() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(client);

            var accountId = Objects.requireNonNull(response.getReceipt(client).accountId);

            var tokenId = Objects.requireNonNull(
                new TokenCreateTransaction()
                    .setTokenName("ffff")
                    .setTokenSymbol("F")
                    .setDecimals(3)
                    .setInitialSupply(1000000)
                    .setTreasuryAccountId(operatorId)
                    .setAdminKey(operatorKey)
                    .setFreezeKey(operatorKey)
                    .setWipeKey(operatorKey)
                    .setKycKey(operatorKey)
                    .setSupplyKey(operatorKey)
                    .setFreezeDefault(false)
                    .execute(client)
                    .getReceipt(client)
                    .tokenId
            );

            new TokenAssociateTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setAccountId(accountId)
                .setTokenIds(Collections.singletonList(tokenId))
                .freezeWith(client)
                .sign(key)
                .execute(client)
                .getReceipt(client);

            new TokenGrantKycTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setAccountId(accountId)
                .setTokenId(tokenId)
                .execute(client)
                .getReceipt(client);

            new TransferTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .addTokenTransfer(tokenId, operatorId, -10)
                .addTokenTransfer(tokenId, accountId, 10)
                .execute(client)
                .getReceipt(client);

            var error = assertThrows(PrecheckStatusException.class, () -> {
                new TokenWipeTransaction()
                    .setNodeAccountIds(Collections.singletonList(response.nodeId))
                    .setTokenId(tokenId)
                    .setAmount(10)
                    .execute(client)
                    .getReceipt(client);
            });

            assertTrue(error.getMessage().contains(Status.INVALID_ACCOUNT_ID.toString()));

            client.close();
        });
    }

    @Test
    @DisplayName("Cannot wipe accounts balance when token ID is not set")
    void cannotWipeAccountsBalanceWhenTokenIDIsNotSet() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(client);

            var accountId = Objects.requireNonNull(response.getReceipt(client).accountId);

            var tokenId = Objects.requireNonNull(
                new TokenCreateTransaction()
                    .setTokenName("ffff")
                    .setTokenSymbol("F")
                    .setDecimals(3)
                    .setInitialSupply(1000000)
                    .setTreasuryAccountId(operatorId)
                    .setAdminKey(operatorKey)
                    .setFreezeKey(operatorKey)
                    .setWipeKey(operatorKey)
                    .setKycKey(operatorKey)
                    .setSupplyKey(operatorKey)
                    .setFreezeDefault(false)
                    .execute(client)
                    .getReceipt(client)
                    .tokenId
            );

            new TokenAssociateTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setAccountId(accountId)
                .setTokenIds(Collections.singletonList(tokenId))
                .freezeWith(client)
                .sign(key)
                .execute(client)
                .getReceipt(client);

            new TokenGrantKycTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setAccountId(accountId)
                .setTokenId(tokenId)
                .execute(client)
                .getReceipt(client);

            new TransferTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .addTokenTransfer(tokenId, operatorId, -10)
                .addTokenTransfer(tokenId, accountId, 10)
                .execute(client)
                .getReceipt(client);

            var error = assertThrows(PrecheckStatusException.class, () -> {
                new TokenWipeTransaction()
                    .setNodeAccountIds(Collections.singletonList(response.nodeId))
                    .setAccountId(accountId)
                    .setAmount(10)
                    .execute(client)
                    .getReceipt(client);
            });

            assertTrue(error.getMessage().contains(Status.INVALID_TOKEN_ID.toString()));

            client.close();
        });
    }

    @Test
    @DisplayName("Cannot wipe accounts balance when amount is not set")
    void cannotWipeAccountsBalanceWhenAmountIsNotSet() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(client);

            var accountId = Objects.requireNonNull(response.getReceipt(client).accountId);

            var tokenId = Objects.requireNonNull(
                new TokenCreateTransaction()
                    .setTokenName("ffff")
                    .setTokenSymbol("F")
                    .setDecimals(3)
                    .setInitialSupply(1000000)
                    .setTreasuryAccountId(operatorId)
                    .setAdminKey(operatorKey)
                    .setFreezeKey(operatorKey)
                    .setWipeKey(operatorKey)
                    .setKycKey(operatorKey)
                    .setSupplyKey(operatorKey)
                    .setFreezeDefault(false)
                    .execute(client)
                    .getReceipt(client)
                    .tokenId
            );

            new TokenAssociateTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setAccountId(accountId)
                .setTokenIds(Collections.singletonList(tokenId))
                .freezeWith(client)
                .sign(key)
                .execute(client)
                .getReceipt(client);

            new TokenGrantKycTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setAccountId(accountId)
                .setTokenId(tokenId)
                .execute(client)
                .getReceipt(client);

            new TransferTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .addTokenTransfer(tokenId, operatorId, -10)
                .addTokenTransfer(tokenId, accountId, 10)
                .execute(client)
                .getReceipt(client);

            var error = assertThrows(PrecheckStatusException.class, () -> {
                new TokenWipeTransaction()
                    .setNodeAccountIds(Collections.singletonList(response.nodeId))
                    .setTokenId(tokenId)
                    .setAccountId(accountId)
                    .execute(client)
                    .getReceipt(client);
            });

            assertTrue(error.getMessage().contains(Status.INVALID_WIPING_AMOUNT.toString()));

            client.close();
        });
    }
}
