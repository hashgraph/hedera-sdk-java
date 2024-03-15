import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenInfoQuery;
import com.hedera.hashgraph.sdk.TokenType;
import com.hedera.hashgraph.sdk.TokenUpdateTransaction;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenUpdateIntegrationTest {

    @Test
    @DisplayName("Can update token")
    void canUpdateToken() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var response = new TokenCreateTransaction()
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
            .setPauseKey(testEnv.operatorKey)
            .setMetadataKey(testEnv.operatorKey)
            .setFreezeDefault(false)
            .execute(testEnv.client);

        var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

        @Var var info = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(testEnv.client);

        assertThat(info.tokenId).isEqualTo(tokenId);
        assertThat(info.name).isEqualTo("ffff");
        assertThat(info.symbol).isEqualTo("F");
        assertThat(info.decimals).isEqualTo(3);
        assertThat(info.treasuryAccountId).isEqualTo(testEnv.operatorId);
        assertThat(info.adminKey).isNotNull();
        assertThat(info.freezeKey).isNotNull();
        assertThat(info.wipeKey).isNotNull();
        assertThat(info.kycKey).isNotNull();
        assertThat(info.supplyKey).isNotNull();
        assertThat(info.adminKey.toString()).isEqualTo(testEnv.operatorKey.toString());
        assertThat(info.freezeKey.toString()).isEqualTo(testEnv.operatorKey.toString());
        assertThat(info.wipeKey.toString()).isEqualTo(testEnv.operatorKey.toString());
        assertThat(info.kycKey.toString()).isEqualTo(testEnv.operatorKey.toString());
        assertThat(info.supplyKey.toString()).isEqualTo(testEnv.operatorKey.toString());
        assertThat(info.pauseKey.toString()).isEqualTo(testEnv.operatorKey.toString());
        assertThat(info.metadataKey.toString()).isEqualTo(testEnv.operatorKey.toString());
        assertThat(info.defaultFreezeStatus).isNotNull().isFalse();
        assertThat(info.defaultKycStatus).isNotNull().isFalse();

        new TokenUpdateTransaction()
            .setTokenId(tokenId)
            .setTokenName("aaaa")
            .setTokenSymbol("A")
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        info = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(testEnv.client);

        assertThat(info.tokenId).isEqualTo(tokenId);
        assertThat(info.name).isEqualTo("aaaa");
        assertThat(info.symbol).isEqualTo("A");
        assertThat(info.decimals).isEqualTo(3);
        assertThat(info.treasuryAccountId).isEqualTo(testEnv.operatorId);
        assertThat(info.adminKey).isNotNull();
        assertThat(info.freezeKey).isNotNull();
        assertThat(info.wipeKey).isNotNull();
        assertThat(info.kycKey).isNotNull();
        assertThat(info.supplyKey).isNotNull();
        assertThat(info.adminKey.toString()).isEqualTo(testEnv.operatorKey.toString());
        assertThat(info.freezeKey.toString()).isEqualTo(testEnv.operatorKey.toString());
        assertThat(info.wipeKey.toString()).isEqualTo(testEnv.operatorKey.toString());
        assertThat(info.kycKey.toString()).isEqualTo(testEnv.operatorKey.toString());
        assertThat(info.supplyKey.toString()).isEqualTo(testEnv.operatorKey.toString());
        assertThat(info.pauseKey.toString()).isEqualTo(testEnv.operatorKey.toString());
        assertThat(info.metadataKey.toString()).isEqualTo(testEnv.operatorKey.toString());
        assertThat(info.defaultFreezeStatus).isNotNull();
        assertThat(info.defaultFreezeStatus).isFalse();
        assertThat(info.defaultKycStatus).isNotNull();
        assertThat(info.defaultKycStatus).isFalse();

        testEnv.close(tokenId);
    }

    @Test
    @DisplayName("Cannot update immutable token")
    void cannotUpdateImmutableToken() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var response = new TokenCreateTransaction()
            .setTokenName("ffff")
            .setTokenSymbol("F")
            .setTreasuryAccountId(testEnv.operatorId)
            .setFreezeDefault(false)
            .execute(testEnv.client);

        var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

        assertThatExceptionOfType(ReceiptStatusException.class).isThrownBy(() -> {
            new TokenUpdateTransaction()
                .setTokenId(tokenId)
                .setTokenName("aaaa")
                .setTokenSymbol("A")
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        }).withMessageContaining(Status.TOKEN_IS_IMMUTABLE.toString());

        testEnv.close();
    }

    /**
     * @notice E2E-HIP-646
     * @url https://hips.hedera.com/hip/hip-646
     */
    @Test
    @DisplayName("Can update a fungible token with metadata")
    void canUpdateFungibleTokenMetadata() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();
        var initialTokenMetadata = new byte[]{1, 1, 1, 1, 1};
        var updatedTokenMetadata = new byte[]{2, 2, 2, 2, 2};
        var emptyTokenMetadata = new byte[]{};
        var metadataKey = PrivateKey.generateED25519();

        // create a fungible token with metadata and metadata key
        var tokenId = Objects.requireNonNull(
            new TokenCreateTransaction()
            .setTokenName("ffff")
            .setTokenSymbol("F")
            .setTokenMetadata(initialTokenMetadata)
            .setTokenType(TokenType.FUNGIBLE_COMMON)
            .setDecimals(3)
            .setInitialSupply(1000000)
            .setAdminKey(testEnv.operatorKey)
            .setMetadataKey(metadataKey)
            .setFreezeDefault(false)
            .execute(testEnv.client)
            .getReceipt(testEnv.client)
            .tokenId
        );

        var tokenInfoAfterCreation = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(testEnv.client);

        assertThat(tokenInfoAfterCreation.metadata).isEqualTo(initialTokenMetadata);
        assertThat(tokenInfoAfterCreation.metadataKey.toString()).isEqualTo(metadataKey.getPublicKey().toString());

        // update token's metadata
        new TokenUpdateTransaction()
            .setTokenId(tokenId)
            .setTokenMetadata(updatedTokenMetadata)
            .sign(metadataKey)
            .execute(testEnv.client);

        var tokenInfoAfterMetadataUpdate = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(testEnv.client);

        assertThat(tokenInfoAfterMetadataUpdate.metadata).isEqualTo(updatedTokenMetadata);

        // update token, but don't update metadata
        new TokenUpdateTransaction()
            .setTokenId(tokenId)
            .setTokenMemo("abc")
            .execute(testEnv.client);

        var tokenInfoAfterMemoUpdate = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(testEnv.client);

        assertThat(tokenInfoAfterMemoUpdate.metadata).isEqualTo(updatedTokenMetadata);

        // update token with empty metadata
        new TokenUpdateTransaction()
            .setTokenId(tokenId)
            .setTokenMetadata(emptyTokenMetadata)
            .sign(metadataKey)
            .execute(testEnv.client);

        var tokenInfoAfterSettingEmptyMetadata = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(testEnv.client);

        assertThat(tokenInfoAfterSettingEmptyMetadata.metadata).isEqualTo(emptyTokenMetadata);

        testEnv.close(tokenId);
    }

    /**
     * @notice E2E-HIP-765
     * @url https://hips.hedera.com/hip/hip-765
     */
    @Test
    @DisplayName("Can update a non fungible token with metadata")
    void canUpdateNonFungibleTokenMetadata() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();
        var initialTokenMetadata = new byte[]{1, 1, 1, 1, 1};
        var updatedTokenMetadata = new byte[]{2, 2, 2, 2, 2};
        var emptyTokenMetadata = new byte[]{};
        var metadataKey = PrivateKey.generateED25519();

        // create a non fungible token with metadata and metadata key
        var tokenId = Objects.requireNonNull(
            new TokenCreateTransaction()
            .setTokenName("ffff")
            .setTokenSymbol("F")
            .setTokenMetadata(initialTokenMetadata)
            .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
            .setTreasuryAccountId(testEnv.operatorId)
            .setAdminKey(testEnv.operatorKey)
            .setMetadataKey(metadataKey)
            .setFreezeDefault(false)
            .execute(testEnv.client)
            .getReceipt(testEnv.client)
            .tokenId
        );

        var tokenInfoAfterCreation = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(testEnv.client);

        assertThat(tokenInfoAfterCreation.metadata).isEqualTo(initialTokenMetadata);
        assertThat(tokenInfoAfterCreation.metadataKey.toString()).isEqualTo(metadataKey.getPublicKey().toString());

        // update token's metadata
        new TokenUpdateTransaction()
            .setTokenId(tokenId)
            .setTokenMetadata(updatedTokenMetadata)
            .sign(metadataKey)
            .execute(testEnv.client);

        var tokenInfoAfterMetadataUpdate = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(testEnv.client);

        assertThat(tokenInfoAfterMetadataUpdate.metadata).isEqualTo(updatedTokenMetadata);

        // update token, but don't update metadata
        new TokenUpdateTransaction()
            .setTokenId(tokenId)
            .setTokenMemo("abc")
            .execute(testEnv.client);

        var tokenInfoAfterMemoUpdate = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(testEnv.client);

        assertThat(tokenInfoAfterMemoUpdate.metadata).isEqualTo(updatedTokenMetadata);

        // update token with empty metadata
        new TokenUpdateTransaction()
            .setTokenId(tokenId)
            .setTokenMetadata(emptyTokenMetadata)
            .sign(metadataKey)
            .execute(testEnv.client);

        var tokenInfoAfterSettingEmptyMetadata = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(testEnv.client);

        assertThat(tokenInfoAfterSettingEmptyMetadata.metadata).isEqualTo(emptyTokenMetadata);

        testEnv.close(tokenId);
    }

    /**
     * @notice E2E-HIP-646
     * @url https://hips.hedera.com/hip/hip-646
     */
    @Test
    @DisplayName("Cannot update a fungible token with metadata when transaction is not signed with metadata key")
    void cannotUpdateFungibleTokenMetadataWhenTransactionIsNotSignedWithMetadataKey() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();
        var initialTokenMetadata = new byte[]{1, 1, 1, 1, 1};
        var updatedTokenMetadata = new byte[]{2, 2, 2, 2, 2};
        var metadataKey = PrivateKey.generateED25519();

        // create a fungible token with metadata and metadata key
        var tokenId = Objects.requireNonNull(
            new TokenCreateTransaction()
            .setTokenName("ffff")
            .setTokenSymbol("F")
            .setTokenMetadata(initialTokenMetadata)
            .setTokenType(TokenType.FUNGIBLE_COMMON)
            .setDecimals(3)
            .setInitialSupply(1000000)
            .setAdminKey(testEnv.operatorKey)
            .setMetadataKey(metadataKey)
            .setFreezeDefault(false)
            .execute(testEnv.client)
            .getReceipt(testEnv.client)
            .tokenId
        );

        // update token's metadata -- assert this will fail
        new TokenUpdateTransaction()
            .setTokenId(tokenId)
            .setTokenMetadata(updatedTokenMetadata)
            .execute(testEnv.client);

        testEnv.close(tokenId);
    }

    /**
     * @notice E2E-HIP-765
     * @url https://hips.hedera.com/hip/hip-765
     */
    @Test
    @DisplayName("Cannot update a non fungible token with metadata when transaction is not signed with metadata key")
    void cannotUpdateNonFungibleTokenMetadataWhenTransactionIsNotSignedWithMetadataKey() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();
        var initialTokenMetadata = new byte[]{1, 1, 1, 1, 1};
        var updatedTokenMetadata = new byte[]{2, 2, 2, 2, 2};
        var metadataKey = PrivateKey.generateED25519();

        // create a non fungible token with metadata and metadata key
        var tokenId = Objects.requireNonNull(
            new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTokenMetadata(initialTokenMetadata)
                .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .setMetadataKey(metadataKey)
                .setFreezeDefault(false)
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .tokenId
        );

        // update token's metadata -- assert this will fail
        new TokenUpdateTransaction()
            .setTokenId(tokenId)
            .setTokenMetadata(updatedTokenMetadata)
            .execute(testEnv.client);

        testEnv.close(tokenId);
    }

    /**
     * @notice E2E-HIP-646
     * @url https://hips.hedera.com/hip/hip-646
     */
    @Test
    @DisplayName("Cannot update a fungible token with metadata when metadata key is not set")
    void cannotUpdateFungibleTokenMetadataWhenMetadataKeyNotSet() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();
        var initialTokenMetadata = new byte[]{1, 1, 1, 1, 1};
        var updatedTokenMetadata = new byte[]{2, 2, 2, 2, 2};
        var metadataKey = PrivateKey.generateED25519();

        // create a fungible token with metadata and without a metadata key
        var tokenId = Objects.requireNonNull(
            new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTokenMetadata(initialTokenMetadata)
                .setTokenType(TokenType.FUNGIBLE_COMMON)
                .setDecimals(3)
                .setInitialSupply(1000000)
                .setAdminKey(testEnv.operatorKey)
                .setFreezeDefault(false)
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .tokenId
        );

        // update token's metadata -- assert this will fail
        new TokenUpdateTransaction()
            .setTokenId(tokenId)
            .setTokenMetadata(updatedTokenMetadata)
            .sign(metadataKey)
            .execute(testEnv.client);

        testEnv.close(tokenId);
    }

    /**
     * @notice E2E-HIP-765
     * @url https://hips.hedera.com/hip/hip-765
     */
    @Test
    @DisplayName("Cannot update a non fungible token with metadata when metadata key is not set")
    void cannotUpdateNonFungibleTokenMetadataWhenMetadataKeyNotSet() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();
        var initialTokenMetadata = new byte[]{1, 1, 1, 1, 1};
        var updatedTokenMetadata = new byte[]{2, 2, 2, 2, 2};
        var metadataKey = PrivateKey.generateED25519();

        // create a non fungible token with metadata and without a metadata key
        var tokenId = Objects.requireNonNull(
            new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTokenMetadata(initialTokenMetadata)
                .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .setFreezeDefault(false)
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .tokenId
        );

        // update token's metadata -- assert this will fail
        new TokenUpdateTransaction()
            .setTokenId(tokenId)
            .setTokenMetadata(updatedTokenMetadata)
            .sign(metadataKey)
            .execute(testEnv.client);

        testEnv.close(tokenId);
    }

    /**
     * @notice E2E-HIP-646
     * @url https://hips.hedera.com/hip/hip-646
     */
    @Test
    @DisplayName("Cannot update fungible token metadata when metadata key was removed")
    void cannotUpdateFungibleTokenMetadataWhenMetadataKeyWasRemoved() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();
        var initialTokenMetadata = new byte[]{1, 1, 1, 1, 1};
        var updatedTokenMetadata = new byte[]{2, 2, 2, 2, 2};
        var metadataKey = PrivateKey.generateED25519();

        // create a fungible token with a metadata key and check it
        var tokenId = Objects.requireNonNull(
            new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTokenMetadata(initialTokenMetadata)
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

        // update token's metadata -- assert this will fail
        new TokenUpdateTransaction()
            .setTokenId(tokenId)
            .setTokenMetadata(updatedTokenMetadata)
            .sign(metadataKey)
            .execute(testEnv.client);

        testEnv.close(tokenId);
    }

    /**
     * @notice E2E-HIP-765
     * @url https://hips.hedera.com/hip/hip-765
     */
    @Test
    @DisplayName("Cannot update a non fungible token metadata when metadata key was removed")
    void cannotUpdateNonFungibleTokenMetadataWhenMetadataKeyWasRemoved() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();
        var initialTokenMetadata = new byte[]{1, 1, 1, 1, 1};
        var updatedTokenMetadata = new byte[]{2, 2, 2, 2, 2};
        var metadataKey = PrivateKey.generateED25519();

        // create a non fungible token with a metadata key and check it
        var tokenId = Objects.requireNonNull(
            new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTokenMetadata(initialTokenMetadata)
                .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .setFreezeDefault(false)
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

        // update token's metadata -- assert this will fail
        new TokenUpdateTransaction()
            .setTokenId(tokenId)
            .setTokenMetadata(updatedTokenMetadata)
            .sign(metadataKey)
            .execute(testEnv.client);

        testEnv.close(tokenId);
    }
}
