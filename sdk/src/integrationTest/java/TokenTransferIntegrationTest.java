import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TokenTransferIntegrationTest {
    @Test
    void test() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            testEnv.newAccountKey = PrivateKey.generate();

            TransactionResponse response = new AccountCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setKey(testEnv.newAccountKey)
                .setInitialBalance(new Hbar(1))
                .execute(testEnv.client);

            testEnv.newAccountId = response.getReceipt(testEnv.client).accountId;
            assertNotNull(testEnv.newAccountId);

            response = new TokenCreateTransaction()
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
                .execute(testEnv.client);

            testEnv.newTokenId = response.getReceipt(testEnv.client).tokenId;
            assertNotNull(testEnv.newTokenId);

            new TokenAssociateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setAccountId(testEnv.newAccountId)
                .setTokenIds(Collections.singletonList(testEnv.newTokenId))
                .freezeWith(testEnv.client)
                .signWithOperator(testEnv.client)
                .sign(testEnv.newAccountKey)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TokenGrantKycTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setAccountId(testEnv.newAccountId)
                .setTokenId(testEnv.newTokenId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TransferTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .addTokenTransfer(testEnv.newTokenId, testEnv.operatorId, -10)
                .addTokenTransfer(testEnv.newTokenId, testEnv.newAccountId, 10)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.cleanUpAndClose();
        });
    }
}
