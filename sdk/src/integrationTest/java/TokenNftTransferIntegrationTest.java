import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TokenAssociateTransaction;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenGrantKycTransaction;
import com.hedera.hashgraph.sdk.TokenMintTransaction;
import com.hedera.hashgraph.sdk.TokenType;
import com.hedera.hashgraph.sdk.TokenWipeTransaction;
import com.hedera.hashgraph.sdk.TransactionResponse;
import com.hedera.hashgraph.sdk.TransferTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class TokenNftTransferIntegrationTest {
    @Test
    @DisplayName("Can transfer NFTs")
    void canTransferNfts() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var key = PrivateKey.generate();

        @Var TransactionResponse response = new AccountCreateTransaction()
            .setKey(key)
            .setInitialBalance(new Hbar(1))
            .execute(testEnv.client);

        var accountId = response.getReceipt(testEnv.client).accountId;
        assertNotNull(accountId);

        response = new TokenCreateTransaction()
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
            .execute(testEnv.client);

        var tokenId = response.getReceipt(testEnv.client).tokenId;
        assertNotNull(tokenId);

        var mintReceipt = new TokenMintTransaction()
            .setTokenId(tokenId)
            .setMetadata(NftMetadataGenerator.generate((byte) 10))
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new TokenAssociateTransaction()
            .setAccountId(accountId)
            .setTokenIds(Collections.singletonList(tokenId))
            .freezeWith(testEnv.client)
            .signWithOperator(testEnv.client)
            .sign(key)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new TokenGrantKycTransaction()
            .setAccountId(accountId)
            .setTokenId(tokenId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        var serialsToTransfer = new ArrayList<Long>(mintReceipt.serials.subList(0, 4));
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
    @DisplayName("Cannot transfer NFTs you don't own")
    void cannotTransferUnownedNfts() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var key = PrivateKey.generate();

        @Var TransactionResponse response = new AccountCreateTransaction()
            .setKey(key)
            .setInitialBalance(new Hbar(1))
            .execute(testEnv.client);

        var accountId = response.getReceipt(testEnv.client).accountId;
        assertNotNull(accountId);

        response = new TokenCreateTransaction()
            .setTokenName("ffff")
            .setTokenSymbol("F")
            .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
            .setTreasuryAccountId(testEnv.operatorId)
            .setAdminKey(testEnv.operatorKey)
            .setFreezeKey(testEnv.operatorKey)
            .setWipeKey(testEnv.operatorKey)
            .setSupplyKey(testEnv.operatorKey)
            .setFreezeDefault(false)
            .execute(testEnv.client);

        var tokenId = response.getReceipt(testEnv.client).tokenId;
        assertNotNull(tokenId);

        var mintReceipt = new TokenMintTransaction()
            .setTokenId(tokenId)
            .setMetadata(NftMetadataGenerator.generate((byte) 10))
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new TokenAssociateTransaction()
            .setAccountId(accountId)
            .setTokenIds(Collections.singletonList(tokenId))
            .freezeWith(testEnv.client)
            .signWithOperator(testEnv.client)
            .sign(key)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        var serialsToTransfer = new ArrayList<Long>(mintReceipt.serials.subList(0, 4));
        var transfer = new TransferTransaction();
        for (var serial : serialsToTransfer) {
            // Try to transfer in wrong direction
            transfer.addNftTransfer(tokenId.nft(serial), accountId, testEnv.operatorId);
        }
        transfer.freezeWith(testEnv.client).sign(key);

        var error = assertThrows(ReceiptStatusException.class, () -> {
            transfer.execute(testEnv.client).getReceipt(testEnv.client);
        });

        assertTrue(error.getMessage().contains(Status.SENDER_DOES_NOT_OWN_NFT_SERIAL_NO.toString()));

        testEnv.close(tokenId, accountId, key);
    }
}
