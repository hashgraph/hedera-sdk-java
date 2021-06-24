import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.util.Collections;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
class TokenNftInfoIntegrationTest {

    @Test
    @DisplayName("Can query NFT info by NftId")
    void canQueryNftInfoByNftId() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var createReceipt = new TokenCreateTransaction()
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
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var tokenId = Objects.requireNonNull(createReceipt.tokenId);
            
            byte[] metadata = {50};

            var mintReceipt = new TokenMintTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTokenId(tokenId)
                .setAmount(1)
                .addMetadata(metadata)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var nftId = tokenId.nft(mintReceipt.serials.get(0));

            var nftInfos = new TokenNftInfoQuery()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .byNftId(nftId)
                .execute(testEnv.client);
                
            assertEquals(nftInfos.size(), 1);
            assertEquals(nftInfos.get(0).nftId, nftId);
            assertEquals(nftInfos.get(0).accountId, testEnv.operatorId);
            assertEquals(nftInfos.get(0).metadata[0], 50);

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Can query NFT info by AccountId")
    void canQueryNftInfoByAccountId() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var createReceipt = new TokenCreateTransaction()
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
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var tokenId = Objects.requireNonNull(createReceipt.tokenId);

            List<byte[]> metadatas = new ArrayList<>();
            for(byte i = 0; i < 100; i++) {
                byte[] md = {i};
                metadatas.add(md);
            }
            
            var mintReceipt = new TokenMintTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTokenId(tokenId)
                .setAmount(100)
                .addMetadatas(metadatas)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var nftInfos = new TokenNftInfoQuery()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .byAccountId(testEnv.operatorId)
                .setEnd(100)
                .execute(testEnv.client);
            
            assertEquals(nftInfos.size(), 100);

            var serials = mintReceipt.serials;

            for(var info : nftInfos) {
                assertEquals(info.nftId.tokenId, tokenId);
                assertTrue(serials.remove(info.nftId.serial));
                assertEquals(info.accountId, testEnv.operatorId);
                assertTrue(metadatas.remove(info.metadata));
            }

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Can query NFT info by TokenId")
    void canQueryNftInfoByTokenId() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var createReceipt = new TokenCreateTransaction()
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
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var tokenId = Objects.requireNonNull(createReceipt.tokenId);

            List<byte[]> metadatas = new ArrayList<>();
            for(byte i = 0; i < 100; i++) {
                byte[] md = {i};
                metadatas.add(md);
            }
            
            var mintReceipt = new TokenMintTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTokenId(tokenId)
                .setAmount(100)
                .addMetadatas(metadatas)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var nftInfos = new TokenNftInfoQuery()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .byTokenId(tokenId)
                .setEnd(100)
                .execute(testEnv.client);
            
            assertEquals(nftInfos.size(), 100);

            var serials = mintReceipt.serials;

            for(var info : nftInfos) {
                assertEquals(info.nftId.tokenId, tokenId);
                assertTrue(serials.remove(info.nftId.serial));
                assertEquals(info.accountId, testEnv.operatorId);
                assertTrue(metadatas.remove(info.metadata));
            }

            testEnv.client.close();
        });
    }
}



