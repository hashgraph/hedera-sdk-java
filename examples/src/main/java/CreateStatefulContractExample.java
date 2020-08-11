import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.ContractCallQuery;
import com.hedera.hashgraph.sdk.ContractCreateTransaction;
import com.hedera.hashgraph.sdk.ContractExecuteTransaction;
import com.hedera.hashgraph.sdk.ContractFunctionParameters;
import com.hedera.hashgraph.sdk.ContractFunctionResult;
import com.hedera.hashgraph.sdk.ContractId;
import com.hedera.hashgraph.sdk.FileCreateTransaction;
import com.hedera.hashgraph.sdk.FileId;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.HederaPreCheckStatusException;
import com.hedera.hashgraph.sdk.HederaReceiptStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TransactionReceipt;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.cdimascio.dotenv.Dotenv;

public final class CreateStatefulContractExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    private CreateStatefulContractExample() { }

    public static void main(String[] args) throws HederaPreCheckStatusException, TimeoutException, IOException, HederaReceiptStatusException {
        ClassLoader cl = CreateStatefulContractExample.class.getClassLoader();

        Gson gson = new Gson();

        JsonObject jsonObject;

        try (InputStream jsonStream = cl.getResourceAsStream("stateful.json")) {
            if (jsonStream == null) {
                throw new RuntimeException("failed to get stateful.json");
            }

            jsonObject = gson.fromJson(new InputStreamReader(jsonStream, StandardCharsets.UTF_8), JsonObject.class);
        }

        String byteCodeHex = jsonObject.getAsJsonPrimitive("object")
            .getAsString();
        byte[] byteCode = byteCodeHex.getBytes(StandardCharsets.UTF_8);

        // `Client.forMainnet()` is provided for connecting to Hedera mainnet
        Client client = Client.forTestnet();

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // default max fee for all transactions executed by this client
        client.setMaxTransactionFee(new Hbar(100));
        client.setMaxQueryPayment(new Hbar(10));

        // create the contract's bytecode file
        var fileTransactionResponse = new FileCreateTransaction()
            // Use the same key as the operator to "own" this file
            .setKeys(OPERATOR_KEY)
            .setContents(byteCode)
            .execute(client);


        TransactionReceipt fileReceipt = fileTransactionResponse.getReceipt(client);
        FileId newFileId = Objects.requireNonNull(fileReceipt.fileId);

        System.out.println("contract bytecode file: " + newFileId);

        var contractTransactionResponse = new ContractCreateTransaction()
            .setBytecodeFileId(newFileId)
            .setGas(100_000_000)
            .setConstructorParameters(
                new ContractFunctionParameters()
                    .addString("hello from hedera!"))
            .execute(client);


        TransactionReceipt contractReceipt = contractTransactionResponse.getReceipt(client);
        ContractId newContractId = Objects.requireNonNull(contractReceipt.contractId);

        System.out.println("new contract ID: " + newContractId);

        ContractFunctionResult contractCallResult = new ContractCallQuery()
            .setContractId(newContractId)
            .setGas(1000)
            .setFunction("get_message")
            .execute(client);

        if (contractCallResult.errorMessage != null) {
            System.out.println("error calling contract: " + contractCallResult.errorMessage);
            return;
        }

        String message = contractCallResult.getString(0);
        System.out.println("contract returned message: " + message);

        var contractExecTransactionResponse = new ContractExecuteTransaction()
            .setContractId(newContractId)
            .setGas(100_000_000)
            .setFunction("set_message", new ContractFunctionParameters()
                .addString("hello from hedera again!"))
            .execute(client);


        // if this doesn't throw then we know the contract executed successfully
        contractExecTransactionResponse.getReceipt(client);

        // now query contract
        ContractFunctionResult contractUpdateResult = new ContractCallQuery()
            .setContractId(newContractId)
            .setGas(100_000_000)
            .setFunction("get_message")
            .execute(client);

        if (contractUpdateResult.errorMessage != null) {
            System.out.println("error calling contract: " + contractUpdateResult.errorMessage);
            return;
        }

        String message2 = contractUpdateResult.getString(0);
        System.out.println("contract returned message: " + message2);
    }
}
