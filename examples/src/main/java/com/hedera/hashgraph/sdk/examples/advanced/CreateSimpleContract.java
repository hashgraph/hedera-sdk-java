package com.hedera.hashgraph.sdk.examples.advanced;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hedera.hashgraph.sdk.CallParams;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.FunctionResult;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.Transaction;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.contract.ContractCallQuery;
import com.hedera.hashgraph.sdk.contract.ContractCreateTransaction;
import com.hedera.hashgraph.sdk.contract.ContractDeleteTransaction;
import com.hedera.hashgraph.sdk.contract.ContractId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.file.FileCreateTransaction;
import com.hedera.hashgraph.sdk.file.FileId;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import io.github.cdimascio.dotenv.Dotenv;

public final class CreateSimpleContract {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId NODE_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("NODE_ID")));
    private static final String NODE_ADDRESS = Objects.requireNonNull(Dotenv.load().get("NODE_ADDRESS"));
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final Ed25519PrivateKey OPERATOR_KEY = Ed25519PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    private CreateSimpleContract() { }

    public static void main(String[] args) throws HederaException, IOException {
        ClassLoader cl = CreateSimpleContract.class.getClassLoader();

        Gson gson = new Gson();

        JsonObject jsonObject;

        try (InputStream jsonStream = cl.getResourceAsStream("hello_world.json")) {
            if (jsonStream == null) {
                throw new RuntimeException("failed to get hello_world.json");
            }

            jsonObject = gson.fromJson(new InputStreamReader(jsonStream), JsonObject.class);
        }

        String byteCodeHex = jsonObject.getAsJsonPrimitive("object")
            .getAsString();

        // To improve responsiveness, you should specify multiple nodes using the
        // `Client(<Map<AccountId, String>>)` constructor instead
        Client client = new Client(NODE_ID, NODE_ADDRESS);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // create the contract's bytecode file
        Transaction fileTx = new FileCreateTransaction(client).setExpirationTime(
            Instant.now()
                .plus(Duration.ofSeconds(3600)))
            // Use the same key as the operator to "own" this file
            .addKey(OPERATOR_KEY.getPublicKey())
            .setContents(byteCodeHex.getBytes())
            .build();

        fileTx.execute();

        TransactionReceipt fileReceipt = fileTx.queryReceipt();
        FileId newFileId = fileReceipt.getFileId();

        System.out.println("contract bytecode file: " + newFileId);

        // create the contract itself
        Transaction contractTx = new ContractCreateTransaction(client).setAutoRenewPeriod(Duration.ofHours(1))
            .setGas(217000)
            .setBytecodeFile(newFileId)
            // set an admin key so we can delete the contract later
            .setAdminKey(OPERATOR_KEY.getPublicKey())
            .build();

        // important: submit the transaction to the network
        contractTx.execute();

        TransactionReceipt contractReceipt = contractTx.executeForReceipt();

        System.out.println(contractReceipt.toProto());

        ContractId newContractId = contractReceipt.getContractId();

        System.out.println("new contract ID: " + newContractId);

        FunctionResult contractCallResult = new ContractCallQuery(client).setGas(30000)
            .setContractId(newContractId)
            .setFunctionParameters(CallParams.function("greet"))
            .execute();

        if (contractCallResult.getErrorMessage() != null) {
            System.out.println("error calling contract: " + contractCallResult.getErrorMessage());
            return;
        }

        String message = contractCallResult.getString();
        System.out.println("contract message: " + message);

        // now delete the contract
        Transaction contractDeleteTxn = new ContractDeleteTransaction(client)
            .setContractId(newContractId)
            .build();

        contractDeleteTxn.execute();

        TransactionReceipt contractDeleteResult = contractDeleteTxn.queryReceipt();

        if (contractDeleteResult.getStatus() != ResponseCodeEnum.SUCCESS) {
            System.out.println("error deleting contract: " + contractDeleteResult.getStatus());
            return;
        }
        System.out.println("Contract successfully deleted");
    }
}
