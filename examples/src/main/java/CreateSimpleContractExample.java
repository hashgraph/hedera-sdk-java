import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.ContractCallQuery;
import com.hedera.hashgraph.sdk.ContractCreateTransaction;
import com.hedera.hashgraph.sdk.ContractDeleteTransaction;
import com.hedera.hashgraph.sdk.ContractFunctionResult;
import com.hedera.hashgraph.sdk.ContractId;
import com.hedera.hashgraph.sdk.FileCreateTransaction;
import com.hedera.hashgraph.sdk.FileId;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.TransactionResponse;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public final class CreateSimpleContractExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private CreateSimpleContractExample() {
    }

    public static void main(String[] args) throws PrecheckStatusException, IOException, TimeoutException, ReceiptStatusException {
        ClassLoader cl = CreateSimpleContractExample.class.getClassLoader();

        Gson gson = new Gson();

        JsonObject jsonObject;

        try (InputStream jsonStream = cl.getResourceAsStream("hello_world.json")) {
            if (jsonStream == null) {
                throw new RuntimeException("failed to get hello_world.json");
            }

            jsonObject = gson.fromJson(new InputStreamReader(jsonStream, StandardCharsets.UTF_8), JsonObject.class);
        }

        String byteCodeHex = jsonObject.getAsJsonPrimitive("object")
            .getAsString();


        Client client = Client.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // create the contract's bytecode file
        TransactionResponse fileTransactionResponse = new FileCreateTransaction()
            // Use the same key as the operator to "own" this file
            .setKeys(OPERATOR_KEY)
            .setContents(byteCodeHex.getBytes(StandardCharsets.UTF_8))
            .setMaxTransactionFee(new Hbar(2))
            .execute(client);


        TransactionReceipt fileReceipt = fileTransactionResponse.getReceipt(client);
        FileId newFileId = Objects.requireNonNull(fileReceipt.fileId);

        System.out.println("contract bytecode file: " + newFileId);

        // create the contract itself
        TransactionResponse contractTransactionResponse = new ContractCreateTransaction()
            .setGas(500)
            .setBytecodeFileId(newFileId)
            // set an admin key so we can delete the contract later
            .setAdminKey(OPERATOR_KEY)
            .setMaxTransactionFee(new Hbar(16))
            .execute(client);


        TransactionReceipt contractReceipt = contractTransactionResponse.getReceipt(client);

        System.out.println(contractReceipt);

        ContractId newContractId = Objects.requireNonNull(contractReceipt.contractId);

        System.out.println("new contract ID: " + newContractId);

        ContractFunctionResult contractCallResult = new ContractCallQuery()
            .setGas(600)
            .setContractId(newContractId)
            .setFunction("greet")
            .setMaxQueryPayment(new Hbar(1))
            .execute(client);

        if (contractCallResult.errorMessage != null) {
            System.out.println("error calling contract: " + contractCallResult.errorMessage);
            return;
        }

        String message = contractCallResult.getString(0);
        System.out.println("contract message: " + message);

        // now delete the contract
        TransactionReceipt contractDeleteResult = new ContractDeleteTransaction()
            .setContractId(newContractId)
            .setTransferAccountId(OPERATOR_ID)
            .setMaxTransactionFee(new Hbar(1))
            .execute(client)
            .getReceipt(client);

        if (contractDeleteResult.status != Status.SUCCESS) {
            System.out.println("error deleting contract: " + contractDeleteResult.status);
            return;
        }
        System.out.println("Contract successfully deleted");
    }
}
