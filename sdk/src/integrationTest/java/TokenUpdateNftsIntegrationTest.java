import static org.assertj.core.api.Assertions.assertThat;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.NftId;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenId;
import com.hedera.hashgraph.sdk.TokenInfoQuery;
import com.hedera.hashgraph.sdk.TokenMintTransaction;
import com.hedera.hashgraph.sdk.TokenNftInfoQuery;
import com.hedera.hashgraph.sdk.TokenType;
import com.hedera.hashgraph.sdk.TokenUpdateNftsTransaction;
import com.hedera.hashgraph.sdk.TokenUpdateTransaction;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @notice E2E-HIP-657
 * @url https://hips.hedera.com/hip/hip-657
 */
public class TokenUpdateNftsIntegrationTest {

    @Test
    @DisplayName("Can update NFT metadata")
    void canUpdateNFTMetadata() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var metadataKey = PrivateKey.generateED25519();
        var nftCount = 4;
        var initialMetadataList = NftMetadataGenerator.generate(new byte[]{4, 2, 0}, nftCount);
        var updatedMetadata = new byte[]{6, 9};
        var updatedMetadataList = NftMetadataGenerator.generate(updatedMetadata, nftCount / 2);

        // create a token with metadata key
        var tokenId = Objects.requireNonNull(
            new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .setSupplyKey(testEnv.operatorKey)
                .setMetadataKey(metadataKey)
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .tokenId
        );

        // mint tokens
        var tokenMintTransactionReceipt = new TokenMintTransaction()
            .setMetadata(initialMetadataList)
            .setTokenId(tokenId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        // check that metadata was set correctly
        var nftSerials = tokenMintTransactionReceipt.serials;
        List<byte[]> metadataListAfterMint = getMetadataList(testEnv.client, tokenId, nftSerials);

        assertThat(metadataListAfterMint.toArray()).isEqualTo(initialMetadataList.toArray());

        // update metadata of the first two minted NFTs
        var tokenUpdateNftsTransactionReceipt = new TokenUpdateNftsTransaction()
            .setTokenId(tokenId)
            .setSerials(nftSerials.subList(0, nftCount / 2))
            .setMetadata(updatedMetadata)
            .sign(metadataKey)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        // check updated NFTs' metadata
        var nftSerialsUpdated = tokenUpdateNftsTransactionReceipt.serials.subList(0, nftCount / 2);
        List<byte[]> metadataListAfterUpdate = getMetadataList(testEnv.client, tokenId, nftSerialsUpdated);

        assertThat(metadataListAfterUpdate.toArray()).isEqualTo(updatedMetadataList.toArray());

        // check that remaining NFTs were not updated
        var nftSerialsSame = tokenUpdateNftsTransactionReceipt.serials.subList(nftCount / 2, nftCount);
        List<byte[]> metadataList = getMetadataList(testEnv.client, tokenId, nftSerialsSame);

        assertThat(metadataList.toArray()).isEqualTo(initialMetadataList.subList(nftCount / 2, nftCount).toArray());

        testEnv.close(tokenId);
    }

    @Test
    @DisplayName("Can update NFT metadata after setting metadata key")
    void canUpdateNFTMetadataAfterMetadataKeySet() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var metadataKey = PrivateKey.generateED25519();
        var nftCount = 4;
        var initialMetadataList = NftMetadataGenerator.generate(new byte[]{4, 2, 0}, nftCount);
        var updatedMetadata = new byte[]{6, 9};
        var updatedMetadataList = NftMetadataGenerator.generate(updatedMetadata, nftCount / 2);

        // create a token without a metadata key and check it
        var tokenId = Objects.requireNonNull(
            new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .setSupplyKey(testEnv.operatorKey)
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .tokenId
        );

        var tokenInfo = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(testEnv.client);

        assertThat(tokenInfo.metadataKey).isNull();

        // update a token with a metadata key and check that it was updated
        new TokenUpdateTransaction()
            .setTokenId(tokenId)
            .setMetadataKey(metadataKey)
            .execute(testEnv.client);

        var tokenInfoAfterUpdate = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(testEnv.client);

        assertThat(tokenInfoAfterUpdate.metadataKey.toString()).isEqualTo(metadataKey.getPublicKey().toString());

        // mint tokens
        var tokenMintTransactionReceipt = new TokenMintTransaction()
            .setMetadata(initialMetadataList)
            .setTokenId(tokenId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        // check that metadata was set correctly
        var nftSerials = tokenMintTransactionReceipt.serials;
        List<byte[]> metadataListAfterMint = getMetadataList(testEnv.client, tokenId, nftSerials);

        assertThat(metadataListAfterMint.toArray()).isEqualTo(initialMetadataList.toArray());

        // update metadata of the first two minted NFTs
        var tokenUpdateNftsTransactionReceipt = new TokenUpdateNftsTransaction()
            .setTokenId(tokenId)
            .setSerials(nftSerials.subList(0, nftCount / 2))
            .setMetadata(updatedMetadata)
            .sign(metadataKey)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        // check updated NFTs' metadata
        var nftSerialsUpdated = tokenUpdateNftsTransactionReceipt.serials.subList(0, nftCount / 2);
        List<byte[]> metadataListAfterUpdate = getMetadataList(testEnv.client, tokenId, nftSerialsUpdated);

        assertThat(metadataListAfterUpdate.toArray()).isEqualTo(updatedMetadataList.toArray());

        testEnv.close(tokenId);
    }

