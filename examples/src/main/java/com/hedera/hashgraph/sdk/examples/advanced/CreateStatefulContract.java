package com.hedera.hashgraph.sdk.examples.advanced;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.FunctionResult;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.contract.ContractCallQuery;
import com.hedera.hashgraph.sdk.contract.ContractCreateTransaction;
import com.hedera.hashgraph.sdk.contract.ContractExecuteTransaction;
import com.hedera.hashgraph.sdk.contract.ContractFunctionParams;
import com.hedera.hashgraph.sdk.contract.ContractId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.file.FileCreateTransaction;
import com.hedera.hashgraph.sdk.file.FileId;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

import io.github.cdimascio.dotenv.Dotenv;

public final class CreateStatefulContract {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId NODE_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("NODE_ID")));
    private static final String NODE_ADDRESS = Objects.requireNonNull(Dotenv.load().get("NODE_ADDRESS"));
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final Ed25519PrivateKey OPERATOR_KEY = Ed25519PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    private CreateStatefulContract() { }

    public static void main(String[] args) throws HederaException, IOException, InterruptedException {
        ClassLoader cl = CreateStatefulContract.class.getClassLoader();

        Gson gson = new Gson();

        JsonObject jsonObject;

        try (InputStream jsonStream = cl.getResourceAsStream("stateful.json")) {
            if (jsonStream == null) {
                throw new RuntimeException("failed to get stateful.json");
            }

            jsonObject = gson.fromJson(new InputStreamReader(jsonStream), JsonObject.class);
        }

        String byteCodeHex = jsonObject.getAsJsonPrimitive("object")
            .getAsString();
        byte[] byteCode = byteCodeHex.getBytes();

        // To improve responsiveness, you should specify multiple nodes using the
        // `Client(<Map<AccountId, String>>)` constructor instead
        Client client = new Client(NODE_ID, NODE_ADDRESS);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // default max fee for all transactions executed by this client
        client.setMaxTransactionFee(100_000_000_000L);
        client.setMaxQueryPayment(1_000_000_000L);

        // create the contract's bytecode file
        TransactionId fileTxId = new FileCreateTransaction()
            // Use the same key as the operator to "own" this file
            .addKey(OPERATOR_KEY.getPublicKey())
            .setContents(byteCode)
            .execute(client);

        TransactionReceipt fileReceipt = fileTxId.getReceipt(client);
        FileId newFileId = fileReceipt.getFileId();

        System.out.println("contract bytecode file: " + newFileId);

        TransactionId contractTxId = new ContractCreateTransaction()
            .setBytecodeFile(newFileId)
            .setGas(100_000_000)
            .setConstructorParams(
                new ContractFunctionParams()
                    .addString("hello from hedera!"))
            .execute(client);

        TransactionReceipt contractReceipt = contractTxId.getReceipt(client);
        ContractId newContractId = contractReceipt.getContractId();

        System.out.println("new contract ID: " + newContractId);

        FunctionResult contractCallResult = new ContractCallQuery()
            .setContractId(newContractId)
            .setGas(1000)
            .setFunction("get_message")
            .execute(client);

        if (contractCallResult.getErrorMessage() != null) {
            System.out.println("error calling contract: " + contractCallResult.getErrorMessage());
            return;
        }

        String message = contractCallResult.getString(0);
        System.out.println("contract returned message: " + message);

        TransactionId contractExecTxnId = new ContractExecuteTransaction()
            .setContractId(newContractId)
            .setGas(100_000_000)
            .setFunction("set_message", new ContractFunctionParams()
                .addString("hello from hedera again!"))
            .execute(client);

        // if this doesn't throw then we know the contract executed successfully
        contractExecTxnId.getReceipt(client);

        // now query contract
        FunctionResult contractUpdateResult = new ContractCallQuery()
            .setContractId(newContractId)
            .setGas(100_000_000)
            .setFunction("get_message")
            .execute(client);

        if (contractUpdateResult.getErrorMessage() != null) {
            System.out.println("error calling contract: " + contractUpdateResult.getErrorMessage());
            return;
        }

        String message2 = contractUpdateResult.getString(0);
        System.out.println("contract returned message: " + message2);
    }
}
