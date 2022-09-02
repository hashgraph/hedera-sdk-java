import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.ContractFunctionParameters;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

/*
This example just instantiates the solidity contract
defined in examples/src/main/resources/precompile-example/PrecompileExample.sol, which has been
compiled into examples/src/main/resources/precompile-example/PrecompileExample.json.

You should go look at that PrecompileExample.sol file, because that's where the meat of this example is.

This example uses the ContractHelper class (defined in ./ContractHelper.java) to declutter things.

When it spits out a raw response code, you can look it up here: https://github.com/hashgraph/hedera-protobufs/blob/main/services/response_code.proto
 */

public class SolidityPrecompileExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private SolidityPrecompileExample() {
    }

    public static void main(String[] args) throws TimeoutException, PrecheckStatusException, ReceiptStatusException, IOException {
        try (Client client = Client.forName(HEDERA_NETWORK)) {

            // Defaults the operator account ID and key such that all generated transactions will be paid for
            // by this account and be signed by this key
            client.setOperator(OPERATOR_ID, OPERATOR_KEY);

            // We need some new accounts for the contract to interact with in some of its steps

            PrivateKey alicePrivateKey = PrivateKey.generateED25519();
            PublicKey alicePublicKey = alicePrivateKey.getPublicKey();
            AccountId aliceAccountId = Objects.requireNonNull(new AccountCreateTransaction()
                .setKey(alicePublicKey)
                .setInitialBalance(Hbar.from(10))
                .execute(client)
                .getReceipt(client)
                .accountId
            );

            PrivateKey bobPrivateKey = PrivateKey.generateED25519();
            PublicKey bobPublicKey = bobPrivateKey.getPublicKey();
            AccountId bobAccountId = Objects.requireNonNull(new AccountCreateTransaction()
                .setKey(alicePublicKey)
                .setInitialBalance(Hbar.from(10))
                .execute(client)
                .getReceipt(client)
                .accountId
            );

            // Instantiate ContractHelper

            ContractHelper contractHelper = new ContractHelper(
                ContractHelper.getJsonResource("precompile-example/PrecompileExample.json"),
                new ContractFunctionParameters()
                    .addAddress(OPERATOR_ID.toSolidityAddress())
                    .addAddress(aliceAccountId.toSolidityAddress())
                    .addAddress(bobAccountId.toSolidityAddress()),
                client
            );

            // Configure steps in ContracHelper

            contractHelper
                .setResultValidatorForStep(0, contractFunctionResult -> {
                    System.out.println("getPseudoRandomSeed() returned " + Arrays.toString(contractFunctionResult.getBytes32(0)));
                    return true;
                }).setPayableAmountForStep(1, Hbar.from(20))
                .setParameterSupplierForStep(11, () -> {
                    return new ContractFunctionParameters()
                        // when contracts work with a public key, they handle the raw bytes of the public key
                        .addBytes(alicePublicKey.toBytesRaw());
                }).setPayableAmountForStep(11, Hbar.from(40))
                // Because we're setting the adminKey for the created NFT token to Alice's key,
                // Alice must sign the ContractExecuteTransaction.
                .addSignerForStep(11, alicePrivateKey);

            // step 0 tests PRNG
            // step 1 creates a fungible token
            // step 2 mints it
            // step 3 associates Alice with it
            // step 4 transfers it.
            // step 5 approves an allowance of the fungible token with operator as the owner and alice as the spender
            // step 6 Alice spends some of her fungible token allowance.
            // steps 7 - 10 test misc functions on the fungible token.
            // step 11 creates an NFT token
            // step 12 mints it
            // step 13 transfers it
            // step 14 approves an NFT allowance with operator as the owner and Alice as the spender
            // step 15 Alice spends some of her NFT allowance.

            contractHelper
                //.executeSteps(/* from step */ 0, /* to step */ 6, client)
                //.executeSteps(/* from step */ 10, /* to step */ 15, client);
                .executeSteps(/* from step */ 2, /* to step */ 2, client);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
