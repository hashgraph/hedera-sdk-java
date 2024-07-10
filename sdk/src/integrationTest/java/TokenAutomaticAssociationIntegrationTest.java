import com.hedera.hashgraph.sdk.AccountAllowanceApproveTransaction;
import com.hedera.hashgraph.sdk.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.AccountInfoQuery;
import com.hedera.hashgraph.sdk.AccountUpdateTransaction;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TokenDeleteTransaction;
import com.hedera.hashgraph.sdk.TokenMintTransaction;
import com.hedera.hashgraph.sdk.TokenWipeTransaction;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.TransferTransaction;
import java.util.ArrayList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class TokenAutomaticAssociationIntegrationTest {

    @Test
    @DisplayName("Limited max auto association for Fungible Tokens")
    void limitedMaxAutoAssociationsFungibleTokens() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();
        var tokenId1 = EntityCreator.createFungibleToken(testEnv, 0);
        var tokenId2 = EntityCreator.createFungibleToken(testEnv, 0);
        var accountKey = PrivateKey.generateED25519();
        var accountMaxAutomaticTokenAssociations = 1;
        var receiverAccountId = EntityCreator.createAccount(testEnv, accountKey, accountMaxAutomaticTokenAssociations);

        var accountInfoBeforeTokenAssociation = new AccountInfoQuery()
            .setAccountId(receiverAccountId)
            .execute(testEnv.client);
        assertThat(accountInfoBeforeTokenAssociation.maxAutomaticTokenAssociations).isEqualTo(1);
        assertThat(accountInfoBeforeTokenAssociation.tokenRelationships.size()).isEqualTo(0);


        var transferRecord = new TransferTransaction()
            .addTokenTransfer(tokenId1, testEnv.operatorId, -1)
            .addTokenTransfer(tokenId1, receiverAccountId, 1)
            .execute(testEnv.client)
            .getRecord(testEnv.client);
        assertThat(transferRecord.automaticTokenAssociations.size()).isEqualTo(1);
        assertThat(transferRecord.automaticTokenAssociations.get(0).accountId).isEqualTo(receiverAccountId);
        assertThat(transferRecord.automaticTokenAssociations.get(0).tokenId).isEqualTo(tokenId1);

        var accountInfoAfterTokenAssociation = new AccountInfoQuery()
            .setAccountId(receiverAccountId)
            .execute(testEnv.client);
        assertThat(accountInfoAfterTokenAssociation.tokenRelationships.size()).isEqualTo(1);
        assertThat(accountInfoAfterTokenAssociation.tokenRelationships.get(tokenId1).automaticAssociation).isTrue();

        assertThatExceptionOfType(Exception.class).isThrownBy(() -> {
            new TransferTransaction()
                .addTokenTransfer(tokenId2, testEnv.operatorId, -1)
                .addTokenTransfer(tokenId2, receiverAccountId, 1)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        }).withMessageContaining("NO_REMAINING_AUTOMATIC_ASSOCIATIONS");

        new AccountUpdateTransaction()
            .setAccountId(receiverAccountId)
            .setMaxAutomaticTokenAssociations(2)
            .freezeWith(testEnv.client)
            .sign(accountKey)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        var accountInfoAfterMaxAssocUpdate = new AccountInfoQuery()
            .setAccountId(receiverAccountId)
            .execute(testEnv.client);
        assertThat(accountInfoAfterMaxAssocUpdate.maxAutomaticTokenAssociations).isEqualTo(2);

        new TokenDeleteTransaction()
            .setTokenId(tokenId1)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new TokenDeleteTransaction()
            .setTokenId(tokenId2)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        testEnv.close(receiverAccountId, accountKey);
    }

    @Test
    @DisplayName("Limited max auto association for Non Fungible Tokens")
    void limitedMaxAutoAssociationsNonFungibleTokens() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();
        var tokenId1 = EntityCreator.createNft(testEnv);
        var tokenId2 = EntityCreator.createNft(testEnv);
        var accountKey = PrivateKey.generateED25519();
        var accountMaxAutomaticTokenAssociations = 1;
        var receiverAccountId = EntityCreator.createAccount(testEnv, accountKey, accountMaxAutomaticTokenAssociations);

        var mintReceiptToken1 = new TokenMintTransaction()
            .setTokenId(tokenId1)
            .setMetadata(NftMetadataGenerator.generate((byte) 10))
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        var mintReceiptToken2 = new TokenMintTransaction()
            .setTokenId(tokenId2)
            .setMetadata(NftMetadataGenerator.generate((byte) 10))
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        var accountInfoBeforeTokenAssociation = new AccountInfoQuery()
            .setAccountId(receiverAccountId)
            .execute(testEnv.client);
        assertThat(accountInfoBeforeTokenAssociation.maxAutomaticTokenAssociations).isEqualTo(1);
        assertThat(accountInfoBeforeTokenAssociation.tokenRelationships.size()).isEqualTo(0);


        var serialsToTransfer = new ArrayList<>(mintReceiptToken2.serials);
        var nftTransferTransaction = new TransferTransaction();
        for (var serial : serialsToTransfer) {
            nftTransferTransaction.addNftTransfer(tokenId1.nft(serial), testEnv.operatorId, receiverAccountId);
        }
        var transferRecord = nftTransferTransaction
            .execute(testEnv.client)
            .getRecord(testEnv.client);

        assertThat(transferRecord.automaticTokenAssociations.size()).isEqualTo(1);
        assertThat(transferRecord.automaticTokenAssociations.get(0).accountId).isEqualTo(receiverAccountId);
        assertThat(transferRecord.automaticTokenAssociations.get(0).tokenId).isEqualTo(tokenId1);

        var accountInfoAfterTokenAssociation = new AccountInfoQuery()
            .setAccountId(receiverAccountId)
            .execute(testEnv.client);
        assertThat(accountInfoAfterTokenAssociation.tokenRelationships.size()).isEqualTo(1);
        assertThat(accountInfoAfterTokenAssociation.tokenRelationships.get(tokenId1).automaticAssociation).isTrue();

        assertThatExceptionOfType(Exception.class).isThrownBy(() -> {
            var serial = mintReceiptToken2.serials.get(0);
            new TransferTransaction()
                .addNftTransfer(tokenId2.nft(serial), testEnv.operatorId, receiverAccountId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        }).withMessageContaining("NO_REMAINING_AUTOMATIC_ASSOCIATIONS");

        new AccountUpdateTransaction()
            .setAccountId(receiverAccountId)
            .setMaxAutomaticTokenAssociations(2)
            .freezeWith(testEnv.client)
            .sign(accountKey)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        var accountInfoAfterMaxAssocUpdate = new AccountInfoQuery()
            .setAccountId(receiverAccountId)
            .execute(testEnv.client);
        assertThat(accountInfoAfterMaxAssocUpdate.maxAutomaticTokenAssociations).isEqualTo(2);

        new TokenDeleteTransaction()
            .setTokenId(tokenId1)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new TokenDeleteTransaction()
            .setTokenId(tokenId2)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        testEnv.close(receiverAccountId, accountKey);
    }

    /**
     * @notice E2E-HIP-904
     * @url https://hips.hedera.com/hip/hip-904
     */
    @Test
    @DisplayName("Can set unlimited max auto associations for Account")
    void canSetUnlimitedMaxAutoAssociationsForAccount() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();
        var accountKey = PrivateKey.generateED25519();
        var accountMaxAutomaticTokenAssociations = -1;
        var accountId = EntityCreator.createAccount(testEnv, accountKey, accountMaxAutomaticTokenAssociations);

        new AccountUpdateTransaction()
            .setAccountId(accountId)
            .setMaxAutomaticTokenAssociations(accountMaxAutomaticTokenAssociations)
            .freezeWith(testEnv.client)
            .sign(accountKey)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        var accountInfoBeforeTokenAssociation = new AccountInfoQuery()
            .setAccountId(accountId)
            .execute(testEnv.client);
        assertThat(accountInfoBeforeTokenAssociation.maxAutomaticTokenAssociations).isEqualTo(-1);
    }

    /**
     * @notice E2E-HIP-904
     * @url https://hips.hedera.com/hip/hip-904
     */
    @Test
    @DisplayName("Unlimited Max Auto Associations allow to transfer Fungible Tokens")
    void unlimitedMaxAutoAssociationsAllowToTransferFungibleTokens() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();
        var tokenId1 = EntityCreator.createFungibleToken(testEnv, 3);
        var tokenId2 = EntityCreator.createFungibleToken(testEnv, 3);
        var accountKey = PrivateKey.generateED25519();
        var accountId1 = EntityCreator.createAccount(testEnv, accountKey, -1);
        var accountId2 = EntityCreator.createAccount(testEnv, accountKey, 100);

        new AccountUpdateTransaction()
            .setAccountId(accountId2)
            .setMaxAutomaticTokenAssociations(-1)
            .freezeWith(testEnv.client)
            .sign(accountKey)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        // transfer to both receivers some token1 tokens
        new TransferTransaction()
            .addTokenTransfer(tokenId1, testEnv.operatorId, -1000)
            .addTokenTransfer(tokenId1, accountId1, 1000)
            .addTokenTransfer(tokenId1, testEnv.operatorId, -1000)
            .addTokenTransfer(tokenId1, accountId2, 1000)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        // transfer to both receivers some token2 tokens
        new TransferTransaction()
            .addTokenTransfer(tokenId2, testEnv.operatorId, -1000)
            .addTokenTransfer(tokenId2, accountId1, 1000)
            .addTokenTransfer(tokenId2, testEnv.operatorId, -1000)
            .addTokenTransfer(tokenId2, accountId2, 1000)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        // verify the balance of the receivers is 1000
        var accountId1Balance = new AccountBalanceQuery()
            .setAccountId(accountId1)
            .execute(testEnv.client);

        assertThat(accountId1Balance.tokens.get(tokenId1)).isEqualTo(1000);
        assertThat(accountId1Balance.tokens.get(tokenId2)).isEqualTo(1000);

        var accountId2Balance = new AccountBalanceQuery()
            .setAccountId(accountId2)
            .execute(testEnv.client);

        assertThat(accountId2Balance.tokens.get(tokenId1)).isEqualTo(1000);
        assertThat(accountId2Balance.tokens.get(tokenId2)).isEqualTo(1000);
    }

    @Test
    @DisplayName("Unlimited Max Auto Associations allow to transfer Fungible Tokens With Decimals")
    void unlimitedMaxAutoAssociationsAllowToTransferFungibleTokensWithDecimals() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();
        var tokenDecimals = 10;
        var tokenId1 = EntityCreator.createFungibleToken(testEnv, tokenDecimals);
        var tokenId2 = EntityCreator.createFungibleToken(testEnv, tokenDecimals);
        var accountKey = PrivateKey.generateED25519();
        var receiverAccountId = EntityCreator.createAccount(testEnv, accountKey, -1);

        new TransferTransaction()
            .addTokenTransferWithDecimals(tokenId1, testEnv.operatorId, -1000, tokenDecimals)
            .addTokenTransferWithDecimals(tokenId1, receiverAccountId, 1000, tokenDecimals)
            .addTokenTransferWithDecimals(tokenId2, testEnv.operatorId, -1000, tokenDecimals)
            .addTokenTransferWithDecimals(tokenId2, receiverAccountId, 1000, tokenDecimals)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        var receiverAccountBalance = new AccountBalanceQuery()
            .setAccountId(receiverAccountId)
            .execute(testEnv.client);

        assertThat(receiverAccountBalance.tokens.get(tokenId1)).isEqualTo(1000);
        assertThat(receiverAccountBalance.tokens.get(tokenId2)).isEqualTo(1000);
    }

    // Fix this one (test approach in Go)
    @Test
    @DisplayName("Unlimited Max Auto Associations allow to transfer from Fungible Tokens")
    void unlimitedMaxAutoAssociationsAllowToTransferFromFungibleTokens() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();
        var tokenId1 = EntityCreator.createFungibleToken(testEnv, 3);
        var tokenId2 = EntityCreator.createFungibleToken(testEnv, 3);
        var accountKey = PrivateKey.generateED25519();
        var accountId = EntityCreator.createAccount(testEnv, accountKey, -1);
        var spenderAccountKey = PrivateKey.generateED25519();
        var spenderAccountId = EntityCreator.createAccount(testEnv, spenderAccountKey, -1);

        new AccountAllowanceApproveTransaction()
            .approveTokenAllowance(tokenId1, testEnv.operatorId, spenderAccountId, 2000)
            .approveTokenAllowance(tokenId2, testEnv.operatorId, spenderAccountId, 2000)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new TransferTransaction()
            .addApprovedTokenTransfer(tokenId1, testEnv.operatorId, -1000)
            .addTokenTransfer(tokenId1, accountId, 1000)
            .addApprovedTokenTransfer(tokenId2, testEnv.operatorId, -1000)
            .addTokenTransfer(tokenId2, accountId, 1000)
            .setTransactionId(TransactionId.generate(spenderAccountId))
            .freezeWith(testEnv.client)
            .sign(spenderAccountKey)
            .execute(testEnv.client);

        var accountBalance = new AccountBalanceQuery()
            .setAccountId(accountId)
            .execute(testEnv.client);

        assertThat(accountBalance.tokens.get(tokenId1)).isEqualTo(1000);
        assertThat(accountBalance.tokens.get(tokenId2)).isEqualTo(1000);
    }
}
