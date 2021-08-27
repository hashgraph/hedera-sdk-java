import com.hedera.hashgraph.sdk.NftId;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenMintTransaction;
import com.hedera.hashgraph.sdk.TokenNftInfoQuery;
import com.hedera.hashgraph.sdk.TokenType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class TokenNftInfoIntegrationTest {

    @Test
    @DisplayName("Can query NFT info by NftId")
    void canQueryNftInfoByNftId() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

            var createReceipt = new TokenCreateTransaction()
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
                .setTokenId(tokenId)
                .addMetadata(metadata)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var nftId = tokenId.nft(mintReceipt.serials.get(0));

            var nftInfos = new TokenNftInfoQuery()
                .byNftId(nftId)
                .execute(testEnv.client);

            assertEquals(nftInfos.size(), 1);
            assertEquals(nftInfos.get(0).nftId, nftId);
            assertEquals(nftInfos.get(0).accountId, testEnv.operatorId);
            assertEquals(nftInfos.get(0).metadata[0], 50);

            testEnv.close(tokenId);
        });
    }

    @Test
    @DisplayName("Cannot query NFT info by invalid NftId")
    void cannotQueryNftInfoByInvalidNftId() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

            var createReceipt = new TokenCreateTransaction()
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
                .setTokenId(tokenId)
                .addMetadata(metadata)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var nftId = tokenId.nft(mintReceipt.serials.get(0));
            var invalidNftId = new NftId(nftId.tokenId, nftId.serial + 1);

            var error = assertThrows(PrecheckStatusException.class, () -> {
                new TokenNftInfoQuery()
                    .byNftId(invalidNftId)
                    .execute(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.INVALID_NFT_ID.toString()));

            testEnv.close(tokenId);
        });
    }

    @Test
    @DisplayName("Cannot query NFT info by invalid NftId Serial Number")
    void cannotQueryNftInfoByInvalidSerialNumber() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

            var createReceipt = new TokenCreateTransaction()
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
                .setTokenId(tokenId)
                .addMetadata(metadata)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var nftId = tokenId.nft(mintReceipt.serials.get(0));
            var invalidNftId = new NftId(nftId.tokenId, -1L);

            var error = assertThrows(PrecheckStatusException.class, () -> {
                new TokenNftInfoQuery()
                    .byNftId(invalidNftId)
                    .execute(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.INVALID_TOKEN_NFT_SERIAL_NUMBER.toString()));

            testEnv.close(tokenId);
        });
    }

    @Disabled
    @Test
    @DisplayName("Can query NFT info by AccountId")
    void canQueryNftInfoByAccountId() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

            var createReceipt = new TokenCreateTransaction()
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

            List<byte[]> metadatas = NftMetadataGenerator.generate((byte) 10);

            var mintReceipt = new TokenMintTransaction()
                .setTokenId(tokenId)
                .setMetadata(metadatas)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var nftInfos = new TokenNftInfoQuery()
                .byAccountId(testEnv.operatorId)
                .setEnd(10)
                .execute(testEnv.client);

            assertEquals(nftInfos.size(), 10);

            var serials = new ArrayList<Long>(mintReceipt.serials);

            for (var info : nftInfos) {
                assertEquals(info.nftId.tokenId, tokenId);
                assertTrue(serials.remove(info.nftId.serial));
                assertEquals(info.accountId, testEnv.operatorId);
            }

            testEnv.close(tokenId);
        });
    }

    @Disabled
    @Test
    @DisplayName("Can query NFT info by TokenId")
    void canQueryNftInfoByTokenId() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

            var createReceipt = new TokenCreateTransaction()
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

            List<byte[]> metadatas = NftMetadataGenerator.generate((byte) 10);

            var mintReceipt = new TokenMintTransaction()
                .setTokenId(tokenId)
                .setMetadata(metadatas)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var nftInfos = new TokenNftInfoQuery()
                .byTokenId(tokenId)
                .setEnd(10)
                .execute(testEnv.client);

            assertEquals(nftInfos.size(), 10);

            var serials = new ArrayList<Long>(mintReceipt.serials);

            for (var info : nftInfos) {
                assertEquals(info.nftId.tokenId, tokenId);
                assertTrue(serials.remove(info.nftId.serial));
                assertEquals(info.accountId, testEnv.operatorId);
            }

            testEnv.close(tokenId);
        });
    }
}



