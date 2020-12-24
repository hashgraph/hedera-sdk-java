import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class TokenFreezeIntegrationTest {
    @Test
    @DisplayName("Can freeze account with token")
    void canFreezeAccountWithToken() {
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

            new TokenFreezeTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setAccountId(accountId)
                .setTokenId(tokenId)
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
    @DisplayName("Cannot freeze account on token when token ID is not set")
    void cannotFreezeAccountOnTokenWhenTokenIDIsNotSet() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());

            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(client);

            var accountId = Objects.requireNonNull(response.getReceipt(client).accountId);

            var error = assertThrows(HederaPreCheckStatusException.class, () -> {
                new TokenFreezeTransaction()
                    .setNodeAccountIds(Collections.singletonList(response.nodeId))
                    .setAccountId(accountId)
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

            assertTrue(error.getMessage().contains(Status.INVALID_TOKEN_ID.toString()));

            client.close();
        });
    }

    @Test
    @DisplayName("Cannot freeze account on token when account ID is not set")
    void cannotFreezeAccountOnTokenWhenAccountIDIsNotSet() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var key = PrivateKey.generate();

            var response = new TokenCreateTransaction()
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
                .execute(client);

            var tokenId = Objects.requireNonNull(response.getReceipt(client).tokenId);

            var error = assertThrows(HederaPreCheckStatusException.class, () -> {
                new TokenFreezeTransaction()
                    .setNodeAccountIds(Collections.singletonList(response.nodeId))
                    .setTokenId(tokenId)
                    .freezeWith(client)
                    .sign(key)
                    .execute(client)
                    .getReceipt(client);
            });

            assertTrue(error.getMessage().contains(Status.INVALID_ACCOUNT_ID.toString()));

            client.close();
        });
    }

    @Test
    @DisplayName("Cannot freeze account on token when account was not associated with")
    void cannotFreezeAccountOnTokenWhenAccountWasNotAssociatedWith() {
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

            var error = assertThrows(HederaPreCheckStatusException.class, () -> {
                new TokenFreezeTransaction()
                    .setNodeAccountIds(Collections.singletonList(response.nodeId))
                    .setAccountId(accountId)
                    .setTokenId(tokenId)
                    .freezeWith(client)
                    .sign(key)
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
                .signWithOperator(client)
                .sign(key)
                .execute(client)
                .getReceipt(client);

            assertTrue(error.getMessage().contains(Status.TOKEN_NOT_ASSOCIATED_TO_ACCOUNT.toString()));

            client.close();
        });
    }
}
