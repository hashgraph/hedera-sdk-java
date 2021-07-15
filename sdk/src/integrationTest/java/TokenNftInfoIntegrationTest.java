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

            testEnv.cleanUpAndClose(tokenId);
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

            List<byte[]> metadatas = NftMetadataGenerator.generate((byte)10);

            var mintReceipt = new TokenMintTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTokenId(tokenId)
                .setMetadata(metadatas)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var nftInfos = new TokenNftInfoQuery()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .byAccountId(testEnv.operatorId)
                .setEnd(10)
                .execute(testEnv.client);

            assertEquals(nftInfos.size(), 10);

            var serials = new ArrayList<Long>(mintReceipt.serials);

            for(var info : nftInfos) {
                assertEquals(info.nftId.tokenId, tokenId);
                assertTrue(serials.remove(info.nftId.serial));
                assertEquals(info.accountId, testEnv.operatorId);
            }

            testEnv.cleanUpAndClose(tokenId);
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

            List<byte[]> metadatas = NftMetadataGenerator.generate((byte)10);

            var mintReceipt = new TokenMintTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTokenId(tokenId)
                .setMetadata(metadatas)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var nftInfos = new TokenNftInfoQuery()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .byTokenId(tokenId)
                .setEnd(10)
                .execute(testEnv.client);

            assertEquals(nftInfos.size(), 10);

            var serials = new ArrayList<Long>(mintReceipt.serials);

            for(var info : nftInfos) {
                assertEquals(info.nftId.tokenId, tokenId);
                assertTrue(serials.remove(info.nftId.serial));
                assertEquals(info.accountId, testEnv.operatorId);
            }

            testEnv.cleanUpAndClose(tokenId);
        });
    }
}



