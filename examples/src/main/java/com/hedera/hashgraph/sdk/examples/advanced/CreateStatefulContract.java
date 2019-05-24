package com.hedera.hashgraph.sdk.examples.advanced;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hedera.hashgraph.sdk.CallParams;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.contract.ContractCallQuery;
import com.hedera.hashgraph.sdk.contract.ContractCreateTransaction;
import com.hedera.hashgraph.sdk.contract.ContractExecuteTransaction;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;
import com.hedera.hashgraph.sdk.file.FileCreateTransaction;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;

public final class CreateStatefulContract {
    public static void main(String[] args) throws HederaException, IOException {
        var cl = CreateStatefulContract.class.getClassLoader();

        var gson = new Gson();

        JsonObject jsonObject;

        try (var jsonStream = cl.getResourceAsStream("hello_world.json")) {
            if (jsonStream == null) {
                throw new RuntimeException("failed to get hello_world.json");
            }

            jsonObject = gson.fromJson(new InputStreamReader(jsonStream), JsonObject.class);
        }

        var byteCodeHex = jsonObject.getAsJsonPrimitive("object")
            .getAsString();
        var byteCode = ExampleHelper.parseHex(byteCodeHex);

        var operatorKey = ExampleHelper.getOperatorKey();
        var client = ExampleHelper.createHederaClient();

        // create the contract's bytecode file
        var fileTx = new FileCreateTransaction(client).setExpirationTime(
            Instant.now()
                .plus(Duration.ofSeconds(2592000))
        )
            // Use the same key as the operator to "own" this file
            .addKey(operatorKey.getPublicKey())
            .setContents(byteCode);

        var fileReceipt = fileTx.executeForReceipt();
        var newFileId = fileReceipt.getFileId();

        System.out.println("contract bytecode file: " + newFileId);

        var contractTx = new ContractCreateTransaction(client).setBytecodeFile(newFileId)
            .setConstructorParams(
                CallParams.constructor()
                    .add("hello from hedera!")
            )
            // own the contract so we can destroy it later
            .setAdminKey(operatorKey.getPublicKey());

        var contractReceipt = contractTx.executeForReceipt();
        var newContractId = contractReceipt.getContractId();

        System.out.println("new contract ID: " + newContractId);

        var contractCallResult = new ContractCallQuery(client).setContractId(newContractId)
            .setFunctionParameters(CallParams.function("get_message"))
            .execute();

        if (contractCallResult.getErrorMessage() != null) {
            System.out.println("error calling contract: " + contractCallResult.getErrorMessage());
            return;
        }

        var message = contractCallResult.getString();
        System.out.println("contract returned message: " + message);

        new ContractExecuteTransaction(client).setContractId(newContractId)
            .setFunctionParameters(CallParams.function("set_message")
                .add("hello from hedera again!")
            )
            .execute();

        var contractUpdateResult = new ContractCallQuery(client).setContractId(newContractId)
            .setFunctionParameters(CallParams.function("get_message"))
            .execute();

        if (contractUpdateResult.getErrorMessage() != null) {
            System.out.println("error calling contract: " + contractUpdateResult.getErrorMessage());
            return;
        }

        var contractCallResult2 = new ContractCallQuery(client).setContractId(newContractId)
            .setFunctionParameters(CallParams.function("get_message"))
            .execute();

        if (contractCallResult2.getErrorMessage() != null) {
            System.out.println("error calling contract: " + contractCallResult2.getErrorMessage());
            return;
        }

        var message2 = contractCallResult.getString();
        System.out.println("contract returned message: " + message2);
    }
}
