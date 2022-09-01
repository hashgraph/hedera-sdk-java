import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.ContractFunctionParameters;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

/*
This example just instantiates the solidity contract
defined in examples/src/main/resources/precompile-example/PrecompileExample.sol, which has been
compiled into examples/src/main/resources/precompile-example/PrecompileExample.json.

You should go look at that PrecompileExample.sol file, because that's where the meat of this example is.

This example uses the ContractHelper class (defined in ./ContractHelper.java) to declutter things.
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
        Client client = Client.forName(HEDERA_NETWORK);

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

        ContractHelper contractHelper = new ContractHelper(
            ContractHelper.getJsonResource("precompile-example/PrecompileExample.json"),
            new ContractFunctionParameters()
                .addAddress(aliceAccountId.toSolidityAddress())
                .addAddress(bobAccountId.toSolidityAddress()),
            client
        );

        contractHelper
            .setResultValidator(0, contractFunctionResult -> {
                System.out.println("getPseudoRandomSeed() returned " + Arrays.toString(contractFunctionResult.getBytes32(0)));
                return true;
            }).setPayableAmount(1, Hbar.from(30))
            .setParameterSupplier(2, () -> {
                return new ContractFunctionParameters()
                    // when contracts work with a public key, they handle the ASN1-DER encoded bytes of the public key
                    .addBytes(alicePublicKey.toBytesDER());
            }).setPayableAmount(2, Hbar.from(40))
            .executeSteps(2, 3, client);
    }
}
