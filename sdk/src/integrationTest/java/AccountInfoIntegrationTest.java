import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountInfoFlow;
import com.hedera.hashgraph.sdk.AccountInfoQuery;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.Transaction;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountInfoIntegrationTest {
    @Test
    @DisplayName("Can query account info for client operator")
    void canQueryAccountInfoForClientOperator() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var info = new AccountInfoQuery()
            .setAccountId(testEnv.operatorId)
            .execute(testEnv.client);

        assertEquals(testEnv.operatorId, info.accountId);
        assertFalse(info.isDeleted);
        assertEquals(testEnv.operatorKey.toString(), info.key.toString());
        assertTrue(info.balance.toTinybars() > 0);
        assertNull(info.proxyAccountId);
        assertEquals(Hbar.ZERO, info.proxyReceived);

        testEnv.close();
    }

    @Test
    @DisplayName("Can get cost for account info query")
    void getCostAccountInfoForClientOperator() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var info = new AccountInfoQuery()
            .setAccountId(testEnv.operatorId)
            .setMaxQueryPayment(new Hbar(1));

        var cost = info.getCost(testEnv.client);

        var accInfo = info.setQueryPayment(cost).execute(testEnv.client);

        assertEquals(testEnv.operatorId, accInfo.accountId);

        testEnv.close();
    }

    @Test
    @DisplayName("Can get cost for account info query, with a bix max")
    void getCostBigMaxAccountInfoForClientOperator() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var info = new AccountInfoQuery()
            .setAccountId(testEnv.operatorId)
            .setMaxQueryPayment(Hbar.MAX);

        var cost = info.getCost(testEnv.client);

        var accInfo = info.setQueryPayment(cost).execute(testEnv.client);

        assertEquals(testEnv.operatorId, accInfo.accountId);

        testEnv.close();
    }

    @Test
    @Disabled
    @DisplayName("Can get cost for account info query, with a small max")
    void getCostSmallMaxAccountInfoForClientOperator() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var info = new AccountInfoQuery()
            .setAccountId(testEnv.operatorId)
            .setMaxQueryPayment(Hbar.fromTinybars(1));

        var cost = info.getCost(testEnv.client);

        var error = assertThrows(RuntimeException.class, () -> {
            info.execute(testEnv.client);
        });

        assertEquals("com.hedera.hashgraph.sdk.MaxQueryPaymentExceededException: cost for AccountInfoQuery, of " + cost.toString() + ", without explicit payment is greater than the maximum allowed payment of 1 tℏ", error.getMessage());

        testEnv.close();
    }

    @Test
    @DisplayName("Insufficient tx fee error.")
    void getCostInsufficientTxFeeAccountInfoForClientOperator() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var info = new AccountInfoQuery()
            .setAccountId(testEnv.operatorId)
            .setMaxQueryPayment(Hbar.fromTinybars(10000));

        var error = assertThrows(PrecheckStatusException.class, () -> {
            info.setQueryPayment(Hbar.fromTinybars(1)).execute(testEnv.client);
        });

        assertEquals("INSUFFICIENT_TX_FEE", error.status.toString());

        testEnv.close();
    }

    @Test
    @DisplayName("AccountInfoFlow.verify functions")
    void accountInfoFlowVerifyFunctions() throws Throwable {
        var testEnv = new IntegrationTestEnv(1);

        var newKey = PrivateKey.generateED25519();
        var newPublicKey = newKey.getPublicKey();

        Transaction<?> signedTx = new AccountCreateTransaction()
            .setKey(newPublicKey)
            .setInitialBalance(Hbar.fromTinybars(1000))
            .freezeWith(testEnv.client)
            .signWithOperator(testEnv.client);

        Transaction<?> unsignedTx = new AccountCreateTransaction()
            .setKey(newPublicKey)
            .setInitialBalance(Hbar.fromTinybars(1000))
            .freezeWith(testEnv.client);

        assertTrue(AccountInfoFlow.verifyTransactionSignature(testEnv.client, testEnv.operatorId, signedTx));
        assertFalse(AccountInfoFlow.verifyTransactionSignature(testEnv.client, testEnv.operatorId, unsignedTx));

        testEnv.close();
    }
}
