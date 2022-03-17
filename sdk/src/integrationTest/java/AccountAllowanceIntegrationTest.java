import com.hedera.hashgraph.sdk.AccountAllowanceApproveTransaction;
import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.TransferTransaction;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccountAllowanceIntegrationTest {
    @Test
    @Disabled
    @DisplayName("Can spend hbar allowance")
    void canSpendHbarAllowance() throws Throwable {
        var testEnv = new IntegrationTestEnv(1);

        var aliceKey = PrivateKey.generateED25519();
        var aliceId = new AccountCreateTransaction()
            .setKey(aliceKey)
            .setInitialBalance(new Hbar(10))
            .execute(testEnv.client)
            .getReceipt(testEnv.client)
            .accountId;

        var bobKey = PrivateKey.generateED25519();
        var bobId = new AccountCreateTransaction()
            .setKey(bobKey)
            .setInitialBalance(new Hbar(10))
            .execute(testEnv.client)
            .getReceipt(testEnv.client)
            .accountId;

        Objects.requireNonNull(aliceId);
        Objects.requireNonNull(bobId);

        var allowanceTx = new AccountAllowanceApproveTransaction()
            .approveHbarAllowance(bobId, aliceId, new Hbar(10))
            .freezeWith(testEnv.client)
            .sign(bobKey);
        allowanceTx
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        var transferTx = new TransferTransaction()
            .addHbarTransfer(testEnv.operatorId, new Hbar(5))
            .addHbarTransfer(bobId, new Hbar(5).negated())
            .setHbarTransferApproval(bobId, true)
            .setTransactionId(TransactionId.generate(aliceId))
            .freezeWith(testEnv.client)
            .sign(aliceKey);
        var transferRecord = transferTx
            .execute(testEnv.client)
            .getRecord(testEnv.client);

        var transferFound = false;
        for (var transfer : transferRecord.transfers) {
            if (transfer.accountId.equals(testEnv.operatorId) && transfer.amount.equals(new Hbar(5))) {
                transferFound = true;
                break;
            }
        }
        assertTrue(transferFound);

        new AccountDeleteTransaction()
            .setAccountId(bobId)
            .setTransferAccountId(testEnv.operatorId)
            .freezeWith(testEnv.client)
            .sign(bobKey)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        testEnv.close(aliceId, aliceKey);
    }
}
