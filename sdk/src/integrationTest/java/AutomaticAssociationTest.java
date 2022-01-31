import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountInfoQuery;
import com.hedera.hashgraph.sdk.AccountUpdateTransaction;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenDeleteTransaction;
import com.hedera.hashgraph.sdk.TransferTransaction;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;

public class AutomaticAssociationTest {
    @Disabled
    @Test
    @DisplayName("Tokens automatically become associated")
    void autoAssociateTest() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var key = PrivateKey.generateED25519();
        var accountId = new AccountCreateTransaction()
            .setKey(key)
            .setInitialBalance(new Hbar(10))
            .setMaxAutomaticTokenAssociations(1)
            .execute(testEnv.client)
            .getReceipt(testEnv.client)
            .accountId;

        Objects.requireNonNull(accountId);

        var accountInfo1 = new AccountInfoQuery().setAccountId(accountId).execute(testEnv.client);

        assertEquals(1, accountInfo1.maxAutomaticTokenAssociations);
        assertEquals(0, accountInfo1.tokenRelationships.size());

        var tokenId1 = new TokenCreateTransaction()
            .setTreasuryAccountId(testEnv.operatorId)
            .setTokenName("Test Token")
            .setTokenSymbol("T")
            .setAdminKey(testEnv.operatorKey)
            .setInitialSupply(1)
            .execute(testEnv.client)
            .getReceipt(testEnv.client)
            .tokenId;

        var tokenId2 = new TokenCreateTransaction()
            .setTreasuryAccountId(testEnv.operatorId)
            .setTokenName("Test Token")
            .setTokenSymbol("T")
            .setAdminKey(testEnv.operatorKey)
            .setInitialSupply(1)
            .execute(testEnv.client)
            .getReceipt(testEnv.client)
            .tokenId;

        Objects.requireNonNull(tokenId1);
        Objects.requireNonNull(tokenId2);

        var transferResponse1 = new TransferTransaction()
            .addTokenTransfer(tokenId1, testEnv.operatorId, -1)
            .addTokenTransfer(tokenId1, accountId, 1)
            .execute(testEnv.client);

        transferResponse1.getReceipt(testEnv.client);
        var transferRecord = transferResponse1.getRecord(testEnv.client);

        assertEquals(1, transferRecord.automaticTokenAssociations.size());
        assertEquals(accountId, transferRecord.automaticTokenAssociations.get(0).accountId);
        assertEquals(tokenId1, transferRecord.automaticTokenAssociations.get(0).tokenId);

        var accountInfo2 = new AccountInfoQuery().setAccountId(accountId).execute(testEnv.client);

        assertEquals(1, accountInfo2.tokenRelationships.size());
        assertTrue(accountInfo2.tokenRelationships.get(tokenId1).automaticAssociation);

        var error = assertThrows(Exception.class, () -> {
            new TransferTransaction()
                .addTokenTransfer(tokenId2, testEnv.operatorId, -1)
                .addTokenTransfer(tokenId2, accountId, 1)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        });

        assertTrue(error.getMessage().contains("NO_REMAINING_AUTO_ASSOCIATIONS"));

        new AccountUpdateTransaction()
            .setAccountId(accountId)
            .setMaxAutomaticTokenAssociations(2)
            .freezeWith(testEnv.client)
            .sign(key)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        var accountInfo3 = new AccountInfoQuery().setAccountId(accountId).execute(testEnv.client);

        assertEquals(2, accountInfo3.maxAutomaticTokenAssociations);

        new TokenDeleteTransaction()
            .setTokenId(tokenId1)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new TokenDeleteTransaction()
            .setTokenId(tokenId2)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        testEnv.close(accountId, key);
    }
}