    @Test
    @DisplayName("Cannot update NFT metadata when transaction is not signed with metadata key")
    void cannotUpdateNFTMetadataWhenTransactionIsNotSignedWithMetadataKey() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var metadataKey = PrivateKey.generateED25519();
        var nftCount = 4;
        var initialMetadataList = NftMetadataGenerator.generate(new byte[]{4, 2, 0}, nftCount);
        var updatedMetadata = new byte[]{6, 9};

        // create a token with a metadata key and check it
        var tokenId = Objects.requireNonNull(
            new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .setSupplyKey(testEnv.operatorKey)
                .setMetadataKey(metadataKey)
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .tokenId
        );

        var tokenInfo = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(testEnv.client);

        assertThat(tokenInfo.metadataKey.toString()).isEqualTo(metadataKey.getPublicKey().toString());

        // mint tokens
        var tokenMintTransactionReceipt = new TokenMintTransaction()
            .setMetadata(initialMetadataList)
            .setTokenId(tokenId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        var nftSerials = tokenMintTransactionReceipt.serials;

        // assert this will fail
        var tokenUpdateNftsTransactionReceipt = new TokenUpdateNftsTransaction()
            .setTokenId(tokenId)
            .setSerials(nftSerials)
            .setMetadata(updatedMetadata)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        testEnv.close(tokenId);
    }

    @Test
    @DisplayName("Cannot update NFT metadata when metadata key is not set")
    void cannotUpdateNFTMetadataWhenMetadataKeyNotSet() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var metadataKey = PrivateKey.generateED25519();
        var nftCount = 4;
        var initialMetadataList = NftMetadataGenerator.generate(new byte[]{4, 2, 0}, nftCount);
        var updatedMetadata = new byte[]{6, 9};

        // create a token without a metadata key and check it
        var tokenId = Objects.requireNonNull(
            new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .setSupplyKey(testEnv.operatorKey)
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .tokenId
        );

        var tokenInfo = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(testEnv.client);

        assertThat(tokenInfo.metadataKey).isNull();

        // mint tokens
        var tokenMintTransactionReceipt = new TokenMintTransaction()
            .setMetadata(initialMetadataList)
            .setTokenId(tokenId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        var nftSerials = tokenMintTransactionReceipt.serials;

        // check NFTs' metadata can't be updated when a metadata key is not set
        var tokenUpdateNftsTransactionReceipt = new TokenUpdateNftsTransaction()
            .setTokenId(tokenId)
            .setSerials(nftSerials)
            .setMetadata(updatedMetadata)
            .sign(metadataKey)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        testEnv.close(tokenId);
    }

    @Test
    @DisplayName("Cannot update NFT metadata when metadata key was removed")
    void cannotUpdateNFTMetadataWhenMetadataWasRemoved() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var metadataKey = PrivateKey.generateED25519();
        var nftCount = 4;
        var initialMetadataList = NftMetadataGenerator.generate(new byte[]{4, 2, 0}, nftCount);
        var updatedMetadata = new byte[]{6, 9};

        // create a token with a metadata key and check it
        var tokenId = Objects.requireNonNull(
            new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .setSupplyKey(testEnv.operatorKey)
                .setMetadataKey(metadataKey)
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .tokenId
        );

        var tokenInfoBeforeUpdate = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(testEnv.client);

        assertThat(tokenInfoBeforeUpdate.metadataKey.toString()).isEqualTo(metadataKey.getPublicKey().toString());

        // update a token by removing a metadata key and check it
        new TokenUpdateTransaction()
            .setTokenId(tokenId)
            .setMetadataKey(null)
            .execute(testEnv.client);

        var tokenInfoAfterUpdate = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(testEnv.client);

        assertThat(tokenInfoAfterUpdate.metadataKey).isNull();

        // mint tokens
        var tokenMintTransactionReceipt = new TokenMintTransaction()
            .setMetadata(initialMetadataList)
            .setTokenId(tokenId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        var nftSerials = tokenMintTransactionReceipt.serials;

        // check NFTs' metadata can't be updated when a metadata key is not set
        var tokenUpdateNftsTransactionReceipt = new TokenUpdateNftsTransaction()
            .setTokenId(tokenId)
            .setSerials(nftSerials)
            .setMetadata(updatedMetadata)
            .sign(metadataKey)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        testEnv.close(tokenId);
    }

    /**
     * Retrieves the metadata information for a given list of NFT serials associated with a token.
     *
     * @param client The Hedera client used for executing the query.
     * @param tokenId The ID of the token.
     * @param nftSerials The list of serial numbers of the NFTs.
     * @return A list of byte arrays representing the metadata information for the NFTs.
     */
    private List<byte[]> getMetadataList(Client client, TokenId tokenId, List<Long> nftSerials) {
        return nftSerials.stream()
            .map(serial -> new NftId(tokenId, serial))
            .flatMap(nftId -> {
                try {
                    return new TokenNftInfoQuery()
                        .setNftId(nftId)
                        .execute(client).stream();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            })
            .map(tokenNftInfo -> tokenNftInfo.metadata)
            .toList();
    }
}
