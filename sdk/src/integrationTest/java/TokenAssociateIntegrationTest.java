import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenAssociateIntegrationTest {
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(System.getProperty("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(System.getProperty("OPERATOR_KEY")));

    @Test
    void test() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();

            PrivateKey key = PrivateKey.generate();

            TransactionResponse response = new AccountCreateTransaction()
                .setKey(key)
                .setMaxTransactionFee(new Hbar(2))
                .setInitialBalance(new Hbar(1))
                .execute(client);

            AccountId accountId = response.getReceipt(client).accountId;
            assertNotNull(accountId);

            response = new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setDecimals(3)
                .setInitialSupply(1000000)
                .setTreasuryAccountId(OPERATOR_ID)
                .setAdminKey(OPERATOR_KEY.getPublicKey())
                .setFreezeKey(OPERATOR_KEY.getPublicKey())
                .setWipeKey(OPERATOR_KEY.getPublicKey())
                .setKycKey(OPERATOR_KEY.getPublicKey())
                .setSupplyKey(OPERATOR_KEY.getPublicKey())
                .setFreezeDefault(false)
                .execute(client);

            TokenId tokenId = response.getReceipt(client).tokenId;
            assertNotNull(tokenId);

                new TokenAssociateTransaction()
                    .setNodeAccountIds(Collections.singletonList(response.nodeId))
                    .setAccountId(accountId)
                    .setTokenIds(tokenId)
                    .freezeWith(client)
                    .sign(OPERATOR_KEY)
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
                .setTransferAccountId(OPERATOR_ID)
                .freezeWith(client)
                .sign(OPERATOR_KEY)
                .sign(key)
                .execute(client)
                .getReceipt(client);
        });
    }
}
