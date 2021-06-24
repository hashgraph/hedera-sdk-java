import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled
class TokenNftTransferIntegrationTest {
    @Test
    void test() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            PrivateKey key = PrivateKey.generate();

            TransactionResponse response = new AccountCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(testEnv.client);

            AccountId accountId = response.getReceipt(testEnv.client).accountId;
            assertNotNull(accountId);

            response = new TokenCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setDecimals(3)
                .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .setFreezeKey(testEnv.operatorKey)
                .setWipeKey(testEnv.operatorKey)
                .setKycKey(testEnv.operatorKey)
                .setSupplyKey(testEnv.operatorKey)
                .setFreezeDefault(false)
                .execute(testEnv.client);

            TokenId tokenId = response.getReceipt(testEnv.client).tokenId;
            assertNotNull(tokenId);

            var mintReceipt = new TokenMintTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTokenId(tokenId)
                .setAmount(100)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TokenAssociateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setAccountId(accountId)
                .setTokenIds(Collections.singletonList(tokenId))
                .freezeWith(testEnv.client)
                .signWithOperator(testEnv.client)
                .sign(key)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TokenGrantKycTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setAccountId(accountId)
                .setTokenId(tokenId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var serialsToTransfer = mintReceipt.serials.subList(0, 10);

            new TransferTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .addTokenNftTransfers(tokenId, serialsToTransfer, testEnv.operatorId, accountId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TokenWipeTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTokenId(tokenId)
                .setAccountId(accountId)
                .setSerials(serialsToTransfer)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.client.close();
        });
    }
}
