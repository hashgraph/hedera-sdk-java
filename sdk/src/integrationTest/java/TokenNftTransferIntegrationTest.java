import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.util.Collections;
import java.util.Objects;
import java.util.ArrayList;

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
                .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
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

            var mintReceipt = new TokenMintTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTokenId(testEnv.newTokenId)
                .setMetadata(NftMetadataGenerator.generate((byte)10))
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

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

            var serialsToTransfer = new ArrayList<Long>(mintReceipt.serials.subList(0, 4));
            var transfer = new TransferTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds);
            for(var serial : serialsToTransfer) {
                transfer.addNftTransfer(testEnv.newTokenId.nft(serial), testEnv.operatorId, testEnv.newAccountId);
            }
            transfer.execute(testEnv.client).getReceipt(testEnv.client);

            new TokenWipeTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTokenId(testEnv.newTokenId)
                .setAccountId(testEnv.newAccountId)
                .setSerials(serialsToTransfer)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.cleanUpAndClose();
        });
    }
}
