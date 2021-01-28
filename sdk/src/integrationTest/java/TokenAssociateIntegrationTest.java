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

            new TokenDeleteTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setTokenId(tokenId)
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
    @DisplayName("Can execute token associate transaction even when token IDs are not set")
    void canExecuteTokenAssociateTransactionEvenWhenTokenIDsAreNotSet() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());

            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(client);

            var accountId = Objects.requireNonNull(response.getReceipt(client).accountId);

            new TokenAssociateTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setAccountId(accountId)
                .freezeWith(client)
                .sign(key)
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
    @DisplayName("Cannot associate account with tokens when account ID is not set")
    void cannotAssociateAccountWithTokensWhenAccountIDIsNotSet() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());

            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(client);

            var accountId = Objects.requireNonNull(response.getReceipt(client).accountId);

            var error = assertThrows(PrecheckStatusException.class, () -> {
                new TokenAssociateTransaction()
                    .setNodeAccountIds(Collections.singletonList(response.nodeId))
                    .freezeWith(client)
                    .sign(key)
                    .execute(client)
                    .getReceipt(client);
            });

            new AccountDeleteTransaction()
                .setAccountId(accountId)
                .setTransferAccountId(operatorId)
                .freezeWith(client)
                .sign(key)
                .execute(client)
                .getReceipt(client);

            assertTrue(error.getMessage().contains(Status.INVALID_ACCOUNT_ID.toString()));

            client.close();
        });
    }

    @Test
    @DisplayName("Cannot associate account with tokens when account does not sign transaction")
    void cannotAssociateAccountWhenAccountDoesNotSignTransaction() {
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

            var error = assertThrows(PrecheckStatusException.class, () -> {
                new TokenAssociateTransaction()
                    .setNodeAccountIds(Collections.singletonList(response.nodeId))
                    .setAccountId(accountId)
                    .setTokenIds(Collections.singletonList(tokenId))
                    .execute(client)
                    .getReceipt(client);
            });

            new TokenDeleteTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setTokenId(tokenId)
                .execute(client)
                .getReceipt(client);

            new AccountDeleteTransaction()
                .setAccountId(accountId)
                .setTransferAccountId(operatorId)
                .freezeWith(client)
                .sign(key)
                .execute(client)
                .getReceipt(client);

            assertTrue(error.getMessage().contains(Status.INVALID_SIGNATURE.toString()));

            client.close();
        });
    }
}
