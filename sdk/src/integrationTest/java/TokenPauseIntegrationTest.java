import static org.junit.jupiter.api.Assertions.assertThrows;

import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.TokenAssociateTransaction;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenPauseTransaction;
import com.hedera.hashgraph.sdk.TransferTransaction;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TokenPauseIntegrationTest {

    @Test
    @DisplayName("Can execute token pause transaction")
    void canExecuteTokenPauseTransaction() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();
        var accountKey = PrivateKey.generateED25519();
        var testTokenAmount = 10;
        var accountId = new AccountCreateTransaction().setKey(accountKey).setInitialBalance(new Hbar(2))
            .execute(testEnv.client).getReceipt(testEnv.client).accountId;

        var tokenId = new TokenCreateTransaction().setTokenName("ffff").setTokenSymbol("F").setInitialSupply(1000000)
            .setDecimals(3).setTreasuryAccountId(testEnv.operatorId).setAdminKey(testEnv.operatorKey)
            .setPauseKey(testEnv.operatorKey).setFreezeDefault(false).execute(testEnv.client)
            .getReceipt(testEnv.client).tokenId;

        new TokenAssociateTransaction().setAccountId(accountId).setTokenIds(Collections.singletonList(tokenId))
            .freezeWith(testEnv.client).sign(accountKey).execute(testEnv.client).getReceipt(testEnv.client);

        new TransferTransaction().addTokenTransfer(tokenId, accountId, testTokenAmount)
            .addTokenTransfer(tokenId, testEnv.operatorId, -testTokenAmount).execute(testEnv.client)
            .getReceipt(testEnv.client);

        new TokenPauseTransaction().setTokenId(tokenId).freezeWith(testEnv.client).execute(testEnv.client)
            .getReceipt(testEnv.client);

        assertThrows(ReceiptStatusException.class, () -> {
            new TransferTransaction().addTokenTransfer(tokenId, accountId, testTokenAmount)
                .addTokenTransfer(tokenId, testEnv.operatorId, -testTokenAmount).freezeWith(testEnv.client)
                .sign(accountKey).execute(testEnv.client).getReceipt(testEnv.client);
        });

        testEnv.close();
    }

    @Test
    @DisplayName("Cannot pause with no token ID")
    void cannotPauseWithNoTokenId() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        assertThrows(PrecheckStatusException.class, () -> {
            new TokenPauseTransaction().execute(testEnv.client).getReceipt(testEnv.client);
        });

        testEnv.close();
    }
}
