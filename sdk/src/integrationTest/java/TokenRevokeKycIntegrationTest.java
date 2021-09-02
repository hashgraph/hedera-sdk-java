import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TokenAssociateTransaction;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenRevokeKycTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenRevokeKycIntegrationTest {
    @Test
    @DisplayName("Can revoke kyc to account with token")
    void canRevokeKycAccountWithToken() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var key = PrivateKey.generate();

        var response = new AccountCreateTransaction()
            .setKey(key)
            .setInitialBalance(new Hbar(1))
            .execute(testEnv.client);

        var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

        var tokenId = Objects.requireNonNull(
            new TokenCreateTransaction()
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
            .setAccountId(accountId)
            .setTokenIds(Collections.singletonList(tokenId))
            .freezeWith(testEnv.client)
            .sign(key)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new TokenRevokeKycTransaction()
            .setAccountId(accountId)
            .setTokenId(tokenId)
            .freezeWith(testEnv.client)
            .sign(key)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        testEnv.close(tokenId, accountId, key);
    }

    @Test
    @DisplayName("Cannot revoke kyc to account on token when token ID is not set")
    void cannotRevokeKycToAccountOnTokenWhenTokenIDIsNotSet() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var key = PrivateKey.generate();

        var response = new AccountCreateTransaction()
            .setKey(key)
            .setInitialBalance(new Hbar(1))
            .execute(testEnv.client);

        var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

        var error = assertThrows(PrecheckStatusException.class, () -> {
            new TokenRevokeKycTransaction()
                .setAccountId(accountId)
                .freezeWith(testEnv.client)
                .sign(key)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        });

        assertTrue(error.getMessage().contains(Status.INVALID_TOKEN_ID.toString()));

        testEnv.close(accountId, key);
    }

    @Test
    @DisplayName("Cannot revoke kyc to account on token when account ID is not set")
    void cannotRevokeKycToAccountOnTokenWhenAccountIDIsNotSet() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var key = PrivateKey.generate();

        var response = new TokenCreateTransaction()
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
            .execute(testEnv.client);

        var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

        var error = assertThrows(PrecheckStatusException.class, () -> {
            new TokenRevokeKycTransaction()
                .setTokenId(tokenId)
                .freezeWith(testEnv.client)
                .sign(key)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        });

        assertTrue(error.getMessage().contains(Status.INVALID_ACCOUNT_ID.toString()));

        testEnv.close(tokenId);
    }

    @Test
    @DisplayName("Cannot revoke kyc to account on token when account was not associated with")
    void cannotRevokeKycToAccountOnTokenWhenAccountWasNotAssociatedWith() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var key = PrivateKey.generate();

        var response = new AccountCreateTransaction()
            .setKey(key)
            .setInitialBalance(new Hbar(1))
            .execute(testEnv.client);

        var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

        var tokenId = Objects.requireNonNull(
            new TokenCreateTransaction()
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
            new TokenRevokeKycTransaction()
                .setAccountId(accountId)
                .setTokenId(tokenId)
                .freezeWith(testEnv.client)
                .sign(key)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        });

        assertTrue(error.getMessage().contains(Status.TOKEN_NOT_ASSOCIATED_TO_ACCOUNT.toString()));

        testEnv.close(tokenId, accountId, key);
    }
}
