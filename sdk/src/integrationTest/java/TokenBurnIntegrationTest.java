import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TokenAssociateTransaction;
import com.hedera.hashgraph.sdk.TokenBurnTransaction;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenMintTransaction;
import com.hedera.hashgraph.sdk.TokenType;
import com.hedera.hashgraph.sdk.TransferTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class TokenBurnIntegrationTest {
    @Test
    @DisplayName("Can burn tokens")
    void canBurnTokens() throws Exception {
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
            .setFreezeDefault(false)
            .execute(testEnv.client);

        var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

        var receipt = new TokenBurnTransaction()
            .setAmount(10)
            .setTokenId(tokenId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        assertThat(receipt.totalSupply).isEqualTo(1000000 - 10);

        testEnv.close(tokenId);
    }

    @Test
    @DisplayName("Cannot burn tokens when token ID is not set")
    void cannotBurnTokensWhenTokenIDIsNotSet() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        assertThatExceptionOfType(PrecheckStatusException.class).isThrownBy(() -> {
            new TokenBurnTransaction()
                .setAmount(10)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        }).withMessageContaining(Status.INVALID_TOKEN_ID.toString());

        testEnv.close();
    }

    @Test
    @DisplayName("Cannot burn tokens when amount is not set")
    void cannotBurnTokensWhenAmountIsNotSet() throws Exception {
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
            .setFreezeDefault(false)
            .execute(testEnv.client);

        var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

        assertThatExceptionOfType(PrecheckStatusException.class).isThrownBy(() -> {
            new TokenBurnTransaction()
                .setTokenId(tokenId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        }).withMessageContaining(Status.INVALID_TOKEN_BURN_AMOUNT.toString());

        testEnv.close(tokenId);
    }

    @Test
    @DisplayName("Cannot burn tokens when supply key does not sign transaction")
    void cannotBurnTokensWhenSupplyKeyDoesNotSignTransaction() throws Exception {
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
            .setSupplyKey(PrivateKey.generate())
            .setFreezeDefault(false)
            .execute(testEnv.client);

        var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

        assertThatExceptionOfType(ReceiptStatusException.class).isThrownBy(() -> {
            new TokenBurnTransaction()
                .setTokenId(tokenId)
                .setAmount(10)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        }).withMessageContaining(Status.INVALID_SIGNATURE.toString());

        testEnv.close(tokenId);
    }


    @Test
    @DisplayName("Can burn NFTs")
    void canBurnNfts() throws Exception {
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

        var mintReceipt = new TokenMintTransaction()
            .setTokenId(tokenId)
            .setMetadata(NftMetadataGenerator.generate((byte) 10))
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new TokenBurnTransaction()
            .setSerials(mintReceipt.serials.subList(0, 4))
            .setTokenId(tokenId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        testEnv.close(tokenId);
    }


    @Test
    @DisplayName("Cannot burn NFTs when NFT is not owned by treasury")
    void cannotBurnNftsWhenNftIsNotOwned() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var createReceipt = new TokenCreateTransaction()
            .setTokenName("ffff")
            .setTokenSymbol("F")
            .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
            .setTreasuryAccountId(testEnv.operatorId)
            .setAdminKey(testEnv.operatorKey)
            .setFreezeKey(testEnv.operatorKey)
            .setWipeKey(testEnv.operatorKey)
            .setSupplyKey(testEnv.operatorKey)
            .setFreezeDefault(false)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        var tokenId = Objects.requireNonNull(createReceipt.tokenId);

        var serials = new TokenMintTransaction()
            .setTokenId(tokenId)
            .setMetadata(NftMetadataGenerator.generate((byte) 1))
            .execute(testEnv.client)
            .getReceipt(testEnv.client)
            .serials;

        var key = PrivateKey.generateED25519();

        var accountId = new AccountCreateTransaction()
            .setKey(key)
            .setInitialBalance(new Hbar(1))
            .execute(testEnv.client)
            .getReceipt(testEnv.client)
            .accountId;

        new TokenAssociateTransaction()
            .setAccountId(accountId)
            .setTokenIds(Collections.singletonList(tokenId))
            .freezeWith(testEnv.client)
            .signWithOperator(testEnv.client)
            .sign(key)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new TransferTransaction()
            .addNftTransfer(tokenId.nft(serials.get(0)), testEnv.operatorId, accountId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        assertThatExceptionOfType(ReceiptStatusException.class).isThrownBy(() -> {
            new TokenBurnTransaction()
                .setSerials(serials)
                .setTokenId(tokenId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        }).withMessageContaining(Status.TREASURY_MUST_OWN_BURNED_NFT.toString());

        testEnv.close(tokenId, accountId, key);
    }
}
