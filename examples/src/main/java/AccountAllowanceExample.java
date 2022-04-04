;import com.hedera.hashgraph.sdk.AccountAllowanceAdjustTransaction;
import com.hedera.hashgraph.sdk.AccountAllowanceApproveTransaction;
import com.hedera.hashgraph.sdk.AccountAllowanceDeleteTransaction;
import com.hedera.hashgraph.sdk.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.AccountInfoQuery;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.TransferTransaction;
import io.github.cdimascio.dotenv.Dotenv;
import org.checkerframework.checker.units.qual.A;

import java.util.Objects;
import java.util.concurrent.TimeoutException;

public class AccountAllowanceExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private final Client client;

    private final PrivateKey aliceKey;
    private final AccountId aliceId;

    private final PrivateKey bobKey;
    private final AccountId bobId;

    private final PrivateKey charlieKey;
    private final AccountId charlieId;

    public static void main(String[] args) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        AccountAllowanceExample example = new AccountAllowanceExample();
        example.demonstrateAllowances();
        example.cleanUp();
        System.out.println("End of example");
    }

    private AccountAllowanceExample() throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        client = Client.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);
        Objects.requireNonNull(client.getOperatorAccountId());

        System.out.println("Generating accounts for example...");

        aliceKey = PrivateKey.generateED25519();
        bobKey = PrivateKey.generateED25519();
        charlieKey = PrivateKey.generateED25519();

        aliceId = new AccountCreateTransaction()
            .setKey(aliceKey)
            .setInitialBalance(Hbar.from(5))
            .execute(client)
            .getReceipt(client)
            .accountId;
        Objects.requireNonNull(aliceId);

        bobId = new AccountCreateTransaction()
            .setKey(bobKey)
            .setInitialBalance(Hbar.from(5))
            .execute(client)
            .getReceipt(client)
            .accountId;
        Objects.requireNonNull(bobId);

        charlieId = new AccountCreateTransaction()
            .setKey(charlieKey)
            .setInitialBalance(Hbar.from(5))
            .execute(client)
            .getReceipt(client)
            .accountId;
        Objects.requireNonNull(charlieId);

        System.out.println("Alice ID: " + aliceId);
        System.out.println("Bob ID: " + bobId);
        System.out.println("Charlie ID: " + charlieId);

        printBalances();
    }

    private void printBalances() throws PrecheckStatusException, TimeoutException {
        System.out.println(
            "Alice's balance: " +
                new AccountBalanceQuery().setAccountId(aliceId).execute(client).hbars
        );
        System.out.println(
            "Bob's balance: " +
                new AccountBalanceQuery().setAccountId(bobId).execute(client).hbars
        );
        System.out.println(
            "Charlie's balance: " +
                new AccountBalanceQuery().setAccountId(charlieId).execute(client).hbars
        );
        System.out.println(
            "Hbar allowances where Alice is the owner: " +
                new AccountInfoQuery().setAccountId(aliceId).execute(client).hbarAllowances
        );
    }

    private void demonstrateAllowances() throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        System.out.println("Approving an allowance of 2 Hbar with owner Alice and spender Bob");

        new AccountAllowanceApproveTransaction()
            .approveHbarAllowance(aliceId, bobId, Hbar.from(2))
            .freezeWith(client)
            .sign(aliceKey)
            .execute(client)
            .getReceipt(client);

        printBalances();

        System.out.println("Transferring 1 Hbar from Alice to Charlie, but the transaction is signed _only_ by Bob (Bob is dipping into his allowance from Alice)");

        new TransferTransaction()
            // "addApproved*Transfer()" means that the transfer has been approved by an allowance
            .addApprovedHbarTransfer(aliceId, Hbar.from(1).negated())
            .addHbarTransfer(charlieId, Hbar.from(1))
            // The allowance spender must be pay the fee for the transaction.
            // use setTransactionId() to set the account ID that will pay the fee for the transaction.
            .setTransactionId(TransactionId.generate(bobId))
            .freezeWith(client)
            .sign(bobKey)
            .execute(client)
            .getReceipt(client);

        System.out.println("Transfer succeeded.  Bob should now have 1 Hbar left in his allowance.");

        printBalances();

        try {
            System.out.println("Attempting to transfer 2 Hbar from Alice to Charlie using Bob's allowance.");
            System.out.println("This should fail, because there is only 1 Hbar left in Bob's allowance.");

            new TransferTransaction()
                .addApprovedHbarTransfer(aliceId, Hbar.from(2).negated())
                .addHbarTransfer(charlieId, Hbar.from(2))
                .setTransactionId(TransactionId.generate(bobId))
                .freezeWith(client)
                .sign(bobKey)
                .execute(client)
                .getReceipt(client);

            System.out.println("The transfer succeeded.  This should not happen.");

        } catch (Throwable error) {
            System.out.println("The transfer failed as expected.");
            System.out.println(error.getMessage());
        }

        System.out.println("Adjusting Bob's allowance, increasing it by 2 Hbar.  After this, Bob's allowance should be 3 Hbar.");

        new AccountAllowanceAdjustTransaction()
            .grantHbarAllowance(aliceId, bobId, Hbar.from(2))
            .freezeWith(client)
            .sign(aliceKey)
            .execute(client)
            .getReceipt(client);

        printBalances();

        System.out.println("Attempting to transfer 2 Hbar from Alice to Charlie using Bob's allowance again.");
        System.out.println("This time it should succeed.");

        new TransferTransaction()
            .addApprovedHbarTransfer(aliceId, Hbar.from(2).negated())
            .addHbarTransfer(charlieId, Hbar.from(2))
            .setTransactionId(TransactionId.generate(bobId))
            .freezeWith(client)
            .sign(bobKey)
            .execute(client)
            .getReceipt(client);

        System.out.println("Transfer succeeded.");

        printBalances();

        System.out.println("Deleting all Hbar allowances owned by Alice");

        new AccountAllowanceDeleteTransaction()
            .deleteAllHbarAllowances(aliceId)
            .freezeWith(client)
            .sign(aliceKey)
            .execute(client)
            .getReceipt(client);

        printBalances();
    }

    private void cleanUp() throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        System.out.println("Cleaning up...");

        new AccountDeleteTransaction()
            .setAccountId(aliceId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(aliceKey)
            .execute(client)
            .getReceipt(client);

        new AccountDeleteTransaction()
            .setAccountId(bobId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(bobKey)
            .execute(client)
            .getReceipt(client);

        new AccountDeleteTransaction()
            .setAccountId(charlieId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(charlieKey)
            .execute(client)
            .getReceipt(client);

        client.close();
    }
}
