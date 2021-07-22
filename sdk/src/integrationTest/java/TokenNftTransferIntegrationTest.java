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
            var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

            var key = PrivateKey.generate();

            TransactionResponse response = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(testEnv.client);

            var accountId = response.getReceipt(testEnv.client).accountId;
            assertNotNull(accountId);

            response = new TokenCreateTransaction()
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

            var tokenId = response.getReceipt(testEnv.client).tokenId;
            assertNotNull(tokenId);

            var mintReceipt = new TokenMintTransaction()
                .setTokenId(tokenId)
                .setMetadata(NftMetadataGenerator.generate((byte)10))
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TokenAssociateTransaction()
                .setAccountId(accountId)
                .setTokenIds(Collections.singletonList(tokenId))
                .freezeWith(testEnv.client)
                .signWithOperator(testEnv.client)
                .sign(key)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TokenGrantKycTransaction()
                .setAccountId(accountId)
                .setTokenId(tokenId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var serialsToTransfer = new ArrayList<Long>(mintReceipt.serials.subList(0, 4));
            var transfer = new TransferTransaction();
            for(var serial : serialsToTransfer) {
                transfer.addNftTransfer(tokenId.nft(serial), testEnv.operatorId, accountId);
            }
            transfer.execute(testEnv.client).getReceipt(testEnv.client);

            new TokenWipeTransaction()
                .setTokenId(tokenId)
                .setAccountId(accountId)
                .setSerials(serialsToTransfer)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.close(tokenId, accountId, key);
        });
    }
}
