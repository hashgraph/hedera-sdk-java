import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountInfoQuery;
import com.hedera.hashgraph.sdk.AccountUpdateTransaction;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenDeleteTransaction;
import com.hedera.hashgraph.sdk.TransferTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class AutomaticAssociationTest {
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

        assertThat(accountInfo1.maxAutomaticTokenAssociations).isEqualTo(1);
        assertThat(accountInfo1.tokenRelationships.size()).isEqualTo(0);

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

        assertThat(transferRecord.automaticTokenAssociations.size()).isEqualTo(1);
        assertThat(transferRecord.automaticTokenAssociations.get(0).accountId).isEqualTo(accountId);
        assertThat(transferRecord.automaticTokenAssociations.get(0).tokenId).isEqualTo(tokenId1);

        var accountInfo2 = new AccountInfoQuery().setAccountId(accountId).execute(testEnv.client);

        assertThat(accountInfo2.tokenRelationships.size()).isEqualTo(1);
        assertThat(accountInfo2.tokenRelationships.get(tokenId1).automaticAssociation).isTrue();

        assertThatExceptionOfType(Exception.class).isThrownBy(() -> {
            new TransferTransaction()
                .addTokenTransfer(tokenId2, testEnv.operatorId, -1)
                .addTokenTransfer(tokenId2, accountId, 1)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        }).withMessageContaining("NO_REMAINING_AUTOMATIC_ASSOCIATIONS");

        new AccountUpdateTransaction()
            .setAccountId(accountId)
            .setMaxAutomaticTokenAssociations(2)
            .freezeWith(testEnv.client)
            .sign(key)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        var accountInfo3 = new AccountInfoQuery().setAccountId(accountId).execute(testEnv.client);

        assertThat(accountInfo3.maxAutomaticTokenAssociations).isEqualTo(2);

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
