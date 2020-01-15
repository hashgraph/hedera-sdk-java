package com.hedera.hashgraph.sdk.examples;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.contract.*;
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
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final Ed25519PrivateKey OPERATOR_KEY = Ed25519PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    private CreateStatefulContract() { }

    public static void main(String[] args) throws HederaStatusException, IOException, InterruptedException {
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

        // `Client.forMainnet()` is provided for connecting to Hedera mainnet
        Client client = Client.forTestnet();

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // default max fee for all transactions executed by this client
        client.setMaxTransactionFee(new Hbar(100));
        client.setMaxQueryPayment(new Hbar(10));

        // create the contract's bytecode file
        TransactionId fileTxId = new FileCreateTransaction()
            // Use the same key as the operator to "own" this file
            .addKey(OPERATOR_KEY.publicKey)
            .setContents(byteCode)
            .execute(client);

        TransactionReceipt fileReceipt = fileTxId.getReceipt(client);
        FileId newFileId = fileReceipt.getFileId();

        System.out.println("contract bytecode file: " + newFileId);

        TransactionId contractTxId = new ContractCreateTransaction()
            .setBytecodeFileId(newFileId)
            .setGas(100_000_000)
            .setConstructorParams(
                new ContractFunctionParams()
                    .addString("hello from hedera!"))
            .execute(client);

        TransactionReceipt contractReceipt = contractTxId.getReceipt(client);
        ContractId newContractId = contractReceipt.getContractId();

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

        TransactionId contractExecTxnId = new ContractExecuteTransaction()
            .setContractId(newContractId)
            .setGas(100_000_000)
            .setFunction("set_message", new ContractFunctionParams()
                .addString("hello from hedera again!"))
            .execute(client);

        // if this doesn't throw then we know the contract executed successfully
        contractExecTxnId.getReceipt(client);

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
