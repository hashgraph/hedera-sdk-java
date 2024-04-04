import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenMintTransaction;
import com.hedera.hashgraph.sdk.TokenSupplyType;
import com.hedera.hashgraph.sdk.TokenType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class TokenMintIntegrationTest {
    @Test
    @DisplayName("Can mint tokens")
    void canMintTokens() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var tokenId = Objects.requireNonNull(
            new TokenCreateTransaction()
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
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .tokenId
        );

        var receipt = new TokenMintTransaction()
            .setAmount(10)
            .setTokenId(tokenId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        assertThat(receipt.totalSupply).isEqualTo(1000000 + 10);

        testEnv.close(tokenId);
    }


    @Test
    @DisplayName("Cannot mint more tokens than max supply")
    void cannotMintMoreThanMaxSupply() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var tokenId = Objects.requireNonNull(
            new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setSupplyType(TokenSupplyType.FINITE)
                .setMaxSupply(5)
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .setSupplyKey(testEnv.operatorKey)
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .tokenId
        );


        assertThatExceptionOfType(ReceiptStatusException.class).isThrownBy(() -> {
            new TokenMintTransaction()
                .setTokenId(tokenId)
                .setAmount(6)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        }).withMessageContaining(Status.TOKEN_MAX_SUPPLY_REACHED.toString());

        testEnv.close(tokenId);
    }

    @Test
    @DisplayName("Cannot mint tokens when token ID is not set")
    void cannotMintTokensWhenTokenIDIsNotSet() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        assertThatExceptionOfType(PrecheckStatusException.class).isThrownBy(() -> {
            new TokenMintTransaction()
                .setAmount(10)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        }).withMessageContaining(Status.INVALID_TOKEN_ID.toString());

        testEnv.close();
    }

    @Test
    @DisplayName("Can mint tokens when amount is not set")
    void canMintTokensWhenAmountIsNotSet() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var tokenId = Objects.requireNonNull(
            new TokenCreateTransaction()
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
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .tokenId
        );

        var receipt = new TokenMintTransaction()
            .setTokenId(tokenId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        assertThat(receipt.status).isEqualTo(Status.SUCCESS);

        testEnv.close(tokenId);
    }

    @Test
    @DisplayName("Cannot mint tokens when supply key does not sign transaction")
    void cannotMintTokensWhenSupplyKeyDoesNotSignTransaction() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var key = PrivateKey.generateED25519();

        var response = new AccountCreateTransaction()
            .setKey(key)
            .setInitialBalance(new Hbar(1))
            .execute(testEnv.client);

        var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

        var tokenId = Objects.requireNonNull(
            new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setDecimals(3)
                .setInitialSupply(1000000)
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .setFreezeKey(testEnv.operatorKey)
                .setWipeKey(testEnv.operatorKey)
                .setKycKey(testEnv.operatorKey)
                .setSupplyKey(key)
                .setFreezeDefault(false)
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .tokenId
        );

        assertThatExceptionOfType(ReceiptStatusException.class).isThrownBy(() -> {
            new TokenMintTransaction()
                .setTokenId(tokenId)
                .setAmount(10)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        }).withMessageContaining(Status.INVALID_SIGNATURE.toString());

        testEnv.close(tokenId, accountId, key);
    }


    @Test
    @DisplayName("Can mint NFTs")
    void canMintNfts() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var tokenId = Objects.requireNonNull(
            new TokenCreateTransaction()
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
                .getReceipt(testEnv.client)
                .tokenId
        );

        var receipt = new TokenMintTransaction()
            .setMetadata(NftMetadataGenerator.generate((byte) 10))
            .setTokenId(tokenId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        assertThat(receipt.serials.size()).isEqualTo(10);

        testEnv.close(tokenId);
    }


    @Test
    @DisplayName("Cannot mint NFTs if metadata too big")
    void cannotMintNftsIfMetadataTooBig() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var tokenId = Objects.requireNonNull(
            new TokenCreateTransaction()
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
                .getReceipt(testEnv.client)
                .tokenId
        );

        assertThatExceptionOfType(ReceiptStatusException.class).isThrownBy(() -> {
            new TokenMintTransaction()
                .setMetadata(NftMetadataGenerator.generateOneLarge())
                .setTokenId(tokenId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        }).withMessageContaining(Status.METADATA_TOO_LONG.toString());

        testEnv.close(tokenId);
    }
}
