import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class TokenTransferIntegrationTest {
    @Test
    @DisplayName("Can transfer tokens")
    void tokenTransferTest() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            testEnv.newAccountKey = PrivateKey.generate();

            TransactionResponse response = new AccountCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setKey(testEnv.newAccountKey)
                .setInitialBalance(new Hbar(1))
                .execute(testEnv.client);

            testEnv.newAccountId = response.getReceipt(testEnv.client).accountId;
            assertNotNull(testEnv.newAccountId);

            response = new TokenCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
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

            testEnv.newTokenId = response.getReceipt(testEnv.client).tokenId;
            assertNotNull(testEnv.newTokenId);

            new TokenAssociateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setAccountId(testEnv.newAccountId)
                .setTokenIds(Collections.singletonList(testEnv.newTokenId))
                .freezeWith(testEnv.client)
                .signWithOperator(testEnv.client)
                .sign(testEnv.newAccountKey)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TokenGrantKycTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setAccountId(testEnv.newAccountId)
                .setTokenId(testEnv.newTokenId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TransferTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .addTokenTransfer(testEnv.newTokenId, testEnv.operatorId, -10)
                .addTokenTransfer(testEnv.newTokenId, testEnv.newAccountId, 10)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.cleanUpAndClose();
        });
    }

    @Test
    @DisplayName("Cannot transfer tokens if balance is insufficient to pay fee")
    void insufficientBalanceForFee() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            PrivateKey key1 = PrivateKey.generate();
            PrivateKey key2 = PrivateKey.generate();
            var accountId1 = new AccountCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setKey(key1)
                .setInitialBalance(new Hbar(20))
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .accountId;
            var accountId2 = new AccountCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setKey(key2)
                .setInitialBalance(new Hbar(20))
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .accountId;

            var tokenId = new TokenCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setInitialSupply(1)
                .setCustomFees(Collections.singletonList(new CustomFixedFee()
                    .setAmount(5000_000_000L)
                    .setFeeCollectorAccountId(testEnv.operatorId)))
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .setFeeScheduleKey(testEnv.operatorKey)
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .tokenId;

            new TokenAssociateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setAccountId(accountId1)
                .setTokenIds(Collections.singletonList(tokenId))
                .freezeWith(testEnv.client)
                .sign(key1)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TokenAssociateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setAccountId(accountId2)
                .setTokenIds(Collections.singletonList(tokenId))
                .freezeWith(testEnv.client)
                .sign(key2)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TransferTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .addTokenTransfer(tokenId, testEnv.operatorId, -1)
                .addTokenTransfer(tokenId, accountId1, 1)
                .freezeWith(testEnv.client)
                .sign(key1)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var error = assertThrows(ReceiptStatusException.class, () -> {
                new TransferTransaction()
                    .setNodeAccountIds(testEnv.nodeAccountIds)
                    .addTokenTransfer(tokenId, accountId1, -1)
                    .addTokenTransfer(tokenId, accountId2, 1)
                    .freezeWith(testEnv.client)
                    .sign(key1)
                    .sign(key2)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.INSUFFICIENT_PAYER_BALANCE_FOR_CUSTOM_FEE.toString()));

            testEnv.client.close();
        });
    }
}
