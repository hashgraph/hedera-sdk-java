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

class TokenDeleteIntegrationTest {
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(System.getProperty("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(System.getProperty("OPERATOR_KEY")));

    @Test
    void test() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();

            TransactionResponse response = new TokenCreateTransaction()
                .setName("ffff")
                .setSymbol("F")
                .setDecimals(3)
                .setInitialSupply(1000000)
                .setTreasury(OPERATOR_ID)
                .setAdminKey(OPERATOR_KEY.getPublicKey())
                .setFreezeKey(OPERATOR_KEY.getPublicKey())
                .setWipeKey(OPERATOR_KEY.getPublicKey())
                .setKycKey(OPERATOR_KEY.getPublicKey())
                .setSupplyKey(OPERATOR_KEY.getPublicKey())
                .setFreezeDefault(false)
                .setExpirationTime(Instant.now().plus(Duration.ofDays(90)).getEpochSecond())
                .execute(client);

            TokenId tokenId = response.getReceipt(client).tokenId;

            new TokenDeleteTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setTokenId(tokenId)
                .execute(client)
                .getReceipt(client);
        });
    }
}
