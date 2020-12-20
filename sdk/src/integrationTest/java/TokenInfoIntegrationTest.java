import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TokenInfoIntegrationTest {
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(System.getProperty("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(System.getProperty("OPERATOR_KEY")));

    @Test
    void test() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();

            TransactionResponse response = new TokenCreateTransaction()
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

                TokenInfo info = new TokenInfoQuery()
                    .setNodeAccountIds(Collections.singletonList(response.nodeId))
                    .setQueryPayment(new Hbar(2))
                    .setTokenId(tokenId)
                    .execute(client);

            assertEquals(tokenId, info.tokenId);
            assertEquals(info.name, "ffff");
            assertEquals(info.symbol, "F");
            assertEquals(info.decimals, 3);
            assertEquals(OPERATOR_ID, info.treasuryAccountId);
            assertEquals(OPERATOR_KEY.getPublicKey().toString(), info.adminKey.toString());
            assertEquals(OPERATOR_KEY.getPublicKey().toString(), info.kycKey.toString());
            assertEquals(OPERATOR_KEY.getPublicKey().toString(), info.freezeKey.toString());
            assertEquals(OPERATOR_KEY.getPublicKey().toString(), info.wipeKey.toString());
            assertEquals(OPERATOR_KEY.getPublicKey().toString(), info.supplyKey.toString());
            assertNotNull(info.defaultFreezeStatus);
            assertFalse(info.defaultFreezeStatus);
            assertNotNull(info.defaultKycStatus);
            assertFalse(info.defaultKycStatus);

                new TokenDeleteTransaction()
                    .setNodeAccountIds(Collections.singletonList(response.nodeId))
                    .setTokenId(tokenId)
                    .execute(client)
                    .getReceipt(client);

            client.close();
        });
    }
}
