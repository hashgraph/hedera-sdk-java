import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TokenAssociateTransaction;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenGrantKycTransaction;
import com.hedera.hashgraph.sdk.TokenMintTransaction;
import com.hedera.hashgraph.sdk.TokenType;
import com.hedera.hashgraph.sdk.TokenWipeTransaction;
import com.hedera.hashgraph.sdk.TransferTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenWipeIntegrationTest {
    @Test
    @DisplayName("Can wipe accounts balance")
    void canWipeAccountsBalance() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var key = PrivateKey.generate();

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
                .setSupplyKey(testEnv.operatorKey)
                .setFreezeDefault(false)
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .tokenId
        );

        new TokenAssociateTransaction()
            .setAccountId(accountId)
            .setTokenIds(Collections.singletonList(tokenId))
            .freezeWith(testEnv.client)
            .sign(key)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new TokenGrantKycTransaction()
            .setAccountId(accountId)
            .setTokenId(tokenId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new TransferTransaction()
            .addTokenTransfer(tokenId, testEnv.operatorId, -10)
            .addTokenTransfer(tokenId, accountId, 10)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new TokenWipeTransaction()
            .setTokenId(tokenId)
            .setAccountId(accountId)
            .setAmount(10)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        testEnv.close(tokenId, accountId, key);
    }


    @Test
    @DisplayName("Can wipe accounts NFTs")
    void canWipeAccountsNfts() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var key = PrivateKey.generate();

        var response = new AccountCreateTransaction()
            .setKey(key)
            .setInitialBalance(new Hbar(1))
            .execute(testEnv.client);

        var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

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

        var mintReceipt = new TokenMintTransaction()
            .setTokenId(tokenId)
            .setMetadata(NftMetadataGenerator.generate((byte) 10))
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new TokenAssociateTransaction()
            .setAccountId(accountId)
            .setTokenIds(Collections.singletonList(tokenId))
            .freezeWith(testEnv.client)
            .sign(key)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new TokenGrantKycTransaction()
            .setAccountId(accountId)
            .setTokenId(tokenId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        var serialsToTransfer = mintReceipt.serials.subList(0, 4);
        var transfer = new TransferTransaction();
        for (var serial : serialsToTransfer) {
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
    }


    @Test
    @DisplayName("Cannot wipe accounts NFTs if the account doesn't own them")
    void cannotWipeAccountsNftsIfNotOwned() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var key = PrivateKey.generate();

        var response = new AccountCreateTransaction()
            .setKey(key)
            .setInitialBalance(new Hbar(1))
            .execute(testEnv.client);

        var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

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

        var mintReceipt = new TokenMintTransaction()
            .setTokenId(tokenId)
            .setMetadata(NftMetadataGenerator.generate((byte) 10))
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new TokenAssociateTransaction()
            .setAccountId(accountId)
            .setTokenIds(Collections.singletonList(tokenId))
            .freezeWith(testEnv.client)
            .sign(key)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new TokenGrantKycTransaction()
            .setAccountId(accountId)
            .setTokenId(tokenId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        var serialsToTransfer = mintReceipt.serials.subList(0, 4);
        // don't transfer them

        var error = assertThrows(ReceiptStatusException.class, () -> {
            new TokenWipeTransaction()
                .setTokenId(tokenId)
                .setAccountId(accountId)
                .setSerials(serialsToTransfer)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        });

        assertTrue(error.getMessage().contains(Status.ACCOUNT_DOES_NOT_OWN_WIPED_NFT.toString()));

        testEnv.close(tokenId, accountId, key);
    }

    @Test
    @DisplayName("Cannot wipe accounts balance when account ID is not set")
    void cannotWipeAccountsBalanceWhenAccountIDIsNotSet() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var key = PrivateKey.generate();

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
                .setSupplyKey(testEnv.operatorKey)
                .setFreezeDefault(false)
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .tokenId
        );

        new TokenAssociateTransaction()
            .setAccountId(accountId)
            .setTokenIds(Collections.singletonList(tokenId))
            .freezeWith(testEnv.client)
            .sign(key)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new TokenGrantKycTransaction()
            .setAccountId(accountId)
            .setTokenId(tokenId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new TransferTransaction()
            .addTokenTransfer(tokenId, testEnv.operatorId, -10)
            .addTokenTransfer(tokenId, accountId, 10)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        var error = assertThrows(PrecheckStatusException.class, () -> {
            new TokenWipeTransaction()
                .setTokenId(tokenId)
                .setAmount(10)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        });

        assertTrue(error.getMessage().contains(Status.INVALID_ACCOUNT_ID.toString()));

        testEnv.close(tokenId, accountId, key);
    }

    @Test
    @DisplayName("Cannot wipe accounts balance when token ID is not set")
    void cannotWipeAccountsBalanceWhenTokenIDIsNotSet() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var key = PrivateKey.generate();

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
                .setSupplyKey(testEnv.operatorKey)
                .setFreezeDefault(false)
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .tokenId
        );

        new TokenAssociateTransaction()
            .setAccountId(accountId)
            .setTokenIds(Collections.singletonList(tokenId))
            .freezeWith(testEnv.client)
            .sign(key)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new TokenGrantKycTransaction()
            .setAccountId(accountId)
            .setTokenId(tokenId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new TransferTransaction()
            .addTokenTransfer(tokenId, testEnv.operatorId, -10)
            .addTokenTransfer(tokenId, accountId, 10)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        var error = assertThrows(PrecheckStatusException.class, () -> {
            new TokenWipeTransaction()
                .setAccountId(accountId)
                .setAmount(10)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        });

        assertTrue(error.getMessage().contains(Status.INVALID_TOKEN_ID.toString()));

        testEnv.close(tokenId, accountId, key);
    }

    @Test
    @DisplayName("Cannot wipe accounts balance when amount is not set")
    void cannotWipeAccountsBalanceWhenAmountIsNotSet() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var key = PrivateKey.generate();

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
                .setSupplyKey(testEnv.operatorKey)
                .setFreezeDefault(false)
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .tokenId
        );

        new TokenAssociateTransaction()
            .setAccountId(accountId)
            .setTokenIds(Collections.singletonList(tokenId))
            .freezeWith(testEnv.client)
            .sign(key)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new TokenGrantKycTransaction()
            .setAccountId(accountId)
            .setTokenId(tokenId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new TransferTransaction()
            .addTokenTransfer(tokenId, testEnv.operatorId, -10)
            .addTokenTransfer(tokenId, accountId, 10)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        var error = assertThrows(PrecheckStatusException.class, () -> {
            new TokenWipeTransaction()
                .setTokenId(tokenId)
                .setAccountId(accountId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        });

        assertTrue(error.getMessage().contains(Status.INVALID_WIPING_AMOUNT.toString()));

        testEnv.close(tokenId, accountId, key);
    }
}
