import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.CustomFixedFee;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TokenAssociateTransaction;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenDeleteTransaction;
import com.hedera.hashgraph.sdk.TokenGrantKycTransaction;
import com.hedera.hashgraph.sdk.TransactionResponse;
import com.hedera.hashgraph.sdk.TransferTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenTransferIntegrationTest {
    @Test
    @DisplayName("Can transfer tokens")
    void tokenTransferTest() throws Exception {
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

        var tokenId = response.getReceipt(testEnv.client).tokenId;
        assertNotNull(tokenId);

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

        new TransferTransaction()
            .addTokenTransfer(tokenId, testEnv.operatorId, -10)
            .addTokenTransfer(tokenId, accountId, 10)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        testEnv.close(tokenId, accountId, key);
    }

    @Test
    @DisplayName("Cannot transfer tokens if balance is insufficient to pay fee")
    void insufficientBalanceForFee() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        PrivateKey key1 = PrivateKey.generate();
        PrivateKey key2 = PrivateKey.generate();
        var accountId1 = new AccountCreateTransaction()
            .setKey(key1)
            .setInitialBalance(new Hbar(2))
            .execute(testEnv.client)
            .getReceipt(testEnv.client)
            .accountId;
        var accountId2 = new AccountCreateTransaction()
            .setKey(key2)
            .setInitialBalance(new Hbar(2))
            .execute(testEnv.client)
            .getReceipt(testEnv.client)
            .accountId;

        var tokenId = new TokenCreateTransaction()
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
            .setAccountId(accountId1)
            .setTokenIds(Collections.singletonList(tokenId))
            .freezeWith(testEnv.client)
            .sign(key1)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new TokenAssociateTransaction()
            .setAccountId(accountId2)
            .setTokenIds(Collections.singletonList(tokenId))
            .freezeWith(testEnv.client)
            .sign(key2)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new TransferTransaction()
            .addTokenTransfer(tokenId, testEnv.operatorId, -1)
            .addTokenTransfer(tokenId, accountId1, 1)
            .freezeWith(testEnv.client)
            .sign(key1)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        var error = assertThrows(ReceiptStatusException.class, () -> {
            new TransferTransaction()
                .addTokenTransfer(tokenId, accountId1, -1)
                .addTokenTransfer(tokenId, accountId2, 1)
                .freezeWith(testEnv.client)
                .sign(key1)
                .sign(key2)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        });

        assertTrue(
            error.getMessage().contains(Status.INSUFFICIENT_SENDER_ACCOUNT_BALANCE_FOR_CUSTOM_FEE.toString()) ||
                error.getMessage().contains(Status.INSUFFICIENT_PAYER_BALANCE_FOR_CUSTOM_FEE.toString())
        );

        new TokenDeleteTransaction()
            .setTokenId(tokenId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new AccountDeleteTransaction()
            .setAccountId(accountId1)
            .setTransferAccountId(testEnv.operatorId)
            .freezeWith(testEnv.client)
            .sign(key1)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new AccountDeleteTransaction()
            .setAccountId(accountId2)
            .setTransferAccountId(testEnv.operatorId)
            .freezeWith(testEnv.client)
            .sign(key2)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        testEnv.close();
    }
}
