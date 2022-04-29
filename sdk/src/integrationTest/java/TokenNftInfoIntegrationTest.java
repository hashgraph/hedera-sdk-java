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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class TokenNftInfoIntegrationTest {

    @Test
    @DisplayName("Can query NFT info by NftId")
    void canQueryNftInfoByNftId() throws Exception {
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
            .setNftId(nftId)
            .execute(testEnv.client);

        assertThat(nftInfos.size()).isEqualTo(1);
        assertThat(nftInfos.get(0).nftId).isEqualTo(nftId);
        assertThat(nftInfos.get(0).accountId).isEqualTo(testEnv.operatorId);
        assertThat(nftInfos.get(0).metadata[0]).isEqualTo(50);

        testEnv.close(tokenId);
    }

    @Test
    @DisplayName("Cannot query NFT info by invalid NftId")
    void cannotQueryNftInfoByInvalidNftId() throws Exception {
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

        assertThatExceptionOfType(PrecheckStatusException.class).isThrownBy(() -> {
            new TokenNftInfoQuery()
                .setNftId(invalidNftId)
                .execute(testEnv.client);
        }).withMessageContaining(Status.INVALID_NFT_ID.toString());

        testEnv.close(tokenId);
    }

    @Test
    @DisplayName("Cannot query NFT info by invalid NftId Serial Number")
    void cannotQueryNftInfoByInvalidSerialNumber() throws Exception {
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

        assertThatExceptionOfType(PrecheckStatusException.class).isThrownBy(() -> {
            new TokenNftInfoQuery()
                .byNftId(invalidNftId)
                .execute(testEnv.client);
        }).withMessageContaining(Status.INVALID_TOKEN_NFT_SERIAL_NUMBER.toString());

        testEnv.close(tokenId);
    }

    @Disabled
    @Test
    @DisplayName("Can query NFT info by AccountId")
    void canQueryNftInfoByAccountId() throws Exception {
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

        assertThat(nftInfos.size()).isEqualTo(10);

        var serials = new ArrayList<Long>(mintReceipt.serials);

        for (var info : nftInfos) {
            assertThat(info.nftId.tokenId).isEqualTo(tokenId);
            assertThat(serials.remove(info.nftId.serial)).isTrue();
            assertThat(info.accountId).isEqualTo(testEnv.operatorId);
        }

        testEnv.close(tokenId);
    }

    @Disabled
    @Test
    @DisplayName("Can query NFT info by TokenId")
    void canQueryNftInfoByTokenId() throws Exception {
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

        assertThat(nftInfos.size()).isEqualTo(10);

        var serials = new ArrayList<Long>(mintReceipt.serials);

        for (var info : nftInfos) {
            assertThat(info.nftId.tokenId).isEqualTo(tokenId);
            assertThat(serials.remove(info.nftId.serial)).isTrue();
            assertThat(info.accountId).isEqualTo(testEnv.operatorId);
        }

        testEnv.close(tokenId);
    }
}



