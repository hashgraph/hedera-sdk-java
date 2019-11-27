package com.hedera.hashgraph.sdk.examples.advanced;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hedera.hashgraph.sdk.CallParams;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.FunctionResult;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.contract.ContractCallQuery;
import com.hedera.hashgraph.sdk.contract.ContractCreateTransaction;
import com.hedera.hashgraph.sdk.contract.ContractExecuteTransaction;
import com.hedera.hashgraph.sdk.contract.ContractId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.file.FileCreateTransaction;
import com.hedera.hashgraph.sdk.file.FileId;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
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

        Ed25519PrivateKey operatorKey = Ed25519PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
        // To connect to a network with more nodes, add additional entries to the network map
        String nodeAddress = Objects.requireNonNull(Dotenv.load().get("NODE_ADDRESS"));
        Client client1 = new Client(AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("NODE_ID"))), nodeAddress);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client1.setOperator(AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID"))), Ed25519PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY"))));

        Client client = client1;

        // create the contract's bytecode file
        FileCreateTransaction fileTx = new FileCreateTransaction(client).setExpirationTime(
            Instant.now()
                .plus(Duration.ofSeconds(2592000)))
            // Use the same key as the operator to "own" this file
            .addKey(operatorKey.getPublicKey())
            .setContents(byteCode)
            .setTransactionFee(1_000_000_000);

        TransactionReceipt fileReceipt = fileTx.executeForReceipt();
        FileId newFileId = fileReceipt.getFileId();

        System.out.println("contract bytecode file: " + newFileId);

        ContractCreateTransaction contractTx = new ContractCreateTransaction(client).setBytecodeFile(newFileId)
            .setAutoRenewPeriod(Duration.ofHours(1))
            .setGas(100_000_000)
            .setTransactionFee(1_000_000_000)
            .setConstructorParams(
                CallParams.constructor()
                    .addString("hello from hedera!"));

        TransactionReceipt contractReceipt = contractTx.executeForReceipt();
        ContractId newContractId = contractReceipt.getContractId();

        System.out.println("new contract ID: " + newContractId);

        FunctionResult contractCallResult = new ContractCallQuery(client).setContractId(newContractId)
            .setGas(100_000_000)
            .setFunctionParameters(CallParams.function("get_message"))
            .setPaymentDefault(8_000_000)
            .execute();

        if (contractCallResult.getErrorMessage() != null) {
            System.out.println("error calling contract: " + contractCallResult.getErrorMessage());
            return;
        }

        String message = contractCallResult.getString();
        System.out.println("contract returned message: " + message);

        new ContractExecuteTransaction(client).setContractId(newContractId)
            .setGas(100_000_000)
            .setFunctionParameters(CallParams.function("set_message")
                .addString("hello from hedera again!"))
            .setTransactionFee(800_000_000)
            .execute();

        // sleep a few seconds to allow consensus + smart contract exec
        System.out.println("Waiting 5s for consensus and contract execution");
        Thread.sleep(5000);
        // now query contract
        FunctionResult contractUpdateResult = new ContractCallQuery(client).setContractId(newContractId)
            .setGas(100_000_000)
            .setFunctionParameters(CallParams.function("get_message"))
            .setPaymentDefault(800_000_000)
            .execute();

        if (contractUpdateResult.getErrorMessage() != null) {
            System.out.println("error calling contract: " + contractUpdateResult.getErrorMessage());
            return;
        }

        String message2 = contractUpdateResult.getString();
        System.out.println("contract returned message: " + message2);
    }
}
